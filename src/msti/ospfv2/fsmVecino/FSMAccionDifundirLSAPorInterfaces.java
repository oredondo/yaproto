/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.Sesion;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.ospfv2.fsmInterfaz.FSMContextoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.ospfv2.mensaje.MensajeOSPFv2Hello;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateUpdate;
import msti.util.Inet4Address;

public class FSMAccionDifundirLSAPorInterfaces implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDifundirLSAPorInterfaces _instancia = new FSMAccionDifundirLSAPorInterfaces();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionDifundirLSAPorInterfaces() {
	}

	public static MensajeOSPFv2LinkStateUpdate generarMensajeOSPFv2LinkStateUpdate(FSMContextoOSPFv2Interfaz contextoI, IMensajeOSPFv2LSA lsaAEnviar) {
		
		MensajeOSPFv2LinkStateUpdate.Builder mensajeLinkStateUpdate = MensajeOSPFv2LinkStateUpdate.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeLinkStateUpdate.setPacketLength(); 					//lo introduce el Builder
		mensajeLinkStateUpdate.setRouterID(contextoI.getRouterID());
		mensajeLinkStateUpdate.setAreaID(contextoI.getAreaID());
		//mensajeLinkStateUpdate.setChecksum(); 						//lo introduce el Builder
		mensajeLinkStateUpdate.setAutype(contextoI.getAuType());
		mensajeLinkStateUpdate.setAuthentication(contextoI.getAuthenticationKey());

		//Campos LinkStateUpdate
		mensajeLinkStateUpdate.setAdvertisements(1);
		
		//Incrementar LSAge  lsa en InfTransDelay segundos
		lsaAEnviar.getHeader().incrementarLSAge((short) contextoI.getInfTransDelay());
		
		List<IMensajeOSPFv2LSA> lsaAEnviarList = new ArrayList<IMensajeOSPFv2LSA>();
		lsaAEnviarList.add(lsaAEnviar);
		mensajeLinkStateUpdate.setLSAs(lsaAEnviarList);
		
		return mensajeLinkStateUpdate.build();		
	}
	
	public void difundirLinkStateUpdatePorInterfaz(FSMMaquinaEstadosOSPFv2Interfaz interfaz, IMensajeOSPFv2LSA lsaAEnviar){
		FSMEstadoOSPFv2Interfaz estadoInterfaz = (FSMEstadoOSPFv2Interfaz) interfaz.getEstadoActivo();
		FSMContextoOSPFv2Interfaz contextoI = (FSMContextoOSPFv2Interfaz) interfaz.getContexto();
		
		//Enviar paquete LinkStateUpdate con este únic LSA en multidifusion por la red de la interfaz		
		Sesion sesion = contextoI.getSesion();
		MensajeOSPFv2LinkStateUpdate mensaje = generarMensajeOSPFv2LinkStateUpdate(contextoI, lsaAEnviar);		

		Escritura escritura = new Escritura(mensaje);
		//Si somos DR o BDR, se envia a todos los routers
		if(estadoInterfaz.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.BACKUP) ||
		estadoInterfaz.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.DR)){
			escritura.setDireccionDestino(new InetSocketAddress("224.0.0.5", 0)); //direccion AllSPFRouters
		}else{
			//Sino, solo a los DR y BDR
			escritura.setDireccionDestino(new InetSocketAddress("224.0.0.6", 0)); //direccion AllDRouters
		}
		sesion.escribir(escritura);
		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		IMensajeOSPFv2LSA lsaADifundir = (IMensajeOSPFv2LSA) o;
		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		FSMContextoOSPFv2Interfaz contextoI;
		FSMEstadoOSPFv2Interfaz estadoInterfazActual;
		
		long claveLSA = contextoV.getContextoInterfaz().getConfiguracion().claveLSA(lsaADifundir.getHeader().getLSType(), lsaADifundir.getHeader().getLinkStateID());
		
		FSMEstadoOSPFv2Vecino estadoVecinoActual;
		FSMContextoOSPFv2Vecino contextoVIterado;
		boolean difundirPorEstaInterfaz;
		boolean pasos1Cy1D;
		//se recorren todas las interfaces que se estén usando en el protocolo
		for(FSMMaquinaEstadosOSPFv2Interfaz interfaz: contextoV.getContextoInterfaz().getConfiguracion().listaFSMInterfaz){
			
			contextoI = (FSMContextoOSPFv2Interfaz) interfaz.getContexto();
			difundirPorEstaInterfaz=false;
			
			//1. Se examina cada vecino, para determinar si debe recibir el nuevo lsa
			for(FSMMaquinaEstadosOSPFv2Vecino vecino: contextoI.getListOfNeighbouringRouters().values()){
				pasos1Cy1D=true;
				contextoVIterado = (FSMContextoOSPFv2Vecino)vecino.getContexto() ;
				estadoVecinoActual = (FSMEstadoOSPFv2Vecino) vecino.getEstadoActivo();
				//1.a. Si el vecino está en estado menor a Exchange, no participa en el proceso				
				if(estadoVecinoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXCHANGE) ||
					estadoVecinoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.LOADING) ||
					estadoVecinoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.FULL)){
					//1.b. Si el vecino no está en full (solo exchange o loading), entonces
					if(estadoVecinoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXCHANGE) ||
						estadoVecinoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.LOADING)){
						//Examinar LinkStateRequestList, si tiene una instancia de esta lsa, entonces
						if(contextoVIterado.getLinkStateRequestList().containsKey(claveLSA)){							
							//Si el nuevo lsa es más reciente, quitar el lsa de la LinkStateRequestList del vecino
							if(1==vecino.determinarQueLSAEsMasReciente(contextoV, lsaADifundir.getHeader(),
							contextoVIterado.getLinkStateRequestList().get(claveLSA))){
								contextoVIterado.getLinkStateRequestList().remove(claveLSA);
							//Si tienen la misma instancia quitar el lsa de la LinkStateRequestList del vecino	
							}else if(0==vecino.determinarQueLSAEsMasReciente(contextoV, lsaADifundir.getHeader(),
							contextoVIterado.getLinkStateRequestList().get(claveLSA))){
								contextoVIterado.getLinkStateRequestList().remove(claveLSA);
							//si el lsaADifundir es más viejo, pasar al siguiente vecino sin hacer pasos C y D
							}else{
								pasos1Cy1D=false;
							}
						}
					}
					//pasos 1.c y 1.d
					if(pasos1Cy1D){
						//1.c. Si el LSA fue recibido por este vecino, pasar al siguiente				
						if(contextoV.getNeighborID() != contextoVIterado.getNeighborID()){
							//1.d.Llegados a este punto, añadir LSA a la LinkRetransmissionList
							contextoVIterado.getLinkStateRetransmissionList().put(claveLSA, lsaADifundir);
							difundirPorEstaInterfaz=true;
						}
					}
				}
			}
			
			//2. Ahora se decide si se difunde el LSA por esta interfaz.
			//Si no se ha añadido a la LinkStateRetransmissionList de ningun vecino, se pasa a la siguiente interfaz sin hacer el resto de pasos
			if(difundirPorEstaInterfaz){
				estadoInterfazActual = (FSMEstadoOSPFv2Interfaz) interfaz.getEstadoActivo();
				//3.Si el LSA se ha recibido por esta interfaz y además se ha recibido por el DR o el BDR, se pasa a la siguiente interfaz
				if(contextoV.getContextoInterfaz().getIpInterfaceAddress() == contextoI.getIpInterfaceAddress() && 
				(contextoV.getNeighborID() == contextoI.getDesignatedRouter() ||
				contextoV.getNeighborID() == contextoI.getBackupDesignatedRouter())){
					
				//4. Si el LSA se ha recibido por esta interfaz y esta está en estado Backup, se pasa a la siguiente interfaz	
				}else if(contextoV.getContextoInterfaz().getIpInterfaceAddress() == contextoI.getIpInterfaceAddress() && 
						estadoInterfazActual.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.BACKUP)){
						
				}else{
					//5. LLegados a este punto debemos difundir el lsa por esta interfaz
					//Enviar un paquete LinkStateUpdate con el nuevo lsa como contenido
					difundirLinkStateUpdatePorInterfaz(interfaz,lsaADifundir);
				}
				
			}
		}
	
	}

}
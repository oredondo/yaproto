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
import msti.ospfv2.fsmInterfaz.FSMContextoOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2DatabaseDescription;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateRequest;
import msti.util.Inet4Address;

public class FSMAccionEnviarLinkStateRequestAVecino implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarLinkStateRequestAVecino _instancia = new FSMAccionEnviarLinkStateRequestAVecino();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEnviarLinkStateRequestAVecino() {
	}

	public static MensajeOSPFv2LinkStateRequest generarMensajeOSPFv2LinkStateRequest(FSMContextoOSPFv2Vecino contextoV) {
		FSMContextoOSPFv2Interfaz contextoI = contextoV.getContextoInterfaz();
		MensajeOSPFv2LinkStateRequest.Builder mensajeLinkStateRequest = MensajeOSPFv2LinkStateRequest.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeLinkStateRequest.setPacketLength(); 					//lo introduce el Builder
		mensajeLinkStateRequest.setRouterID(contextoI.getRouterID());
		mensajeLinkStateRequest.setAreaID(contextoI.getAreaID());
		//mensajeLinkStateRequest.setChecksum(); 						//lo introduce el Builder
		mensajeLinkStateRequest.setAutype(contextoI.getAuType());
		mensajeLinkStateRequest.setAuthentication(contextoI.getAuthenticationKey());

		//Campos DatabaseDescription

		List<Integer> lSTypes = new ArrayList<Integer>();
		List<Integer> lSIDs = new ArrayList<Integer>();
		List<Integer> advertisingRouters = new ArrayList<Integer>();
		//rellenamos la Lista para que no supere el máximo numero de bytes del paquete
		//MaxByes=65000 - 24(header)=64976
		//tamaño de cada "request" = 12, asíque como mucho podriamos meter hasta 5414 lsaHeaders
		int contador=0;
		//boolean m = false;
		for(IMensajeOSPFv2LinkStateAdvertisementHeader lsaH: contextoV.getLinkStateRequestList().values()){
			contador++;
			if(contador<=5414){
				lSTypes.add((int) lsaH.getLSType().getCodigo());
				lSIDs.add(lsaH.getLinkStateID());
				advertisingRouters.add(lsaH.getAdvertisingRouter());			
			}/*else{
				m= true;
			}*/						
		}
		mensajeLinkStateRequest.setLSTypes(lSTypes);
		mensajeLinkStateRequest.setLSIDs(lSIDs);
		mensajeLinkStateRequest.setAdvertisingRouters(advertisingRouters);
		
		return mensajeLinkStateRequest.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {

		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		
		//los lsa ya recibidos se quitaron de la LinkStateRequestList al recibir un mensaje LinkStateUpdate
		
		//Enviar paquete DatabaseDescription a vecino
		Sesion sesion = contextoV.getContextoInterfaz().getSesion();
		MensajeOSPFv2LinkStateRequest mensaje = generarMensajeOSPFv2LinkStateRequest(contextoV);
		
		//Guardar mensaje en el contexto
		//contextoV.setUltimoDDPEnviado(mensaje);

		Escritura escritura = new Escritura(mensaje);
		String dirVecino = Inet4Address.fromInt(contextoV.getNeighborIPAddress()).getHostAddress();
		escritura.setDireccionDestino(new InetSocketAddress(dirVecino, 0)); //direccion del vecino
		sesion.escribir(escritura);
		
		
		
	}

}
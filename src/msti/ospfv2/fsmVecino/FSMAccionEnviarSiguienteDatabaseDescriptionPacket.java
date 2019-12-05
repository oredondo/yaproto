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
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.ospfv2.mensaje.MensajeOSPFv2DatabaseDescription;
import msti.util.Inet4Address;

public class FSMAccionEnviarSiguienteDatabaseDescriptionPacket implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarSiguienteDatabaseDescriptionPacket _instancia = new FSMAccionEnviarSiguienteDatabaseDescriptionPacket();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEnviarSiguienteDatabaseDescriptionPacket() {
	}

	
	public static MensajeOSPFv2DatabaseDescription generarMensajeOSPFv2DatabaseDescription(FSMContextoOSPFv2Vecino contextoV) {
		FSMContextoOSPFv2Interfaz contextoI = contextoV.getContextoInterfaz();
		MensajeOSPFv2DatabaseDescription.Builder mensajeDatabaseDescription = MensajeOSPFv2DatabaseDescription.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeDatabaseDescription.setPacketLength(); 					//lo introduce el Builder
		mensajeDatabaseDescription.setRouterID(contextoI.getRouterID());
		mensajeDatabaseDescription.setAreaID(contextoI.getAreaID());
		//mensajeDatabaseDescription.setChecksum(); 						//lo introduce el Builder
		mensajeDatabaseDescription.setAutype(contextoI.getAuType());
		mensajeDatabaseDescription.setAuthentication(contextoI.getAuthenticationKey());

		//Campos DatabaseDescription
		mensajeDatabaseDescription.setOptions(contextoI.getOptions());		
		mensajeDatabaseDescription.setDDSequenceNumber(contextoV.getDdSequenceNumber());

		List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders = new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
		//rellenamos la Lista para que no supere el máximo numero de bytes del paquete
		//MaxByes=65000 - 24(header)- 8 (otros campos)=64968
		//tamaño header = 20, asíque como mucho podriamos meter hasta 3248 lsaHeaders
		int contador=0;
		boolean m = false;
		for(IMensajeOSPFv2LSA lsa: contextoV.getDatabaseSummaryList().values()){
			contador++;
			if(contador<=3248){
				lSAHeaders.add(lsa.getHeader());				
			}else{
				m= true;
			}						
		}
		mensajeDatabaseDescription.setLSAHeaders(lSAHeaders);
		
		//campo imms lo último para ver saber la M (more)
		//I=0, ya que no es el primer DDP
		int immsInt=0;
		//M depende de si han cabido todos los headers o no
		if(m)
			immsInt=immsInt+2;
		//MS se saca del contexto
		if(contextoV.isMaster())
			immsInt=immsInt+1;
		
		byte imms=(byte)immsInt;
		imms = (byte) (imms & 7);
		mensajeDatabaseDescription.setIMMS(imms);
		
		return mensajeDatabaseDescription.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		
		//eliminar lsa ya enviados previamente de la DatabaseSummaryList
		List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeadersYaEnviadas = contextoV.getUltimoDDPEnviado().getLSAHeaders();
		for(IMensajeOSPFv2LinkStateAdvertisementHeader lsaH: lSAHeadersYaEnviadas){
			long claveLSAH = contextoV.getContextoInterfaz().getConfiguracion().claveLSA(lsaH.getLSType(), lsaH.getLinkStateID());
			contextoV.getDatabaseSummaryList().remove(claveLSAH);			
		}
		
		//Enviar paquete DatabaseDescription a vecino
		Sesion sesion = contextoV.getContextoInterfaz().getSesion();
		MensajeOSPFv2DatabaseDescription mensaje = generarMensajeOSPFv2DatabaseDescription(contextoV);
		
		//Guardar mensaje en el contexto
		contextoV.setUltimoDDPEnviado(mensaje);

		Escritura escritura = new Escritura(mensaje);
		String dirVecino = Inet4Address.fromInt(contextoV.getNeighborIPAddress()).getHostAddress();
		escritura.setDireccionDestino(new InetSocketAddress(dirVecino, 0)); //direccion del vecino
		sesion.escribir(escritura);

		
		
	}

}
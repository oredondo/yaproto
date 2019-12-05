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
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2DatabaseDescription;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateUpdate;
import msti.util.Inet4Address;

public class FSMAccionEnviarLinkStateUpdateAVecino implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarLinkStateUpdateAVecino _instancia = new FSMAccionEnviarLinkStateUpdateAVecino();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEnviarLinkStateUpdateAVecino() {
	}

	public static MensajeOSPFv2LinkStateUpdate generarMensajeOSPFv2LinkStateUpdate(FSMContextoOSPFv2Vecino contextoV, List<IMensajeOSPFv2LSA> lsaAEnviar) {
		FSMContextoOSPFv2Interfaz contextoI = contextoV.getContextoInterfaz();
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
		mensajeLinkStateUpdate.setAdvertisements(lsaAEnviar.size());
		
		//Incrementar LSAge de cada lsa en InfTransDelay segundos
		for(IMensajeOSPFv2LSA lsa: lsaAEnviar){
			lsa.getHeader().incrementarLSAge((short) contextoI.getInfTransDelay());
		}
		mensajeLinkStateUpdate.setLSAs(lsaAEnviar);
		
		return mensajeLinkStateUpdate.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		List<IMensajeOSPFv2LSA> lsaAEnviar = (List<IMensajeOSPFv2LSA>) o;
		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		
		//Enviar paquete LinkStateUpdate con la lista de lsa que nos viene en el argumento "o" al vecino		
		Sesion sesion = contextoV.getContextoInterfaz().getSesion();
		MensajeOSPFv2LinkStateUpdate mensaje = generarMensajeOSPFv2LinkStateUpdate(contextoV, lsaAEnviar);		

		Escritura escritura = new Escritura(mensaje);
		String dirVecino = Inet4Address.fromInt(contextoV.getNeighborIPAddress()).getHostAddress();
		escritura.setDireccionDestino(new InetSocketAddress(dirVecino, 0)); //direccion del vecino
		sesion.escribir(escritura);
		
		
		
	}

}
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
import msti.ospfv2.mensaje.MensajeOSPFv2Hello;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateAcknowledgment;
import msti.util.Inet4Address;

public class FSMAccionEnviarLinkStateAcknowledgmentAVecino implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarLinkStateAcknowledgmentAVecino _instancia = new FSMAccionEnviarLinkStateAcknowledgmentAVecino();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEnviarLinkStateAcknowledgmentAVecino() {
	}

	public static MensajeOSPFv2LinkStateAcknowledgment generarMensajeOSPFv2LinkStateAcknowledgment(FSMContextoOSPFv2Interfaz contextoI, List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders) {
		MensajeOSPFv2LinkStateAcknowledgment.Builder mensajeAck = MensajeOSPFv2LinkStateAcknowledgment.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeAck.setPacketLength(); 					//lo introduce el Builder
		mensajeAck.setRouterID(contextoI.getRouterID());
		mensajeAck.setAreaID(contextoI.getAreaID());
		//mensajeAck.setChecksum(); 						//lo introduce el Builder
		mensajeAck.setAutype(contextoI.getAuType());
		mensajeAck.setAuthentication(contextoI.getAuthenticationKey());

		//Campos LinkStateAcknowledgment
		mensajeAck.setLSAHeaders(lSAHeaders);

		return mensajeAck.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		List<IMensajeOSPFv2LinkStateAdvertisementHeader> lsaHAEnviar = (List<IMensajeOSPFv2LinkStateAdvertisementHeader>) o;
		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		
		//Enviar paquete Hello al vecino únicamente"		
		Sesion sesion = contextoV.getContextoInterfaz().getSesion();
		MensajeOSPFv2LinkStateAcknowledgment mensaje = generarMensajeOSPFv2LinkStateAcknowledgment(contextoV.getContextoInterfaz(), lsaHAEnviar);

		Escritura escritura = new Escritura(mensaje);
		String dirVecino = Inet4Address.fromInt(contextoV.getNeighborIPAddress()).getHostAddress();
		escritura.setDireccionDestino(new InetSocketAddress(dirVecino, 0)); //direccion del vecino
		sesion.escribir(escritura);		
		
		
	}

}
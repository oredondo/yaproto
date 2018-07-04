/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.savarese.rocksaw.net.RawSocket;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.Sesion;
import msti.io.SesionDatagrama;
import msti.io.SesionRawSocket;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.mensaje.MensajeOSPFv2Hello;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMAccionDifundirHelloPackets implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDifundirHelloPackets _instancia = new FSMAccionDifundirHelloPackets();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionDifundirHelloPackets() {
	}

	public static MensajeOSPFv2Hello generarMensajeOSPFv2Hello(FSMContextoOSPFv2Interfaz contextoI) {
		MensajeOSPFv2Hello.Builder mensajeHello = MensajeOSPFv2Hello.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeHello.setPacketLength(); 					//lo introduce el Builder
		mensajeHello.setRouterID(contextoI.getRouterID());
		mensajeHello.setAreaID(contextoI.getAreaID());
		//mensajeHello.setChecksum(); 						//lo introduce el Builder
		mensajeHello.setAutype(contextoI.getAuType());
		mensajeHello.setAuthentication(contextoI.getAuthenticationKey());

		//Campos Hello
		mensajeHello.setNetworkMask(contextoI.getIpInterfaceMask());
		mensajeHello.setHelloInterval(contextoI.getHelloInterval());
		mensajeHello.setOptions(contextoI.getOptions());
		mensajeHello.setRtrPri(contextoI.getRouterPriority());
		mensajeHello.setRouterDeadInterval(contextoI.getRouterDeadInterval());
		mensajeHello.setDesignatedRouter(contextoI.getDesignatedRouter());
		mensajeHello.setBackupDesignatedRouter(contextoI.getBackupDesignatedRouter());
		
		List<Integer> neighbors= new ArrayList<Integer>();
		neighbors.addAll(contextoI.getListOfNeighbouringRouters().keySet());
		mensajeHello.setNeighbors(neighbors);
		
		return mensajeHello.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Interfaz contextoI = (FSMContextoOSPFv2Interfaz) contexto;
		//Si la red es multicast o Point-to-Point
		//Enviar paquete Hello a la direcci�n multicast "AllSPFRouters"		
		Sesion sesion = contextoI.getSesion();  //TODO: una sesión por interfaz
		MensajeOSPFv2Hello mensaje = generarMensajeOSPFv2Hello(contextoI);

		Escritura escritura = new Escritura(mensaje);
		escritura.setDireccionDestino(new InetSocketAddress("224.0.0.5", 0)); //direccion AllSPFRouters
		sesion.escribir(escritura);

		
		//si la red es virtualLink
		//si la red es non-broadcast

	}

}
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
import msti.ospfv2.mensaje.MensajeOSPFv2Hello;
import msti.util.Inet4Address;

public class FSMAccionEnviarHelloAVecino implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarHelloAVecino _instancia = new FSMAccionEnviarHelloAVecino();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEnviarHelloAVecino() {
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
		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		
		//Enviar paquete Hello al vecino únicamente"		
		Sesion sesion = contextoV.getContextoInterfaz().getSesion();
		MensajeOSPFv2Hello mensaje = generarMensajeOSPFv2Hello(contextoV.getContextoInterfaz());

		Escritura escritura = new Escritura(mensaje);
		String dirVecino = Inet4Address.fromInt(contextoV.getNeighborIPAddress()).getHostAddress();
		escritura.setDireccionDestino(new InetSocketAddress(dirVecino, 0)); //direccion del vecino
		sesion.escribir(escritura);		
		
		
	}

}
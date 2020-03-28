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
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2DatabaseDescription;
import msti.util.Inet4Address;

public class FSMAccionReenviarUltimoDatabaseDescriptionPacket implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionReenviarUltimoDatabaseDescriptionPacket _instancia = new FSMAccionReenviarUltimoDatabaseDescriptionPacket();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionReenviarUltimoDatabaseDescriptionPacket() {
	}


	
	@Override
	public void execute(FSMContexto contexto, Object o) {

		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		
		//Renviar paquete ultimo DatabaseDescription a vecino		
		Sesion sesion = contextoV.getContextoInterfaz().getSesion();
		MensajeOSPFv2DatabaseDescription mensaje = contextoV.getUltimoDDPEnviado();		

		Escritura escritura = new Escritura(mensaje);
		String dirVecino = Inet4Address.fromInt(contextoV.getNeighborIPAddress()).getHostAddress();
		escritura.setDireccionDestino(new InetSocketAddress(dirVecino, 0)); //direccion del vecino
		sesion.escribir(escritura);
				
		
	}

}
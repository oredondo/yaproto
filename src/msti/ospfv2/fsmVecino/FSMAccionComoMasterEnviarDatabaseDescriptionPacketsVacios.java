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

public class FSMAccionComoMasterEnviarDatabaseDescriptionPacketsVacios implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionComoMasterEnviarDatabaseDescriptionPacketsVacios _instancia = new FSMAccionComoMasterEnviarDatabaseDescriptionPacketsVacios();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionComoMasterEnviarDatabaseDescriptionPacketsVacios() {
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
		//imms todo a 1
		byte imms = (byte) 7;
		mensajeDatabaseDescription.setIMMS(imms);
		mensajeDatabaseDescription.setDDSequenceNumber(contextoV.getDdSequenceNumber());

		//lista de headers vacía
		List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders = new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
		mensajeDatabaseDescription.setLSAHeaders(lSAHeaders);
		
		return mensajeDatabaseDescription.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {

		FSMContextoOSPFv2Vecino contextoV = (FSMContextoOSPFv2Vecino) contexto;
		contextoV.setAllDatabaseDescriptionPacketSent(false);

		//ponernos como Máster:
		contextoV.setMaster(true);
		
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
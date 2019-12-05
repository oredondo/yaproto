/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import java.util.Iterator;
import java.util.Map;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.fsm.FSMEvento;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMIdEventoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;

public class FSMAccionDestruirConexionVecinos implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDestruirConexionVecinos _instancia = new FSMAccionDestruirConexionVecinos();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionDestruirConexionVecinos() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		
		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		//Derstruye la conexión con todos los vecinos
		
		Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters=contextoI.getListOfNeighbouringRouters();
		for (Map.Entry<Integer, FSMMaquinaEstadosOSPFv2Vecino> pair : listOfNeighbouringRouters.entrySet()) {					
			FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) pair.getValue().getContexto();
			
		    //Generar evento KillNbr en todos los vecinos
			FSMEvento eventoKillNbr = new FSMEvento(FSMIdEventoOSPFv2Vecino.KILLNBR, contextoV.getNeighborID());
			((FSMMaquinaEstadosOSPFv2Vecino)contextoV.getMaquinaEstados()).encolarEvento(eventoKillNbr);
	    
		}
		
		
	}

}
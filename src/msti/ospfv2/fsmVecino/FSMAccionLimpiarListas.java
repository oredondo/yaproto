/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.util.Iterator;
import java.util.List;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;

public class FSMAccionLimpiarListas implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionLimpiarListas _instancia = new FSMAccionLimpiarListas();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionLimpiarListas() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;

		//limpiar las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		contextoV.getLinkStateRetransmissionList().clear();
		contextoV.getDatabaseSummaryList().clear();
		contextoV.getLinkStateRequestList().clear();
	}

}
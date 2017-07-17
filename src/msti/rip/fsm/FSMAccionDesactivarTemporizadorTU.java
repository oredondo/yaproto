/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.util.concurrent.ScheduledFuture;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.rip.mensaje.MensajeRIPRuta;


public class FSMAccionDesactivarTemporizadorTU implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDesactivarTemporizadorTU _instancia = new FSMAccionDesactivarTemporizadorTU();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionDesactivarTemporizadorTU() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		// Debe ser uno por ruta en la tabla de rutas
		ScheduledFuture tareaTemporizada = (ScheduledFuture) contexto.get("TemporizadorTU");
		
		if (tareaTemporizada != null) {
			tareaTemporizada.cancel(false);
			tareaTemporizada = null;
		}
	}

}

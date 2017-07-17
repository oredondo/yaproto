/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;


public class FSMAccionDesactivarTemporizadorOpSincrona implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDesactivarTemporizadorOpSincrona _instancia = new FSMAccionDesactivarTemporizadorOpSincrona();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionDesactivarTemporizadorOpSincrona() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		throw new UnsupportedOperationException("no implementado.");
/*
		TimerEventProducer timerEventProducer = (TimerEventProducer) contexto.get("TemporizadorOpSincrona");
		
		if (timer == null)
			throw new IllegalArgumentException("Objeto con clave 'TemporizadorTU' no existente en el contexto.");

		// Desactiva el temporizador
		timer.cancel();
*/	}

}

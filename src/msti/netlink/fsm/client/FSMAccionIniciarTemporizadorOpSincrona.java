/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;

public class FSMAccionIniciarTemporizadorOpSincrona implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionIniciarTemporizadorOpSincrona _instancia = new FSMAccionIniciarTemporizadorOpSincrona();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionIniciarTemporizadorOpSincrona() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		throw new UnsupportedOperationException("no implementado.");
		// Obtener timer de la lista de op síncronas en espera
/* TODO		Timer timer = map.get(idpeticion).timer;
		Long retardo = (Long)o;
		
		if (timer == null)
			throw new IllegalArgumentException("Objeto con clave 'TemporizadorOpSincrona' no existente en el contexto.");

		//  reinicia el temporizador con un tiempo aleatorio 1000-5000s como indica la norma.
		if (timer.isCancelled())
			timer.restart();
		timer.schedule(retardo.longValue()); // Valor aleatorio en rango 1000 a 5000 ms
*/
		}

}

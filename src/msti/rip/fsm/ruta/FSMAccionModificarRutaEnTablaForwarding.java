/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;

public class FSMAccionModificarRutaEnTablaForwarding implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionModificarRutaEnTablaForwarding _instancia = new FSMAccionModificarRutaEnTablaForwarding();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionModificarRutaEnTablaForwarding() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		// TODO Auto-generated method stub		
	}

}
/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;

public class FSMAccionBorrarRutaEnTablaRutas implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionBorrarRutaEnTablaRutas _instancia = new FSMAccionBorrarRutaEnTablaRutas();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionBorrarRutaEnTablaRutas() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		Long clave = (Long) o;
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		tablaRutas.removeRuta(clave);
	}
}
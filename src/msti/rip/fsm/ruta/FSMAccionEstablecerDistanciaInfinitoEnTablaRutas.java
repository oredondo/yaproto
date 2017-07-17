/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.EstadoRuta;
import msti.rip.TablaRutas.Ruta;

public class FSMAccionEstablecerDistanciaInfinitoEnTablaRutas implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEstablecerDistanciaInfinitoEnTablaRutas _instancia = new FSMAccionEstablecerDistanciaInfinitoEnTablaRutas();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEstablecerDistanciaInfinitoEnTablaRutas() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		String clave = (String) o;
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		Ruta ruta = tablaRutas.getRuta(clave);
		synchronized (ruta) {
			ruta.distancia = 16; //TODO: constante INFINITO
			ruta.estado = EstadoRuta.MODIFICADA;
			// fecha?
		}
		// Informa a la tabla de que ha modificado una ruta que contiene (sin pasar por su interfaz)
		tablaRutas.setTablaModificada();
	}
}
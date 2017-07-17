/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import java.util.concurrent.ScheduledFuture;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMAccionDesactivarTemporizadorRutaEliminar implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDesactivarTemporizadorRutaEliminar _instancia = new FSMAccionDesactivarTemporizadorRutaEliminar();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionDesactivarTemporizadorRutaEliminar() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		// Debe ser uno por ruta en la tabla de rutas
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		MensajeRIPRuta mensajeRuta = (MensajeRIPRuta) o;
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");

		// Obtiene clave para la ruta en la tabla de rutas
		String clave = TablaRutas.generarClaveTablaRutas(
				mensajeRuta.getDireccionIP(),
				mensajeRuta.getLongitudPrefijoRed()
				);

		// Obtiene y desactiva la posible tarea temporizador de esta ruta
		ScheduledFuture tareaTemporizada = tablaRutas.getRuta(clave).temporizador;
		if (tareaTemporizada != null)
			tareaTemporizada.cancel(false); 

	}

}

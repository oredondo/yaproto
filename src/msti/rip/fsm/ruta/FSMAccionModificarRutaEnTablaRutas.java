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
import msti.rip.mensaje.MensajeRIPRuta;
import msti.util.ITimerListener;

public class FSMAccionModificarRutaEnTablaRutas implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionModificarRutaEnTablaRutas _instancia = new FSMAccionModificarRutaEnTablaRutas();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionModificarRutaEnTablaRutas() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		MensajeRIPRuta mensajeRuta = (MensajeRIPRuta) o;
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");

		String clave = TablaRutas.generarClaveTablaRutas(mensajeRuta.getDireccionIP(), 
				mensajeRuta.getLongitudPrefijoRed());

		Ruta ruta = tablaRutas.getRuta(clave);
		synchronized (ruta) {
			ruta.distancia = mensajeRuta.getMetrica() + 1;
			ruta.proximoSalto = mensajeRuta.getDireccionProximoSalto();
			ruta.estado = EstadoRuta.MODIFICADA;
			// TODO: Actualizar fecha ?
		}
		// Informa a la tabla de rutas de que ha modificada una ruta extraída con get sin su conocimiento
		tablaRutas.setTablaModificada();
		
	}

}
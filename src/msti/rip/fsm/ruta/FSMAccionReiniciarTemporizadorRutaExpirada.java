/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.rip.mensaje.MensajeRIPRuta;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMAccionReiniciarTemporizadorRutaExpirada implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionReiniciarTemporizadorRutaExpirada _instancia = new FSMAccionReiniciarTemporizadorRutaExpirada();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionReiniciarTemporizadorRutaExpirada() {
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
		
		// Obtiene temporizador de esta ruta. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = tablaRutas.getRuta(clave).temporizador; 
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo 180s (180000ms) como indica la norma.
		ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorRutaExpirada#" + clave, (ITimerListener) contexto.getMaquinaEstados()), 
				180, TimeUnit.SECONDS );
	}

}

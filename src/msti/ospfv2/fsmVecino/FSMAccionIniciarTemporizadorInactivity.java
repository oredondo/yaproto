/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMAccionIniciarTemporizadorInactivity implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionIniciarTemporizadorInactivity _instancia = new FSMAccionIniciarTemporizadorInactivity();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaci√≥n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionIniciarTemporizadorInactivity() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		TablaRutas tablaRutas = (TablaRutas) contextoV.get("TablaRutas");
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		// Obtiene tarea temporizada de InactivityTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoV.getTemporizadorInactivity();
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a RouterDeadInterval en segundos
		ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorInactivity", (ITimerListener) contextoV.getMaquinaEstados()), 
				new Long(contextoV.getContextoInterfaz().getRouterDeadInterval()), TimeUnit.SECONDS );
		
		//El RouterDeadInterval est· en el contexto de la Interfaz
		contextoV.setTemporizadorInactivity(tareaPlanificada);
		
		
		
		
	}

}
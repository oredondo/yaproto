/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;


import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.ospfv2.TablaRutas;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMAccionIniciarTemporizadorWait implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionIniciarTemporizadorWait _instancia = new FSMAccionIniciarTemporizadorWait();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionIniciarTemporizadorWait() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		TablaRutas tablaRutas = (TablaRutas) contextoI.get("TablaRutas");
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		// Obtiene tarea temporizada de WaitTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoI.getTemporizadorWait(); 
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a RouterDeadInterval en segundos
		ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorWait", (ITimerListener) contextoI.getMaquinaEstados()), 
				new Long(contextoI.getRouterDeadInterval()), TimeUnit.SECONDS );
		contextoI.setTemporizadorWait(tareaPlanificada);
		
		
	}

}
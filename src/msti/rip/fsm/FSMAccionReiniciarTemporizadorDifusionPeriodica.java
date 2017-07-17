/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.util.ITimerListener;

import msti.util.TimerEventProducer;

public class FSMAccionReiniciarTemporizadorDifusionPeriodica implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionReiniciarTemporizadorDifusionPeriodica _instancia = new FSMAccionReiniciarTemporizadorDifusionPeriodica();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionReiniciarTemporizadorDifusionPeriodica() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		System.out.println("AccionReiniciarTemporizadorDifusionPeriodica: execute() " + new Date());
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");

		// Obtiene tarea temporizada de TU. 
		// La primera vez, lo programa. Como es tarea periódica, no requiere reiniciar la tarea.Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = (ScheduledFuture) contexto.get("TemporizadorDifusionPeriodica"); 
		if (tareaPlanificada == null) {
			//  reinicia el temporizador con tiempo aleatoria 1-5 s como indica la norma.
			ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
			tareaPlanificada = stpe.scheduleAtFixedRate( 
					new TimerEventProducer("TemporizadorDifusionPeriodica", (ITimerListener) contexto.getMaquinaEstados()), 
					30, 30, TimeUnit.SECONDS );
			contexto.put("TemporizadorDifusionPeriodica", tareaPlanificada);
		}
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMAccionReiniciarTemporizadorTU implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionReiniciarTemporizadorTU _instancia = new FSMAccionReiniciarTemporizadorTU();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}


	public FSMAccionReiniciarTemporizadorTU() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		Random random = (Random) contexto.get("Random");
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		if (random == null)
			throw new IllegalArgumentException("Objeto con clave 'Random' no existente en el contexto.");

		
		// Obtiene tarea temporizada de TU. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = (ScheduledFuture) contexto.get("TemporizadorTU"); 
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo aleatoria 1-5 s como indica la norma.
		ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorTU", (ITimerListener) contexto.getMaquinaEstados()), 
				(long) (random.nextInt(4001) + 1000), TimeUnit.MILLISECONDS );
		contexto.put("TemporizadorTU", tareaPlanificada);

	}

}

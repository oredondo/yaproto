/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

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

public class FSMAccionIniciarTemporizadorHello implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionIniciarTemporizadorHello _instancia = new FSMAccionIniciarTemporizadorHello();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionIniciarTemporizadorHello() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		
		TablaRutas tablaRutas = (TablaRutas) contextoI.get("TablaRutas");
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		// Obtiene tarea temporizada de HelloTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoI.getTemporizadorHello(); 
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a HelloInterval en segundos
		ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorHello", (ITimerListener) contextoI.getMaquinaEstados()), 
				new Long(contextoI.getHelloInterval()), TimeUnit.SECONDS );
		contextoI.setTemporizadorHello(tareaPlanificada);
		
		
		
		
	}

}
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

public class FSMAccionIniciarTemporizadorEnvioDDPconIMMS implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionIniciarTemporizadorEnvioDDPconIMMS _instancia = new FSMAccionIniciarTemporizadorEnvioDDPconIMMS();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionIniciarTemporizadorEnvioDDPconIMMS() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		TablaRutas tablaRutas = (TablaRutas) contextoV.get("TablaRutas");
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		// Obtiene tarea temporizada de EnvioDDPconIMMSTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoV.getTemporizadorDDPconIMMS();
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a RxmtInterval en segundos
		ScheduledThreadPoolExecutor stpe = tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorDDPconIMMS", (ITimerListener) contextoV.getMaquinaEstados()), 
				new Long(contextoV.getContextoInterfaz().getRxmtInterval()), TimeUnit.SECONDS );
		
		contextoV.setTemporizadorDDPconIMMS(tareaPlanificada);
		
		
		
		
	}

}
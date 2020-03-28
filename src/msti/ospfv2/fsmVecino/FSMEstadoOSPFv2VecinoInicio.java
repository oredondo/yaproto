/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEstadoInicio;
import msti.ospfv2.fsmVecino.FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino;

/**
 * Pseudo-estado inicio
 *
 * En RIP, antes de llegar al primer estado, difunde una solicitud (request) de tabla completa
 */
public class FSMEstadoOSPFv2VecinoInicio extends FSMEstadoInicio {
	static {
		_instancia = new FSMEstadoOSPFv2VecinoInicio(FSMIdEstadoOSPFv2Vecino.INICIO);
	}

	protected FSMEstadoOSPFv2VecinoInicio(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}
	@Override
	public void onSalida(FSMContexto contexto) {

		// Envía solicitud de tabla completa a los vecinos
		//FSMIdAccionRIP.ENVIAR_SOLICITUD_TABLACOMPLETA_A_TODOS.getInstance().execute(contexto, null);
	}

	public FSMEstado procesarEventoInicio (FSMContexto contexto) {
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, null);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Obtiene y ejecuta acciones */

		/*
		// Envía solicitud de tabla completa a los vecinos
		FSMIdAccionOSPFv2Interfaz.ENVIAR_SOLICITUD_TABLACOMPLETA_A_TODOS.getInstance().execute(contexto, null);
		// Inicia temporizador difusión periódica 30s
		FSMIdAccionOSPFv2Interfaz.REINICIAR_TEMPORIZADOR_DIFUSIONPERIODICA.getInstance().execute(contexto, null);
		*/
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
}
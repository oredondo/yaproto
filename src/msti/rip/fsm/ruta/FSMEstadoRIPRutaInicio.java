/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEstadoInicio;
import msti.rip.fsm.FSMEstadoRIP.FSMIdEstadoRIP;
import msti.rip.fsm.FSMMaquinaEstadosRIP.FSMIdAccionRIP;
import msti.rip.fsm.ruta.FSMEstadoRIPRuta.FSMIdEstadoRIPRuta;

/**
 * Pseudo-estado inicio
 *
 * En RIP, antes de llegar al primer estado, difunde una solicitud (request) de tabla completa
 */
public class FSMEstadoRIPRutaInicio extends FSMEstadoInicio {
	static {
		_instancia = new FSMEstadoRIPRutaInicio(FSMIdEstadoRIPRuta.INICIO);
	}

	protected FSMEstadoRIPRutaInicio(FSMIdEstado id) {		
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


		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
}

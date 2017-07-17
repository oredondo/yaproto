/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.rip.fsm.FSMMaquinaEstadosRIP.FSMIdAccionRIP;

public class FSMEstadoRIPTemporizadorTUActivoYTablaRutasModificada extends
		FSMEstadoRIP {
	static {
		_instancia = new FSMEstadoRIPTemporizadorTUActivoYTablaRutasModificada(
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA);
	}

	protected FSMEstadoRIPTemporizadorTUActivoYTablaRutasModificada(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	@Override
	public FSMEstado procesarEventoExpiradoTemporizadorDifusionPeriodica(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía tabla completa a todos
		FSMIdAccionRIP.ENVIAR_TABLACOMPLETA_A_TODOS.getInstance().execute(contexto, evento.getArgumento());
		// Inicia temporizador difusión periódica
		FSMIdAccionRIP.REINICIAR_TEMPORIZADOR_DIFUSIONPERIODICA.getInstance().execute(contexto, evento.getArgumento());
		// Inicia temporizador difusión periódica
		FSMIdAccionRIP.BORRAR_MARCAS_CAMBIO_EN_TABLARUTAS.getInstance().execute(contexto, evento.getArgumento());		
		// Cesa temporizador TU? Decisión: SÍ
		FSMIdAccionRIP.DESACTIVAR_TEMPORIZADOR_TU.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoExpiradoTemporizadorEsperaDifusionPorActualizacion(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía rutas modificadas
		FSMIdAccionRIP.ENVIAR_RUTASMODIFICADAS_A_TODOS.getInstance().execute(contexto, evento.getArgumento());
		// Inicia temporizador triggered-updates
		FSMIdAccionRIP.REINICIAR_TEMPORIZADOR_TU.getInstance().execute(contexto, evento.getArgumento());
		// Inicia temporizador difusión periódica
		FSMIdAccionRIP.BORRAR_MARCAS_CAMBIO_EN_TABLARUTAS.getInstance().execute(contexto, evento.getArgumento());		

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoPeticionDesdeVecino(FSMContexto contexto,
			FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía respuesta al solicitante
		FSMIdAccionRIP.ENVIAR_RUTASSOLICITADAS_A_UNO.getInstance().execute(contexto, evento.getArgumento());
		// Ambigüedad. Decisión: No limpiar las marcas de cambio en la tabla de rutas

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoTablaRutasModificada(FSMContexto contexto,
			FSMEvento evento) {


		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** No acción */
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoRespuestaDesdeVecino(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Obtiene y ejecuta acciones */

		// Notifica a cada máquina de estados de cada entrada la llegada de un evento
		FSMIdAccionRIP.GENERAR_EVENTOS_UPDATERUTA_INDIVIDUAL.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

}

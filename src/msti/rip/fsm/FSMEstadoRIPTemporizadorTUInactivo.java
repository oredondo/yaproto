/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.rip.fsm.FSMMaquinaEstadosRIP.FSMIdAccionRIP;

public class FSMEstadoRIPTemporizadorTUInactivo extends FSMEstadoRIP {
	static {
		_instancia = new FSMEstadoRIPTemporizadorTUInactivo(
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
	}

	protected FSMEstadoRIPTemporizadorTUInactivo(FSMIdEstado id) {		
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
		
		// Envía a todos
		FSMIdAccionRIP.ENVIAR_TABLACOMPLETA_A_TODOS.getInstance().execute(contexto, evento.getArgumento());
		// Reinicia temporizador
		FSMIdAccionRIP.REINICIAR_TEMPORIZADOR_DIFUSIONPERIODICA.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoExpiradoTemporizadorEsperaDifusionPorActualizacion(
			FSMContexto contexto, FSMEvento evento) {

		// No debería ocurrir. Mantiene estado.
		return this;
	}

	@Override
	public FSMEstado procesarEventoPeticionDesdeVecino(FSMContexto contexto,
			FSMEvento evento) {
		System.out.println("EstadoRIPTemporizadorTUInactivo:eventoPeticion");

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía a todos
		FSMIdAccionRIP.ENVIAR_RUTASSOLICITADAS_A_UNO.getInstance().execute(contexto, evento.getArgumento());
		/** Ambigüedad en RFC RIP. Decidido: No eliminar las marcas de cambio en la tabla de rutas (se supone que los request son para inicialización, diagnóstico,...) */
		//FSMIdAccionRIP.BORRAR_MARCAS_CAMBIO_EN_TABLARUTAS.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoTablaRutasModificada(FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía a todos
		FSMIdAccionRIP.ENVIAR_RUTASMODIFICADAS_A_TODOS.getInstance().execute(contexto, evento.getArgumento());
		// Inica temporizador TU (durante este tiempo no se transmitirán más cambios)
		FSMIdAccionRIP.REINICIAR_TEMPORIZADOR_TU.getInstance().execute(contexto, evento.getArgumento());
		// Inica temporizador TU (durante este tiempo no se transmitirán más cambios)
		FSMIdAccionRIP.BORRAR_MARCAS_CAMBIO_EN_TABLARUTAS.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	/* Este procesar es común en todos los estados */
	@Override
	public FSMEstado procesarEventoRespuestaDesdeVecino(
			FSMContexto contexto, FSMEvento evento) {
		System.out.println("EstadoRIPTemporizadorTUInactivo:eventoRespuesta");

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

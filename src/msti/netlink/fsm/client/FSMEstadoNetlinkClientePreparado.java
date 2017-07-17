/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.netlink.fsm.client.FSMMaquinaEstadosNetlinkCliente.FSMIdAccionNetlinkCliente;
import msti.rip.fsm.FSMContextoRIP;

public class FSMEstadoNetlinkClientePreparado extends FSMEstadoNetlinkCliente {
	static {
		_instancia = new FSMEstadoNetlinkClientePreparado(FSMIdEstadoNetlinkCliente.PREPARADO);
	}

	private FSMEstadoNetlinkClientePreparado(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	@Override
	public FSMEstado procesarEventoOrdenModificarRuta(FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía la solicitud de modificación
		FSMIdAccionNetlinkCliente.ENVIAR_PETICION_NUEVARUTA.getInstance().execute(contexto, evento.getArgumento());
/*		Esto estaría en el enviar_peticion_nuevaRuta:
 * 		if (esOpSincrona) {
 			// Debe traer un idRuta único, y un esOpSincrona, además de la ruta, en el argumento del evento
			// asignar un idRequest (el de la petición construida)->nl_req
			// map(idRequest, new Entry{int codigo, boolean timeout=false, Timer temporizador;)
			// inicializar temporizador a cuenta
			FSMIdAccionNetlinkCliente.INICIAR_TEMPORIZADOR_OP_SINCRONA.getInstance().execute(contexto, evento.getArgumento());
		}
*/
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoOrdenBorrarRuta(FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Envía la solicitud de modificación
		FSMIdAccionNetlinkCliente.ENVIAR_PETICION_BORRARRUTA.getInstance().execute(contexto, evento.getArgumento());
		/*		Esto estaría en el enviar_peticion_borrarRuta:
		 * 		if (esOpSincrona) {
		 			// Debe traer un idRuta único, y un esOpSincrona, además de la ruta, en el argumento del evento
					// asignar un idRequest (el de la petición construida)->nl_req
					// map(idRequest, new Entry{int codigo, int nl_req, boolean timeout=false, Timer temporizador;)
					// inicializar temporizador a cuenta
					FSMIdAccionNetlinkCliente.INICIAR_TEMPORIZADOR_OP_SINCRONA.getInstance().execute(contexto, evento.getArgumento());
				}
		*/
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;	}

	@Override
	public FSMEstado procesarEventoRespuestaControlRecibida(FSMContexto contexto,
			FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// Notifica del resultado a la posible op síncrona que esté esperando
		FSMIdAccionNetlinkCliente.NOTIFICAR_RESULTADO_OP_SINCRONA.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoRespuestaNoControlRecibida(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** No acción */
		
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoTemporizadorOpSincrona (
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Obtiene y ejecuta acciones */
		// Notifica del resultado a la posible op síncrona que esté esperando
		FSMIdAccionNetlinkCliente.NOTIFICAR_RESULTADO_OP_SINCRONA.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

}

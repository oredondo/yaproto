/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.fsm.FSMEstadoInicio;
import msti.fsm.FSMEvento;
import msti.fsm.FSMMaquinaEstados;
import msti.fsm.FSMEstado.FSMIdEstado;
import msti.fsm.FSMEvento.FSMIdEvento;
import msti.io.FiltroNotificador;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.netlink.fsm.client.FSMEstadoNetlinkCliente.FSMIdEstadoNetlinkCliente;
import msti.netlink.mensaje.IMensajeNetlink.NetlinkMessageType;
import msti.netlink.mensaje.MensajeNetlink;
import msti.rip.TablaRutas.Ruta;
import msti.util.ITimerListener;

import msti.util.TimerEventProducer;

public class FSMMaquinaEstadosNetlinkCliente extends FSMMaquinaEstados implements ITimerListener, ISesionCreadaListener, ILecturaListener, INetlinkOrden {

	
	public FSMMaquinaEstadosNetlinkCliente(FSMContexto contexto) {
		super(contexto);

		// Crea Atomic para incremento de peticiones 
		this.getContexto().put("Secuencia", new AtomicInteger());
	}

	@Override
	public void init(FSMContexto contexto) {
		// Obtiene el inicio		
		setEstado(FSMIdEstadoNetlinkCliente.INICIO.getInstance());
		// Realiza la primera transición desde INICIO
		setEstado(((FSMEstadoInicio) this.getEstadoActivo()).procesarEventoInicio(contexto));		
	}

	/** 
	 * Identificador de las acciones, que funciona a su vez como factoría (en este caso de Singleton)
	 */
	public enum FSMIdAccionNetlinkCliente {
		ENVIAR_PETICION_BORRARRUTA (FSMAccionEnviarPeticionBorrarRuta.getInstance()),
		ENVIAR_PETICION_NUEVARUTA (FSMAccionEnviarPeticionModificarRuta.getInstance()),
		NOTIFICAR_RESULTADO_OP_SINCRONA (FSMAccionNotificarFinOpSincrona.getInstance()),
		INICIAR_TEMPORIZADOR_OP_SINCRONA (FSMAccionIniciarTemporizadorOpSincrona.getInstance()),
		CANCELAR_TEMPORIZADOR_OP_SINCRONA (FSMAccionDesactivarTemporizadorOpSincrona.getInstance());
		
		private FSMAccion value;

		private FSMIdAccionNetlinkCliente(FSMAccion value) { this.value = value; }
		public FSMAccion getInstance() { return this.value; }
		public FSMIdAccionNetlinkCliente getInstance(FSMAccion value) { 
			for (FSMIdAccionNetlinkCliente id: values())
				if (id.getInstance() == value)
					return id;
			return null;
		}
	}
	
	public enum FSMIdEventoNetlinkCliente implements FSMIdEvento {
		ORDEN_MODIFICAR_RUTA,
		ORDEN_BORRAR_RUTA,
		RESPUESTA_CONTROL_RECIBIDA,
		RESPUESTA_NOCONTROL_RECIBIDA,
		TEMPORIZADOR_OPSINCRONA;
	}


	/**
	 * Configuración de transacciones
	 * EstadoOrigen,Evento,guarda -> EstadoFinal
	 */
	protected void configurarTransiciones()	{
		anadirTransicion(FSMIdEstadoNetlinkCliente.INICIO, 
				null, null, 
				FSMIdEstadoNetlinkCliente.PREPARADO);
		anadirTransicion(FSMIdEstadoNetlinkCliente.PREPARADO, 
				FSMIdEventoNetlinkCliente.ORDEN_BORRAR_RUTA, null, 
				FSMIdEstadoNetlinkCliente.PREPARADO);
		anadirTransicion(FSMIdEstadoNetlinkCliente.PREPARADO, 
				FSMIdEventoNetlinkCliente.ORDEN_MODIFICAR_RUTA, null, 
				FSMIdEstadoNetlinkCliente.PREPARADO);
		anadirTransicion(FSMIdEstadoNetlinkCliente.PREPARADO, 
				FSMIdEventoNetlinkCliente.RESPUESTA_CONTROL_RECIBIDA, null, 
				FSMIdEstadoNetlinkCliente.PREPARADO);
		anadirTransicion(FSMIdEstadoNetlinkCliente.PREPARADO, 
				FSMIdEventoNetlinkCliente.RESPUESTA_NOCONTROL_RECIBIDA, null, 
				FSMIdEstadoNetlinkCliente.PREPARADO);
		anadirTransicion(FSMIdEstadoNetlinkCliente.PREPARADO, 
				FSMIdEventoNetlinkCliente.TEMPORIZADOR_OPSINCRONA, null, 
				FSMIdEstadoNetlinkCliente.PREPARADO);
	}

	/** 
	 * Realiza una transición en la máquina de estados. Es decir:
	 *    - Recoge un evento de la cola de evento
	 *    - Pasa el evento al estado actual (que realiza, en su caso teniendo en cuenta la condición de guarda, las acciones establecidas)
	 *    - Pasa a un nuevo estado.
	 *    
	 * Este método puede ser bloqueante si la cola de eventos estuviese vacía, en espera de un evento.
	 */
	public void doTransicion() {
		try { 
			// 1. Recoge(espera, si no existe) un evento
			FSMEvento evento = (FSMEvento) getColaEventos().take();

			FSMIdEstado idesAnterior = getEstadoActivo().getId();
			FSMIdEvento idevAnterior = evento.getIdEvento();

			// 2. Descomponer en eventos
			switch((FSMIdEventoNetlinkCliente) evento.getIdEvento()) {
			// Ha expirado el temporizador de difusión de rutas (30 segundos)
			case ORDEN_MODIFICAR_RUTA:
				this.setEstado( ((IFSMEventoNetlinkClienteListener) this.getEstadoActivo()).procesarEventoOrdenModificarRuta(
						this.getContexto(), 
						evento));
				break;
			 // Ha expirado el temporizador de espera de difusión por actualización (triggered-update), valor aleatorio 1-5 segundos
			case ORDEN_BORRAR_RUTA:
				this.setEstado( ((IFSMEventoNetlinkClienteListener) this.getEstadoActivo()).procesarEventoOrdenBorrarRuta(
						this.getContexto(), 
						evento));
				break;
			case RESPUESTA_CONTROL_RECIBIDA:
			// Se recibe una petición NetlinkCliente desde un encaminador vecino
				this.setEstado( ((IFSMEventoNetlinkClienteListener) this.getEstadoActivo()).procesarEventoRespuestaControlRecibida(
						this.getContexto(), 
						evento));
				break;
			// La tabla de rutas ha sufrido algún cambio
			case RESPUESTA_NOCONTROL_RECIBIDA:
				this.setEstado( ((IFSMEventoNetlinkClienteListener) this.getEstadoActivo()).procesarEventoRespuestaNoControlRecibida(
						this.getContexto(), 
						evento));
				break;
			case TEMPORIZADOR_OPSINCRONA:
			// Se recibe un mensaje NetlinkCliente de respuesta
				this.setEstado( ((IFSMEventoNetlinkClienteListener) this.getEstadoActivo()).procesarEventoTemporizadorOpSincrona(
						this.getContexto(), 
						evento));
				break;
			default: 
				throw new IllegalStateException("Id de evento desconocido.");
			}
			
			System.out.println("FSMNetlinkCliente: Transición: " + idesAnterior + "(" + idevAnterior + ")->" + getEstadoActivo().getId());

		} catch (InterruptedException e1) {
			// TODO colaEventos ha generado una excepción...
			e1.printStackTrace();
		}
	}

	/** ITimerListener: patrón observador(listener) de los temporizadores */

	@Override
	public void expiredTimer(TimerEventProducer timerEventProducer) {
		FSMEvento evento;
		// Construye evento
		evento = new FSMEvento(FSMIdEventoNetlinkCliente.TEMPORIZADOR_OPSINCRONA, timerEventProducer);

		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Realiza la transición en este tiempo de hilo, cuando la máquina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();

	}


	/** ISesionCreadaListener. Creada una sesión Netlink. */
	
	@Override
	public void sesionCreada(Sesion sesion) {
		System.out.println("FSMMaquinaEstadosNetlinkCliente::sesionCreada()");
		/** ISesionCreadaListener. Creada una sesión UDP para RIP. */

		// Suscribe esta máquina al notificador
		FiltroNotificador f = (FiltroNotificador)sesion.getAceptador().getCadenaFiltros().getLast();
		f.addLecturaListener(this, sesion.getId());

		// Guarda la sesión en el contexto de la máquina de estados para que pueda enviar/recibir
		this.getContexto().put("SesionNetlinkCliente", sesion);
	}

	/** ILecturaListener. Recepción de PDU Netlink. */
	
	@Override
	public void sesionInactiva(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("FSMMaquinaEstadosNetlinkCliente::sesionInactiva()");
		
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("FSMMaquinaEstadosNetlinkCliente::sesionCerrada()");
		
	}

	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		FSMEvento evento;
		// Construye evento
		MensajeNetlink mensaje = (MensajeNetlink)lectura.getMensaje();

		// Esto se podría hacer dentro del estado (decidir si es control o no... TODO: decidir.
		if (mensaje.getMessageType().getValue() < NetlinkMessageType.RTM_BASE.getValue())
			evento = new FSMEvento(FSMIdEventoNetlinkCliente.RESPUESTA_CONTROL_RECIBIDA, mensaje);
		else 
			evento = new FSMEvento(FSMIdEventoNetlinkCliente.RESPUESTA_NOCONTROL_RECIBIDA, mensaje);
		
		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Realiza la transición en este tiempo de hilo, cuando la máquina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();

		return true;
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		// TODO Auto-generated method stub
		System.out.println("FSMMaquinaEstadosNetlinkCliente::excepcionCapturada()");
	
	}


	@Override
	public void ordenModificarRuta(Ruta ruta) {
		FSMEvento evento;
		// Construye evento
		evento = new FSMEvento(FSMIdEventoNetlinkCliente.ORDEN_MODIFICAR_RUTA, ruta);

		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Realiza la transición en este tiempo de hilo, cuando la máquina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();

	}


	@Override
	public void ordenBorrarRuta(Ruta ruta) {
		FSMEvento evento;
		// Construye evento
		evento = new FSMEvento(FSMIdEventoNetlinkCliente.ORDEN_BORRAR_RUTA, ruta);

		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Realiza la transición en este tiempo de hilo, cuando la máquina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();
	}


	@Override
	public int ordenSincronaModificarRuta(Ruta ruta, long timeout)
			throws TimeoutException {
		throw new UnsupportedOperationException("No implementada.");
	}


	@Override
	public int ordenSincronaBorrarRuta(Ruta ruta, long timeout)
			throws TimeoutException {
		throw new UnsupportedOperationException("No implementada.");
	}
	
}

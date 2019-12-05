/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.fsm.FSMEstadoInicio;
import msti.fsm.FSMEvento;
import msti.fsm.FSMMaquinaEstados;
import msti.fsm.FSMEstado.FSMIdEstado;
import msti.fsm.FSMEvento.FSMIdEvento;
import msti.rip.fsm.FSMEstadoRIP.FSMIdEstadoRIP;
import msti.rip.fsm.IFSMEventoUpdateRutaListener;
import msti.rip.fsm.ruta.FSMEstadoRIPRuta.FSMIdEstadoRIPRuta;
import msti.rip.mensaje.IMensajeRIPRuta;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMMaquinaEstadosRIPRuta extends FSMMaquinaEstados implements ITimerListener, IFSMEventoUpdateRutaListener {

	public FSMMaquinaEstadosRIPRuta(FSMContexto contexto) {
		super(contexto);	
	}

	@Override
	public void init(FSMContexto contexto) {
		// Obtiene el inicio		
		setEstado(FSMIdEstadoRIPRuta.INICIO.getInstance());
		// Realiza la primera transición desde INICIO
		setEstado(((FSMEstadoInicio) this.getEstadoActivo()).procesarEventoInicio(contexto));		
	}

	/**
	 * Configura transiciones de la máquina
	 */
	@Override
	protected void configurarTransiciones()	{
		/* Pseudo-estado Inicio */
		anadirTransicion(FSMIdEstadoRIP.INICIO, 
				null, null, 
				FSMIdEstadoRIPRuta.NOEXISTERUTA);
		/* Estado No existe ruta */
		anadirTransicion(FSMIdEstadoRIPRuta.NOEXISTERUTA, 
				FSMIdEventoRIPRuta.TEMPORIZADORRUTAEXPIRADA, null, 
				FSMIdEstadoRIPRuta.NOEXISTERUTA);
		anadirTransicion(FSMIdEstadoRIPRuta.NOEXISTERUTA, 
				FSMIdEventoRIPRuta.TEMPORIZADORRUTAELIMINAR, null, 
				FSMIdEstadoRIPRuta.NOEXISTERUTA);
		anadirTransicion(FSMIdEstadoRIPRuta.NOEXISTERUTA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("D>=15"), 
				FSMIdEstadoRIPRuta.NOEXISTERUTA);
		anadirTransicion(FSMIdEstadoRIPRuta.NOEXISTERUTA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("D<15"), 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);
		/* Estado Ruta aceptada */
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.TEMPORIZADORRUTAEXPIRADA, null, 
				FSMIdEstadoRIPRuta.RUTAINVALIDA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.TEMPORIZADORRUTAELIMINAR, null, 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("N!=Nexthop,D+1>=Dist"), 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("N!=Nexthop,D+1<Dist,D<15"), 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("N==Nexthop,D>=15"), 
				FSMIdEstadoRIPRuta.RUTAINVALIDA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("N==Nexthop,D<15,D+1==Dist"), 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAACEPTADA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("N==Nexthop,D<15,D+1!=Dist"), 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);
		/* Estado Ruta inválida */
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAINVALIDA, 
				FSMIdEventoRIPRuta.TEMPORIZADORRUTAEXPIRADA, null, 
				FSMIdEstadoRIPRuta.RUTAINVALIDA); //No debe ocurrir.
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAINVALIDA, 
				FSMIdEventoRIPRuta.TEMPORIZADORRUTAELIMINAR, null, 
				FSMIdEstadoRIPRuta.NOEXISTERUTA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAINVALIDA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("D>=15"), 
				FSMIdEstadoRIPRuta.RUTAINVALIDA);
		anadirTransicion(FSMIdEstadoRIPRuta.RUTAINVALIDA, 
				FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, new String("D<15"), 
				FSMIdEstadoRIPRuta.RUTAACEPTADA);  //TODO: Ir a Final y desuscribir/destruir maquina ??
	}


	/** 
	 * Identificador de las acciones, que funciona a su vez como factoría (en este caso de Singleton)
	 */
	public enum FSMIdAccionRIPRuta {
		TABLARUTAS_ANADIR_RUTA (FSMAccionAnadirRutaEnTablaRutas.getInstance()),
		TABLARUTAS_MODIFICAR_RUTA (FSMAccionModificarRutaEnTablaRutas.getInstance()),
		TABLARUTAS_MODIFICAR_RUTA_ESTABLECER_DISTANCIA_INFINITO (FSMAccionEstablecerDistanciaInfinitoEnTablaRutas.getInstance()),
		TABLARUTAS_BORRAR_RUTA (FSMAccionBorrarRutaEnTablaRutas.getInstance()),
		TABLAFORWARDING_ANADIR_RUTA (FSMAccionAnadirRutaEnTablaForwarding.getInstance()),
		TABLAFORWARDING_MODIFICAR_RUTA (FSMAccionModificarRutaEnTablaForwarding.getInstance()),
		TABLAFORWARDING_BORRAR_RUTA (FSMAccionBorrarRutaEnTablaForwarding.getInstance()),
		REINICIAR_TEMPORIZADOR_RUTAEXPIRADA (FSMAccionReiniciarTemporizadorRutaExpirada.getInstance()),
		REINICIAR_TEMPORIZADOR_RUTAELIMINAR (FSMAccionReiniciarTemporizadorRutaEliminar.getInstance()),
		DESACTIVAR_TEMPORIZADOR_RUTAELIMINAR (FSMAccionDesactivarTemporizadorRutaEliminar.getInstance());
		
		private FSMAccion value;

		private FSMIdAccionRIPRuta(FSMAccion value) { this.value = value; }
		public FSMAccion getInstance() { return this.value; }
		public FSMIdAccionRIPRuta getInstance(FSMAccion value) { 
			for (FSMIdAccionRIPRuta id: values())
				if (id.getInstance() == value)
					return id;
			return null;
		}
	}
	
	/** ITimerListener: patrón observador(listener) de los temporizadores */

	@Override
	public void expiredTimer(TimerEventProducer tareaTemporizada) {
		FSMEvento evento;
		// Construye evento
		if (tareaTemporizada.getId().startsWith("TemporizadorRutaExpirada")) 
			evento = new FSMEvento(FSMIdEventoRIPRuta.TEMPORIZADORRUTAEXPIRADA, tareaTemporizada);
		else if (tareaTemporizada.getId().startsWith("TemporizadorTU"))
			evento = new FSMEvento(FSMIdEventoRIPRuta.TEMPORIZADORRUTAELIMINAR, tareaTemporizada);
		else 
			throw new IllegalArgumentException("Evento desde temporizador desconocido.");

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

	/* IFSMEventoUpdateRutaListener */

	@Override
	public void updateRuta(IMensajeRIPRuta mensaje) {
		// TODO Auto-generated method stub
		FSMEvento evento;

		System.out.println("FSMRIPRuta: updateRuta()");
		
		// Construye evento
		evento = new FSMEvento(FSMIdEventoRIPRuta.ACTUALIZACIONRUTA, mensaje);

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
			switch((FSMIdEventoRIPRuta) evento.getIdEvento()) {
			// Ha expirado el temporizador de ruta(180 segundos)
			case TEMPORIZADORRUTAEXPIRADA:
				this.setEstado( ((IFSMEventoRIPRutaListener) this.getEstadoActivo()).procesarEventoExpiradoTemporizadorRutaExpirada (
						this.getContexto(), 
						evento));
				break;
			 // Ha expirado el temporizador de eliminación de ruta 
			case TEMPORIZADORRUTAELIMINAR:
				this.setEstado( ((IFSMEventoRIPRutaListener) this.getEstadoActivo()).procesarEventoExpiradoTemporizadorRutaEliminar(
						this.getContexto(), 
						evento));
				break;
				// Se recibe un Update(D.N) RIP desde un encaminador vecino
			case ACTUALIZACIONRUTA:
				this.setEstado( ((IFSMEventoRIPRutaListener) this.getEstadoActivo()).procesarEventoActualizacionRuta(
						this.getContexto(), 
						evento));
				break;
			default: 
				throw new IllegalStateException("Id de evento desconocido.");
			}
			
			System.out.println("FSMRIP: Transición: " + idesAnterior + "(" + idevAnterior + ")->" + getEstadoActivo().getId());

		} catch (InterruptedException e1) {
			// TODO colaEventos ha generado una excepción...
			e1.printStackTrace();
		}
	}

}

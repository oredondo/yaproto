/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado.FSMIdEstado;
import msti.fsm.FSMEstadoInicio;
import msti.fsm.FSMEvento;
import msti.fsm.FSMEvento.FSMIdEvento;
import msti.fsm.FSMMaquinaEstados;
import msti.io.FiltroNotificador;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.rip.ITablaRutasModificadaListener;
import msti.rip.fsm.FSMEstadoRIP.FSMIdEstadoRIP;
import msti.rip.mensaje.IMensajeRIPRuta;
import msti.rip.mensaje.MensajeRIP;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMMaquinaEstadosRIP extends FSMMaquinaEstados implements ITimerListener, ISesionCreadaListener, ILecturaListener, ITablaRutasModificadaListener {

	/**
	 *  Mapa hash para localizar los listeners suscritos a los eventos upDateRuta para cada direccion de red
	 *  
	 *  La clave es la direccion de red.
	 */
	Map<Long, List<IFSMEventoUpdateRutaListener>> mapaRutaUpdateListeners;
	List<IFSMEventoUpdateRutaListener> listaTodasRutaUpdateListeners;

	
	public FSMMaquinaEstadosRIP(FSMContexto contexto) {
		super(contexto);

		// Crea lista de listeners
		mapaRutaUpdateListeners = new ConcurrentHashMap<Long, List<IFSMEventoUpdateRutaListener>>();
		listaTodasRutaUpdateListeners = new CopyOnWriteArrayList<IFSMEventoUpdateRutaListener>();
		
		// Crea generador aleatorio 
		contexto.put("Random", new Random());
	}

	@Override
	public void init(FSMContexto contexto) {
		// Obtiene el inicio		
		setEstado(FSMIdEstadoRIP.INICIO.getInstance());
		// Realiza la primera transición desde INICIO
		setEstado(((FSMEstadoInicio)getEstadoActivo()).procesarEventoInicio(contexto));		
	}

	/**
	 * Configura transiciones de la máquina
	 */
	@Override
	protected void configurarTransiciones()	{
		/* Inicio */
		anadirTransicion(FSMIdEstadoRIP.INICIO, 
				null, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		/* Temporizador TU inactivo */
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO, 
				FSMIdEventoRIP.PETICION, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO, 
				FSMIdEventoRIP.RESPUESTA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO, 
				FSMIdEventoRIP.TABLARUTASCAMBIADA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO, 
				FSMIdEventoRIP.TEMPORIZADORDIFUSIONPERIODICA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO, 
				FSMIdEventoRIP.TEMPORIZADORTU, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		/* Temporizador TU activo */
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO, 
				FSMIdEventoRIP.PETICION, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO, 
				FSMIdEventoRIP.RESPUESTA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO, 
				FSMIdEventoRIP.TABLARUTASCAMBIADA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO, 
				FSMIdEventoRIP.TEMPORIZADORDIFUSIONPERIODICA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO, 
				FSMIdEventoRIP.TEMPORIZADORTU, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		/* Temporizador TU activo y tabla rutas modificada */
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA, 
				FSMIdEventoRIP.PETICION, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA, 
				FSMIdEventoRIP.RESPUESTA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA, 
				FSMIdEventoRIP.TABLARUTASCAMBIADA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA, 
				FSMIdEventoRIP.TEMPORIZADORDIFUSIONPERIODICA, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO);
		anadirTransicion(FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA, 
				FSMIdEventoRIP.TEMPORIZADORTU, null, 
				FSMIdEstadoRIP.TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO);
	}
	/**
	 * Añade un listener de los eventos update de ruta
	 * @param updateRutaListener listener
	 * @param direccionRed Se suscribe a los eventos de ruta que tengan esta dirección de red destino
	 */
	public void addRutaUpdateListener(IFSMEventoUpdateRutaListener updateRutaListener, InetAddress direccionRed, int prefijoRed) 
	{
		List<IFSMEventoUpdateRutaListener> list;
		int _direccionRed = msti.util.Inet4Address.toInt(direccionRed);  // como entero
		Long clave = new Long(((long)_direccionRed) << 16 ) + prefijoRed;

		list = mapaRutaUpdateListeners.get(clave); 
		if (list == null) {
			list = new CopyOnWriteArrayList<IFSMEventoUpdateRutaListener>();
			list.add(updateRutaListener);
			mapaRutaUpdateListeners.put(clave, list);
		}
		else {
			list.add(updateRutaListener);
		}
	}

	public void removeLecturaListener(IFSMEventoUpdateRutaListener updateRutaListener, InetAddress direccionRed, int prefijoRed) {
		List<IFSMEventoUpdateRutaListener> list;
		int _direccionRed = msti.util.Inet4Address.toInt(direccionRed);  // como entero
		Long clave = new Long(((long)_direccionRed) << 16 ) + prefijoRed;
		
		list = mapaRutaUpdateListeners.get(clave);
		if (list != null) {
			list.remove(updateRutaListener); 
			if (list.isEmpty())
				mapaRutaUpdateListeners.remove(clave);  // elimina entrada en el mapa si no hay listeners para esa ruta
		}
	}

	public void addRutaUpdateAllListener(IFSMEventoUpdateRutaListener updateRutaListener) {
		this.listaTodasRutaUpdateListeners.add(updateRutaListener);
	}

	public void removeRutaUpdateAllListener(IFSMEventoUpdateRutaListener updateRutaListener) {
		this.listaTodasRutaUpdateListeners.remove(updateRutaListener);
	}

	/**
	 * Notificar a los listeners de la aparición de un evento update sobre una ruta
	 * @param ruta  Ruta afectada. Contiene internamente la dirección de red y prefijo de red.
	 */
	public void notificarEventoUpdateRuta(IMensajeRIPRuta ruta) {
		int _direccionRed = msti.util.Inet4Address.toInt(ruta.getDireccionIP());  // como entero
		Long clave = new Long(((long)_direccionRed) << 16 ) + ruta.getLongitudPrefijoRed();

		if (! mapaRutaUpdateListeners.isEmpty()) 
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (IFSMEventoUpdateRutaListener updateRutaListener: mapaRutaUpdateListeners.get(clave)) {
				updateRutaListener.updateRuta(ruta);		
				System.out.println("FSMRIP: notificarEventoUpdateRuta(): Notificado evento update selectivo ruta=" + ruta.getDireccionIP() + "/" + ruta.getLongitudPrefijoRed());
			}
		if (! this.listaTodasRutaUpdateListeners.isEmpty())
			for (IFSMEventoUpdateRutaListener updateRutaListener: this.listaTodasRutaUpdateListeners) {
				updateRutaListener.updateRuta(ruta);
				System.out.println("FSMRIP: notificarEventoUpdateRuta(): Notificado evento update (suscrito a todas) ruta=" + ruta.getDireccionIP() + "/" + ruta.getLongitudPrefijoRed());
			}
		// TODO: se podría mejorar buscando en la lista selectiva para no entregar duplicados
	}

/*	** 
	 * Sobreescribe la factoría acorde a los estados RIP y sus nombres de clase asociados 
	 * 
	 * *
	protected FSMEstado getEstadoPorId(FSMIdEstado idEstado) {
		switch ((FSMIdEstadoRIP) idEstado) {
		case INICIO:
			return FSMEstadoInicio.getInstance();
		case FIN:
			return FSMEstadoFin.getInstance();
		case TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO:
			return FSMEstadoRIPTemporizadorTUActivo.getInstance();
		case TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO:
			return FSMEstadoRIPTemporizadorTUActivo.getInstance();
		case TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA:
			return FSMEstadoRIPTemporizadorTUActivoYTablaRutasModificada.getInstance();
		default: // no debería ocurrir (enum)
			return null;
		}
	}
*/

	/** 
	 * Identificador de las acciones, que funciona a su vez como factoría (en este caso de Singleton)
	 */
	public enum FSMIdAccionRIP {
		BORRAR_MARCAS_CAMBIO_EN_TABLARUTAS (FSMAccionBorrarMarcasCambioEnTablaRutas.getInstance()),
		DESACTIVAR_TEMPORIZADOR_TU (FSMAccionDesactivarTemporizadorTU.getInstance()),
		REINICIAR_TEMPORIZADOR_TU (FSMAccionReiniciarTemporizadorTU.getInstance()),
		REINICIAR_TEMPORIZADOR_DIFUSIONPERIODICA (FSMAccionReiniciarTemporizadorDifusionPeriodica.getInstance()),
		GENERAR_EVENTOS_UPDATERUTA_INDIVIDUAL (FSMAccionGenerarEventosUpdateRutaIndividual.getInstance()),
		ENVIAR_SOLICITUD_TABLACOMPLETA_A_TODOS (FSMAccionEnviarSolicitudTablaCompletaATodos.getInstance()),
		ENVIAR_TABLACOMPLETA_A_TODOS (FSMAccionEnviarTablaCompletaATodos.getInstance()),
		ENVIAR_RUTASMODIFICADAS_A_TODOS (FSMAccionEnviarRutasModificadasATodos.getInstance()),
		ENVIAR_RUTASSOLICITADAS_A_UNO (FSMAccionEnviarRutasSolicitadasAUno.getInstance());
		
		private FSMAccion value;

		private FSMIdAccionRIP(FSMAccion value) { this.value = value; }
		public FSMAccion getInstance() { return this.value; }
		public FSMIdAccionRIP getInstance(FSMAccion value) { 
			for (FSMIdAccionRIP id: values())
				if (id.getInstance() == value)
					return id;
			return null;
		}
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
			switch((FSMIdEventoRIP) evento.getIdEvento()) {
			// Ha expirado el temporizador de difusión de rutas (30 segundos)
			case TEMPORIZADORDIFUSIONPERIODICA:
				this.setEstado( ((IFSMEventoRIPListener) this.getEstadoActivo()).procesarEventoExpiradoTemporizadorDifusionPeriodica(
						this.getContexto(), 
						evento));
				break;
			 // Ha expirado el temporizador de espera de difusión por actualización (triggered-update), valor aleatorio 1-5 segundos
			case TEMPORIZADORTU:
				this.setEstado( ((IFSMEventoRIPListener) this.getEstadoActivo()).procesarEventoExpiradoTemporizadorEsperaDifusionPorActualizacion(
						this.getContexto(), 
						evento));
				break;
			case PETICION:
			// Se recibe una petición RIP desde un encaminador vecino
				this.setEstado( ((IFSMEventoRIPListener) this.getEstadoActivo()).procesarEventoPeticionDesdeVecino(
						this.getContexto(), 
						evento));
				break;
			// La tabla de rutas ha sufrido algún cambio
			case TABLARUTASCAMBIADA:
				this.setEstado( ((IFSMEventoRIPListener) this.getEstadoActivo()).procesarEventoTablaRutasModificada(
						this.getContexto(), 
						evento));
				break;
			case RESPUESTA:
			// Se recibe un mensaje RIP de respuesta
				this.setEstado( ((IFSMEventoRIPListener) this.getEstadoActivo()).procesarEventoRespuestaDesdeVecino(
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


	/** ITimerListener: patrón observador(listener) de los temporizadores */

	@Override
	public void expiredTimer(TimerEventProducer timer) {
		FSMEvento evento;

		System.out.println("FSMRIP::expiredTimer():" + timer.getId() + " " + new Date());

		// Construye evento
		if (timer.getId() == "TemporizadorDifusionPeriodica") 
			evento = new FSMEvento(FSMIdEventoRIP.TEMPORIZADORDIFUSIONPERIODICA, timer);
		else if (timer.getId() == "TemporizadorTU")
			evento = new FSMEvento(FSMIdEventoRIP.TEMPORIZADORTU, timer);
		else 
			throw new IllegalArgumentException("Evento desde temporizador desconocido.");

		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Realiza la transición en este tiempo de hilo, si la máquina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();
		
	}


	/** ISesionCreadaListener. Creada una sesión UDP para RIP. */
	
	@Override
	public void sesionCreada(Sesion sesion) {
		// TODO Auto-generated method stub
		// Instanciar una máquina de estados para atender y enganchar el ILecturaListener Debería estar fuera
		System.out.println("FSMRIP::sesionCreada()");
		// Suscribe esta máquina al notificador
		FiltroNotificador f = (FiltroNotificador)sesion.getAceptador().getCadenaFiltros().getLast();
		f.addLecturaListener(this, sesion.getId());

		// Guarda la sesión en el contexto de la máquina de estados para que pueda enviar/recibir
		this.getContexto().put("SesionRIP", sesion);
	}

	/** ILecturaListener. Recepción de PDU RIP. */
	
	@Override
	public void sesionInactiva(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("FSMRIP::sesionInactiva()");
		
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("FSMRIP::sesionCerrada()");
		
	}

	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		// TODO Auto-generated method stub
		FSMEvento evento;
		// Construye evento
		System.out.println("FSMRIP: mensajeRecibido() tipo=" + ((MensajeRIP) lectura.getMensaje()).getTipo());

		// Esto se podría hacer dentro del estado (decidir si es petición o respuesta... TODO: decidir.
		switch (((MensajeRIP) lectura.getMensaje()).getTipo()) {
		case RIPPeticion: 
			evento = new FSMEvento(FSMIdEventoRIP.PETICION, lectura);
			break;
		case RIPRespuesta: 
			evento = new FSMEvento(FSMIdEventoRIP.RESPUESTA, lectura);
			break;
		default:
			throw new IllegalArgumentException("Evento mensaje RIP desconocido.");
		}
		
		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Realiza la transición en este tiempo de hilo, si la máquina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();

		return true;
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		// TODO Auto-generated method stub
		
	}

	/* ITablaRutasModificadaListener: cambio en la tabla de rutas */

	@Override
	public void tablaRutasModificada(int idTablaRutas) {
		FSMEvento evento;

		// Construye evento
		evento = new FSMEvento(FSMIdEventoRIP.TABLARUTASCAMBIADA, new Integer(idTablaRutas));

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
		
}

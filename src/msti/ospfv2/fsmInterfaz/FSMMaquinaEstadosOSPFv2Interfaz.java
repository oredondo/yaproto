/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

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
import msti.ospfv2.ConfiguracionOSPFv2;
import msti.ospfv2.ITablaRutasModificadaListener;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMIdEventoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino;
//import msti.ospfv2.mensaje.IMensajeRIPRuta;
import msti.ospfv2.mensaje.MensajeOSPFv2;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMMaquinaEstadosOSPFv2Interfaz extends FSMMaquinaEstados implements ITimerListener {

	/**
	 *  Mapa hash para localizar los listeners suscritos a los eventos upDateRuta para cada direccion de red
	 *  
	 *  La clave es la direccion de red.
	 */
	

	
	public FSMMaquinaEstadosOSPFv2Interfaz(FSMContexto contexto) {
		super(contexto);

		
	}

	@Override
	public void init(FSMContexto contexto) {
		// Obtiene el inicio		
		setEstado(FSMIdEstadoOSPFv2Interfaz.INICIO.getInstance());
		// Realiza la primera transición desde INICIO
		setEstado(((FSMEstadoInicio)getEstadoActivo()).procesarEventoInicio(contexto));
		
		//Realizar transicion a estado Down
		FSMEvento evento = new FSMEvento(FSMIdEventoOSPFv2Interfaz.UNLOOPIND, contexto);
		encolarEvento(evento);
		
		//Realizar transicion a estado Waiting (Interfaz ya levantada llegados a este punto
		FSMEvento evento2 = new FSMEvento(FSMIdEventoOSPFv2Interfaz.INTERFACEUP, contexto);
		encolarEvento(evento2);
	}

	/**
	 * Configura transiciones de la máquina
	 */
	@Override
	protected void configurarTransiciones()	{
		/* Inicio */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.INICIO, 
				null, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		/* Loopback */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.LOOPBACK, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		/* Down */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, new String("PointToPoint"), 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, new String("DrOther"), 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, new String("Waiting"), 
				FSMIdEstadoOSPFv2Interfaz.WAITING);		
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DOWN, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		/* PointToPoint */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, null, 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, null, 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, null, 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT);
		/* Waiting */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.WAITING);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, null, 
				FSMIdEstadoOSPFv2Interfaz.WAITING);		
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, new String("Backup"), 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, new String("Dr"), 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, new String("DrOther"), 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);		
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, new String("Backup"), 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, new String("Dr"), 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, new String("DrOther"), 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);		
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, null, 
				FSMIdEstadoOSPFv2Interfaz.WAITING);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.WAITING, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.WAITING);
		/* Backup */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, null, 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, null, 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("Backup"), 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("Dr"), 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("DrOther"), 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.BACKUP, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		/* Dr */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, null, 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, null, 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("Backup"), 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("Dr"), 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("DrOther"), 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DR, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.DR);
		/* Backup */
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.LOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.LOOPBACK);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.UNLOOPIND, null, 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEDOWN, null, 
				FSMIdEstadoOSPFv2Interfaz.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.INTERFACEUP, null, 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.WAITTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, null, 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("Backup"), 
				FSMIdEstadoOSPFv2Interfaz.BACKUP);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("Dr"), 
				FSMIdEstadoOSPFv2Interfaz.DR);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, new String("DrOther"), 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		anadirTransicion(FSMIdEstadoOSPFv2Interfaz.DROTHER, 
				FSMIdEventoOSPFv2Interfaz.HELLOTIMER, null, 
				FSMIdEstadoOSPFv2Interfaz.DROTHER);
		
	}


	/** 
	 * Identificador de las acciones, que funciona a su vez como factoría (en este caso de Singleton)
	 */
	public enum FSMIdAccionOSPFv2Interfaz {
		
		RESETEAR_VARIABLES_INTERFAZ (FSMAccionResetearVariablesInterfaz.getInstance()),
		DESTRUIR_CONEXION_VECINOS (FSMAccionDestruirConexionVecinos.getInstance()),
		INICIAR_TEMPORIZADOR_HELLO (FSMAccionIniciarTemporizadorHello.getInstance()),
		INICIAR_TEMPORIZADOR_WAIT (FSMAccionIniciarTemporizadorWait.getInstance()),
		CALCULAR_BDR_Y_DR (FSMAccionCalcularBdrYDr.getInstance()),
		DIFUNDIR_HELLO_PACKETS (FSMAccionDifundirHelloPackets.getInstance()),
		GENERAR_LSA_ROUTER_LINKS (FSMAccionGenerarLSARouterLink.getInstance()),
		GENERAR_LSA_NETWORK_LINKS (FSMAccionGenerarLSANetworkLink.getInstance()),
		DIFUNDIR_LSA_NUEVO_POR_INTERFACES (FSMAccionDifundirLSANuevoPorInterfaces.getInstance());
		
		
		private FSMAccion value;

		private FSMIdAccionOSPFv2Interfaz(FSMAccion value) { this.value = value; }
		public FSMAccion getInstance() { return this.value; }
		public FSMIdAccionOSPFv2Interfaz getInstance(FSMAccion value) { 
			for (FSMIdAccionOSPFv2Interfaz id: values())
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
			switch((FSMIdEventoOSPFv2Interfaz) evento.getIdEvento()) {
			// Ha expirado el temporizador de difusión de rutas (30 segundos)
			case LOOPIND:
				this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoLoopInd(
						this.getContexto(), 
						evento));
				break;
			 // Ha expirado el temporizador de espera de difusión por actualización (triggered-update), valor aleatorio 1-5 segundos
			case UNLOOPIND:
				this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoUnloopInd(
						this.getContexto(), 
						evento));
				break;
			case INTERFACEDOWN:
			// Se recibe una petición RIP desde un encaminador vecino
				this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoInterfaceDown(
						this.getContexto(), 
						evento));
				break;
			// La tabla de rutas ha sufrido algún cambio
			case INTERFACEUP:
				this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoInterfaceUp(
						this.getContexto(), 
						evento));
				break;
			case WAITTIMER:
			// Se recibe un mensaje RIP de respuesta
				this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoWaitTimer(
						this.getContexto(), 
						evento));
				break;				
			case BACKUPSEEN:
				// Se recibe un mensaje RIP de respuesta
					this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoBackupSeen(
							this.getContexto(), 
							evento));
					break;					
			case NEIGHBORCHANGE:
				// Se recibe un mensaje RIP de respuesta
					this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoNeighborChange(
							this.getContexto(), 
							evento));
					break;
			case HELLOTIMER:
				// Se recibe un mensaje RIP de respuesta
					this.setEstado( ((IFSMEventoOSPFv2InterfazListener) this.getEstadoActivo()).procesarEventoHelloTimer(
							this.getContexto(), 
							evento));
					break;
			default: 
				throw new IllegalStateException("Id de evento desconocido.");
			}
			
			System.out.println("FSMOSPFv2Interfaz: Transición: " + idesAnterior + "(" + idevAnterior + ")->" + getEstadoActivo().getId());

		} catch (InterruptedException e1) {
			// TODO colaEventos ha generado una excepción...
			e1.printStackTrace();
		}
	}


	/** ITimerListener: patrón observador(listener) de los temporizadores */

	@Override
	public void expiredTimer(TimerEventProducer timer) {
		FSMEvento evento;

		System.out.println("FSMOSPFv2Interfaz::expiredTimer():" + timer.getId() + " " + new Date());

		// Construye evento
		if (timer.getId() == "TemporizadorWait"){
			evento = new FSMEvento(FSMIdEventoOSPFv2Interfaz.WAITTIMER, timer);
			encolarEvento(evento);
		}else if (timer.getId() == "TemporizadorHello"){
			evento = new FSMEvento(FSMIdEventoOSPFv2Interfaz.HELLOTIMER, timer);
			encolarEvento(evento);
		}else if (timer.getId() == "TemporizadorRouterLink"){
			ConfiguracionOSPFv2.getInstance().setTemporizadorRouterLink(null);
			if(ConfiguracionOSPFv2.getInstance().routerLinkPospuesto){			
				//LLama a la accion de generacion de routerLinks
				FSMIdAccionOSPFv2Interfaz.GENERAR_LSA_ROUTER_LINKS.getInstance().execute(this.getContexto(), timer);
			}
		
		}else if (timer.getId() == "TemporizadorRouterLinkRefreshTime"){
			ConfiguracionOSPFv2.getInstance().setTemporizadorRouterLinkRefreshTime(null);
			FSMIdAccionOSPFv2Interfaz.GENERAR_LSA_ROUTER_LINKS.getInstance().execute(this.getContexto(), timer);
		
		}else if (timer.getId() == "TemporizadorNetworkLink"){
			ConfiguracionOSPFv2.getInstance().setTemporizadorNetworkLink(null);
			if(ConfiguracionOSPFv2.getInstance().networkLinkPospuesto){			
				//LLama a la accion de generacion de networkLinks
				FSMIdAccionOSPFv2Interfaz.GENERAR_LSA_NETWORK_LINKS.getInstance().execute(this.getContexto(), timer);
			}
		
		}else if (timer.getId() == "TemporizadorNetworkLinkRefreshTime"){
			ConfiguracionOSPFv2.getInstance().setTemporizadorNetworkLinkRefreshTime(null);
			FSMIdAccionOSPFv2Interfaz.GENERAR_LSA_NETWORK_LINKS.getInstance().execute(this.getContexto(), timer);
			
		}else{ 
			throw new IllegalArgumentException("Evento desde temporizador desconocido.");
		}
		
	}




	/*//ITablaRutasModificadaListener: cambio en la tabla de rutas 

	@Override
	public void tablaRutasModificada(int idTablaRutas) {
		FSMEvento evento;

		// Construye evento
		evento = new FSMEvento(FSMIdEventoOSPFv2Interfaz.TABLARUTASCAMBIADA, new Integer(idTablaRutas));

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
	*/
	
	public void encolarEvento(FSMEvento evento){
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

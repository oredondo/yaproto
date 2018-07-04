/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEstadoInicio;
import msti.fsm.FSMEvento;
import msti.fsm.FSMMaquinaEstados;
import msti.fsm.FSMEstado.FSMIdEstado;
import msti.fsm.FSMEvento.FSMIdEvento;
import msti.io.Escritura;
import msti.io.FiltroNotificador;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.ospfv2.Dijkstra;
import msti.ospfv2.fsmInterfaz.FSMContextoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMIdEventoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino;
import msti.ospfv2.mensaje.*;
import msti.rip.fsm.FSMIdEventoRIP;
import msti.rip.fsm.IFSMEventoUpdateRutaListener;
import msti.rip.mensaje.MensajeRIPRespuesta;
//import msti.rip.fsm.IFSMEventoUpdateRutaListener;
//import msti.rip.mensaje.IMensajeRIPRuta;
//import msti.rip.fsm.FSMEstadoRIP.FSMIdEstadoRIP;
//import msti.rip.fsm.IFSMEventoUpdateRutaListener;
//import msti.rip.fsm.ruta.FSMEstadoRIPRuta.FSMIdEstadoRIPRuta;
//import msti.rip.mensaje.IMensajeRIPRuta;
import msti.util.ITimerListener;
import msti.util.Inet4Address;
import msti.util.TimerEventProducer;

public class FSMMaquinaEstadosOSPFv2Vecino extends FSMMaquinaEstados implements ITimerListener, ILecturaListener {


	
	
	public FSMMaquinaEstadosOSPFv2Vecino(FSMContexto contexto) {
		super(contexto);
		
		
	}

	@Override
	public void init(FSMContexto contexto) {
		// Obtiene el inicio		
		setEstado(FSMIdEstadoOSPFv2Vecino.INICIO.getInstance());
		// Realiza la primera transiciÃ³n desde INICIO
		setEstado(((FSMEstadoInicio) this.getEstadoActivo()).procesarEventoInicio(contexto));		
	}

	/**
	 * Configura transiciones de la mÃ¡quina
	 */
	@Override
	protected void configurarTransiciones()	{
		/* Pseudo-estado Inicio */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INICIO, 
				null, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		/* Estado Down */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.ADJOK, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.DOWN, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		
		/* Estado Attempt */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.ADJOK, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.ATTEMPT, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.ATTEMPT);
		
		/* Estado Init */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, new String("Crear adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, new String("No crear adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);		
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.ADJOK, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.INIT, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		
		/* Estado Twoway */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Crear adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("No crear adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.TWOWAY, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		
		/* Estado Exstart */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Conservar adjacencia"),
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Destruir adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXSTART, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		
		/* Estado Exchange */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, new String("LinkStateRequestList empty"), 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, new String("LinkStateRequestList not empty"), 
				FSMIdEstadoOSPFv2Vecino.LOADING);	
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Conservar adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Destruir adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.EXCHANGE, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.EXCHANGE);
		
		/* Estado Loading */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, new String("ExchangeDone"), 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, new String("LoadingDone"), 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Conservar adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Destruir adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.LOADING, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.LOADING);
		
		/* Estado Full */
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.KILLNBR, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.LLDOWN, null, 
				FSMIdEstadoOSPFv2Vecino.DOWN);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.START, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.HELLORECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, null, 
				FSMIdEstadoOSPFv2Vecino.INIT);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.BADLSREQ, null, 
				FSMIdEstadoOSPFv2Vecino.EXSTART);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.LOADINGDONE, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Conservar adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.FULL);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.ADJOK, new String("Destruir adjacencia"), 
				FSMIdEstadoOSPFv2Vecino.TWOWAY);
		anadirTransicion(FSMIdEstadoOSPFv2Vecino.FULL, 
				FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, null, 
				FSMIdEstadoOSPFv2Vecino.FULL);
		
	
	}


	/** 
	 * Identificador de las acciones, que funciona a su vez como factorÃ­a (en este caso de Singleton)
	 */
	public enum FSMIdAccionOSPFv2Vecino {
		LIMPIAR_LISTAS (FSMAccionLimpiarListas.getInstance()),
		DESACTIVAR_TEMPORIZADOR_INACTIVITY (FSMAccionDesactivarTemporizadorInactivity.getInstance()),
		ENVIAR_HELLO_A_VECINO (FSMAccionEnviarHelloAVecino.getInstance()),
		INICIAR_TEMPORIZADOR_INACTIVITY (FSMAccionIniciarTemporizadorInactivity.getInstance()),
		INCREMENTAR_DDSEQUENCENUMBER_DEL_VECINO (FSMAccionIncrementarDDSequenceNumberDelVecino.getInstance()),
		COMO_MASTER_ENVIAR_DATABASEDESCRIPTIONPACKETS_VACIOS (FSMAccionComoMasterEnviarDatabaseDescriptionPacketsVacios.getInstance()),
		ENVIAR_DATABASESUMMARYLIST_A_VECINO (FSMAccionEnviarDatabaseSummaryListAVecino.getInstance()),
		ENVIAR_SIGUIENTE_DATABASE_DESCRIPTION_PACKET (FSMAccionEnviarSiguienteDatabaseDescriptionPacket.getInstance()),
		REENVIAR_ULTIMO_DATABASE_DESCRIPTION_PACKET (FSMAccionReenviarUltimoDatabaseDescriptionPacket.getInstance()),
		ENVIAR_LINKSTATEREQUEST_A_VECINO (FSMAccionEnviarLinkStateRequestAVecino.getInstance()),
		DETERMINAR_SI_SE_FORMA_ADJACENCIA (FSMAccionDeterminarSiSeFormaAdjacencia.getInstance()),
		DETERMINAR_SI_SE_CONSERVA_ADJACENCIA  (FSMAccionDeterminarSiSeConservaAdjacencia.getInstance()),
		INICIAR_TEMPORIZADOR_ENVIO_DDP_CON_IMMS (FSMAccionIniciarTemporizadorEnvioDDPconIMMS.getInstance()),
		DESACTIVAR_TEMPORIZADOR_ENVIO_DDP_CON_IMMS (FSMAccionDesactivarTemporizadorEnvioDDPconIMMS.getInstance()),		
		INICIAR_TEMPORIZADOR_LINK_STATE_REQUEST_LIST (FSMAccionIniciarTemporizadorLinkStateRequestList.getInstance()),
		ENVIAR_LINKSTATEUPDATE_A_VECINO (FSMAccionEnviarLinkStateUpdateAVecino.getInstance()),
		ENVIAR_LINKSTATEACKNOWLEDGMENT_A_VECINO (FSMAccionEnviarLinkStateAcknowledgmentAVecino.getInstance()),
		DIFUNDIR_LSA_POR_INTERFACES (FSMAccionDifundirLSAPorInterfaces.getInstance());
		
		
		private FSMAccion value;

		private FSMIdAccionOSPFv2Vecino(FSMAccion value) { this.value = value; }
		public FSMAccion getInstance() { return this.value; }
		public FSMIdAccionOSPFv2Vecino getInstance(FSMAccion value) { 
			for (FSMIdAccionOSPFv2Vecino id: values())
				if (id.getInstance() == value)
					return id;
			return null;
		}
	}
	
	/** ITimerListener: patrÃ³n observador(listener) de los temporizadores */

	@Override
	public void expiredTimer(TimerEventProducer timer) {
		FSMEvento evento;
		
		System.out.println("FSMOSPFv2Vecino::expiredTimer():" + timer.getId() + " " + new Date());
		
		// Construye evento
		if (timer.getId() == "TemporizadorInactivity"){ 
			evento = new FSMEvento(FSMIdEventoOSPFv2Vecino.INACTIVITYTIMER, timer);
			this.encolarEvento(evento);
		}else if (timer.getId() == "TemporizadorDDPconIMMS"){
			evento = new FSMEvento(FSMIdEventoOSPFv2Vecino.DDPCONIMMSTIMER, timer);
			this.encolarEvento(evento);
		}else if (timer.getId() == "TemporizadorLinkStateRequestList"){
			evento = new FSMEvento(FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, timer);
			this.encolarEvento(evento);
		}else{ 
			throw new IllegalArgumentException("Evento desde temporizador desconocido.");
		}		
		
	}


	
	/** 
	 * Realiza una transiciÃ³n en la mÃ¡quina de estados. Es decir:
	 *    - Recoge un evento de la cola de evento
	 *    - Pasa el evento al estado actual (que realiza, en su caso teniendo en cuenta la condiciÃ³n de guarda, las acciones establecidas)
	 *    - Pasa a un nuevo estado.
	 *    
	 * Este mÃ©todo puede ser bloqueante si la cola de eventos estuviese vacÃ­a, en espera de un evento.
	 */
	public void doTransicion() {
		try { 
			// 1. Recoge(espera, si no existe) un evento
			FSMEvento evento = (FSMEvento) getColaEventos().take();

			FSMIdEstado idesAnterior = getEstadoActivo().getId();
			FSMIdEvento idevAnterior = evento.getIdEvento();
			// 2. Descomponer en eventos
			switch((FSMIdEventoOSPFv2Vecino) evento.getIdEvento()) {
			// KILLNBR
			case KILLNBR:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoKillNbr (
						this.getContexto(), 
						evento));
				break;
			// INACTIVITYTIMER 
			case INACTIVITYTIMER:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoInactivityTimer(
						this.getContexto(), 
						evento));
				break;
			// LLDOWN
			case LLDOWN:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoLLDown(
						this.getContexto(), 
						evento));
				break;
			//START	
			case START:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoStart (
						this.getContexto(), 
						evento));
				break;	
			//HELLORECEIVED	
			case HELLORECEIVED:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoHelloReceived (
						this.getContexto(), 
						evento));
				break;
			//TWOWAYRECEIVED	
			case TWOWAYRECEIVED:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEvento2WayReceived (
						this.getContexto(), 
						evento));
				break;
			//ONEWAYRECEIVED	
			case ONEWAYRECEIVED:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEvento1WayReceived (
						this.getContexto(), 
						evento));
				break;
			//SEQNUMBERMISMATCH	
			case SEQNUMBERMISMATCH:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoSeqNumberMismatch (
						this.getContexto(), 
						evento));
				break;
			//BADLSREQ	
			case BADLSREQ:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoBadLSReq (
						this.getContexto(), 
						evento));
				break;
			//NEGOTIATIONDONE	
			case NEGOTIATIONDONE:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoNegotiationDone (
						this.getContexto(), 
						evento));
				break;
			//EXCHANGEDONE	
			case EXCHANGEDONE:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoExchangeDone (
						this.getContexto(), 
						evento));
				break;
			//LOADINGDONE	
			case LOADINGDONE:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoLoadingDone (
						this.getContexto(), 
						evento));
				break;
			//ADJOK	
			case ADJOK:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoAdjOK (
						this.getContexto(), 
						evento));
				break;		
			//DDPCONIMMSTIMER	
			case DDPCONIMMSTIMER:
				this.setEstado( ((IFSMEventoOSPFv2VecinoListener) this.getEstadoActivo()).procesarEventoDDPconIMMSTimer (
						this.getContexto(), 
						evento));
				break;
				
			default: 
				throw new IllegalStateException("Id de evento desconocido.");
			}
			
			System.out.println("FSMOSPFv2Vecino: TransiciÃ³n: " + idesAnterior + "(" + idevAnterior + ")->" + getEstadoActivo().getId());

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	
	


	/** ILecturaListener. RecepciÃ³n de PDU RIP. */
	
	@Override
	public void sesionInactiva(Sesion sesion) {
		System.out.println("FSMOSPFv2Interfaz::sesionInactiva()");
		
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		System.out.println("FSMOSPFv2Interfaz::sesionCerrada()");
		
	}
	
	public void encolarEvento(FSMEvento evento){
		// Encola el evento
		try {
			this.getColaEventos().put(evento);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Realiza la transiciÃ³n en este tiempo de hilo, cuando la mÃ¡quina no es hilo aparte
		if (! this.esHilo())
			this.doTransicion();	
	}
	
		
	
	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		System.out.println("FSMOSPFv2Interfaz: mensajeRecibido() tipo=" + ((MensajeOSPFv2) lectura.getMensaje()).getTipo());

		FSMContextoOSPFv2Vecino contexto =(FSMContextoOSPFv2Vecino) this.getContexto();
		FSMEstadoOSPFv2Vecino estadoActual;
		
		switch (((MensajeOSPFv2) lectura.getMensaje()).getTipo()) {
		case OSPFHello:
			
			boolean procesar=true;
			//Si la red es PTP se comprueba que los valores de HelloInterval y Router DeadInterval coincida			
			//si no coincide se descarta el paquete, ya que son valores de configuración de la red.		
			if(((MensajeOSPFv2Hello) lectura.getMensaje()).getHelloInterval() != contexto.getContextoInterfaz().getHelloInterval() &&
				((MensajeOSPFv2Hello) lectura.getMensaje()).getRouterDeadInterval() != contexto.getContextoInterfaz().getRouterDeadInterval()){
					procesar=false;
			}
			//Si la red es multicast tambien se comprueba el vaor de NetworkMask
			if(!contexto.getContextoInterfaz().isPointToPoint()){
				if(((MensajeOSPFv2Hello) lectura.getMensaje()).getNetworkMask() != contexto.getContextoInterfaz().getIpInterfaceMask()){
					procesar=false;
				}
			}
			//Comprobar E-bit de las opciones del mensaje, debe ser igual que "ExternalRoutingCapability"
			boolean bitE;
			if((((MensajeOSPFv2Hello) lectura.getMensaje()).getOptions() & 2)==2){
				//System.out.println("bit E: si");
				bitE=true;
			}else{
				//System.out.println("bit E: no");
				bitE=false;
			}
			if(bitE!=contexto.getContextoInterfaz().isExternalRoutingCapability()){
				procesar=false;
			}

			//Comprobamos para procesar el evento
			if (procesar){
				//Introducir ID del vecino en el contexto (o IP si es red ptp)
				if(contexto.getContextoInterfaz().isPointToPoint()){
					contexto.setNeighborIPAddress(Inet4Address.toInt(((InetSocketAddress)lectura.getDireccionOrigen()).getAddress()));
				}else{
					contexto.setNeighborID(((MensajeOSPFv2Hello) lectura.getMensaje()).getRouterID());
				}
				
				//Seguimos los cinco pasos de recepción de Hello
				//Paso 1. HelloReceived executed
				FSMEvento eventoHelloReceived = new FSMEvento(FSMIdEventoOSPFv2Vecino.HELLORECEIVED, lectura);
				try {
					this.getColaEventos().put(eventoHelloReceived);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				if (! this.esHilo())
					this.doTransicion();
				//Paso 2. Examinar lista de Vecinos, si aparecemos en la lista TwoWayReceived Executed,
				//sino, OneWayReceived Executed y paramos el proceso.
				List<Integer> vecinosHello = ((MensajeOSPFv2Hello) lectura.getMensaje()).getNeighbors();
				if(!vecinosHello.contains(contexto.getContextoInterfaz().getRouterID())){
					FSMEvento eventoOneWayReceived = new FSMEvento(FSMIdEventoOSPFv2Vecino.ONEWAYRECEIVED, lectura);
					try {
						this.getColaEventos().put(eventoOneWayReceived);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
					if (! this.esHilo())
						this.doTransicion();
					//Fin del proceso
				}else{
					FSMEvento eventoTwoWayReceived = new FSMEvento(FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, lectura);
					try {
						this.getColaEventos().put(eventoTwoWayReceived);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
					if (! this.esHilo())
						this.doTransicion();
					//Paso 3. Actualizar RouterPriority, si ha cambiado respecto al anterior generar NeighborChange Scheduled en Interfaz
					byte rtrPri = ((MensajeOSPFv2Hello) lectura.getMensaje()).getRtrPri();
					if(rtrPri!=contexto.getNeighborPriority()){
						FSMEvento eventoNeighborChange = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, lectura);
						((FSMMaquinaEstadosOSPFv2Interfaz)contexto.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange);
						
						//se actualiza el nuevo valor
						contexto.setNeighborPriority(rtrPri);
					}
					
					//Paso 4. Se examina el campo de DR
					int  designatedRouter = ((MensajeOSPFv2Hello) lectura.getMensaje()).getDesignatedRouter();
					int  backupDesignatedRouter = ((MensajeOSPFv2Hello) lectura.getMensaje()).getBackupDesignatedRouter();
					FSMEstado estadoActualInterfaz = (FSMEstado) contexto.getContextoInterfaz().getMaquinaEstados().getEstadoActivo();
					//si el vecino se declara DR, tiene BDR=0 y la interfaz está en Waiting, generar BackupSeen Scheduled en Interfaz
					if(designatedRouter==contexto.getNeighborIPAddress() &&
						backupDesignatedRouter==0 &&
						estadoActualInterfaz.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.WAITING)){
							FSMEvento eventoBackupSeen = new FSMEvento(FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, lectura);
							((FSMMaquinaEstadosOSPFv2Interfaz)contexto.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoBackupSeen);
							
					}
					//además, si el vecino se declaraba DR y antes no, o viceversa, generar NeighborChange Scheduled en Interfaz
					if((designatedRouter==contexto.getNeighborIPAddress() && contexto.getNeighborDesignatedRouter()!=contexto.getNeighborIPAddress()) ||
						(designatedRouter!=contexto.getNeighborIPAddress() && contexto.getNeighborDesignatedRouter()==contexto.getNeighborIPAddress())){
							FSMEvento eventoNeighborChange2 = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, lectura);
							((FSMMaquinaEstadosOSPFv2Interfaz)contexto.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange2);
							
					}
					//se actualiza el nuevo valor del DR
					contexto.setNeighborDesignatedRouter(designatedRouter);
					
					//Paso 5.Se examina el campo BDR
					//si el vecino se declara BDR y la interfaz está en Waiting, generar BackupSeen Scheduled en Interfaz
					if(backupDesignatedRouter==contexto.getNeighborIPAddress() &&
						estadoActualInterfaz.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.WAITING)){
							FSMEvento eventoBackupSeen2 = new FSMEvento(FSMIdEventoOSPFv2Interfaz.BACKUPSEEN, lectura);
							((FSMMaquinaEstadosOSPFv2Interfaz)contexto.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoBackupSeen2);
							
					}
					//además, si el vecino se declaraba BDR y antes no, o viceversa, generar NeighborChange Scheduled en Interfaz
					if((backupDesignatedRouter==contexto.getNeighborIPAddress() && contexto.getNeighborBackupDesignatedRouter()!=contexto.getNeighborIPAddress()) ||
						(backupDesignatedRouter!=contexto.getNeighborIPAddress() && contexto.getNeighborBackupDesignatedRouter()==contexto.getNeighborIPAddress())){
							FSMEvento eventoNeighborChange2 = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, lectura);
							((FSMMaquinaEstadosOSPFv2Interfaz)contexto.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange2);
					}
					//se actualiza el nuevo valor del BDR
					contexto.setNeighborBackupDesignatedRouter(backupDesignatedRouter);
				}
				
				//Redes Non-Broadcast, Enviar mensajeHello de respuesta a Vecino si este no es DR o BDR
				
			}//fin procesar
			

			break;
		case OSPFDatabaseDescription: 
			
			//Dependiendo del Estado actual del Vecino se trata de una manera o de otra
			//si está en Down o Attempt, se ignora (En estado Inicio y estado Fin también)
			estadoActual = (FSMEstadoOSPFv2Vecino) this.getEstadoActivo();
			switch ((FSMIdEstadoOSPFv2Vecino) estadoActual.getId()) {
			case INICIO: 				
				break;
			case FIN: 	
				break;
			case DOWN: 				
				break;
			case ATTEMPT: 				
				break;
			case INIT: 
				//Se genera evento TwoWayReceived Executed				
				FSMEvento eventoTwoWayReceived = new FSMEvento(FSMIdEventoOSPFv2Vecino.TWOWAYRECEIVED, lectura);
				try {
					this.getColaEventos().put(eventoTwoWayReceived);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				if (! this.esHilo())
					this.doTransicion();
				//Esto causa que se cambie al estado TWOWAY o EXSTART
				//Si es el caso de EXSTART, debe procesarse el Mensaje como tal en este estado.	
				//para ello, las acciones de EXSTART para este mensaje están extraidas en un método propio
				//Tras el doTransicion comprobamos el nuevo estado
				if(((FSMIdEstadoOSPFv2Vecino) this.getEstadoActivo().getId()).equals(FSMIdEstadoOSPFv2Vecino.EXSTART)){
					this.procesarDatabaseDescriptionPacketDesdeExstart(lectura);
				}

				break;
			case TWOWAY: 
				//Se ignora, ya que el objetivo de estos paquetes es crear adjacencias.
				//Es normal recibirlo en este estado cuando hay un desacuerdo momentáneo sobre la identidad del DR	
				break;
			case EXSTART: 
				//extraido en este método para no duplicar código
				this.procesarDatabaseDescriptionPacketDesdeExstart(lectura);
				
				break;
			case EXCHANGE: 
				//extraido en este método para no duplicar código
				this.procesarDatabaseDescriptionPacketDesdeExchange(lectura);
				
				break;
			case LOADING: 
				//extraido en este método para no duplicar código
				this.procesarDatabaseDescriptionPacketDesdeLoadingOFull(lectura);
				
				break;
			case FULL: 
				//extraido en este método para no duplicar código
				this.procesarDatabaseDescriptionPacketDesdeLoadingOFull(lectura);				
				
				break;				
			default:
				throw new IllegalArgumentException("Estado desconocido.");
			}
			
			
	
			
			
			break;
		case OSPFLinkStateRequest: 
			
			//Sólo deben aceptarse si el estado actual es Exchange, Loading o Full, en el resto de estados el paquete se ignora
			estadoActual = (FSMEstadoOSPFv2Vecino) this.getEstadoActivo();
			if(estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXCHANGE) ||
			estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.LOADING) ||
			estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.FULL)){
					
				//Cada LSA indicado en el mensaje debe localizarse en el "router Database"
				//y enviarlos al vecino en "LinkStateUpdatePackets"
				List<Integer> lSTypes = ((MensajeOSPFv2LinkStateRequest) lectura.getMensaje()).getLSTypes();
				List<Integer> lSIDs = ((MensajeOSPFv2LinkStateRequest) lectura.getMensaje()).getLSIDs();
				List<Integer> advertisingRouters = ((MensajeOSPFv2LinkStateRequest) lectura.getMensaje()).getAdvertisingRouters();
				//compruebo que las listas recibidas son coherentes y tienen el mismo numero de elementos
				if(lSTypes.size()==lSIDs.size() && lSIDs.size()==advertisingRouters.size()){
					
					Iterator<Integer> iterLSTypes = lSTypes.listIterator();
					Iterator<Integer> iterLSIDss = lSIDs.listIterator();
					Iterator<Integer> iterAdvertisingRouters = advertisingRouters.listIterator();
					int lsType;
					int lsID;
					int advertisingRouter;
					Collection<IMensajeOSPFv2LSA> databaseList=contexto.getContextoInterfaz().getConfiguracion().database.values();
					List<IMensajeOSPFv2LSA> lsaAEnviar= new ArrayList();
					//Buscar en el Database global un LSA con estos tres campos y genero LinkStateUpdate
					//Si algún LSA no se encuentra, algo ha ido mal con el proceso de DatabaseExchange, asíque se genera evento BadLSReq
					boolean encontrados=false;
				    while (iterLSTypes.hasNext()) {
				    	encontrados=false;
				    	lsType = iterLSTypes.next();
						lsID = iterLSIDss.next();
						advertisingRouter = iterAdvertisingRouters.next();
						for(IMensajeOSPFv2LSA lsa : databaseList){
							if(lsa.getHeader().getLSType().getCodigo()==(byte)lsType &&
							lsa.getHeader().getLinkStateID()==lsID &&
							lsa.getHeader().getAdvertisingRouter()==advertisingRouter ){
								encontrados=true;
								lsaAEnviar.add(lsa);								
							}
						}
						
				    }
				    //Compruebo si todos han sido encontrados, en ese caro genero el LinkStateUpdatePacket
				    //si alguno no ha sido encontrado no envío nada y genero evento BadLSReq
				    if(encontrados){
				    	FSMIdAccionOSPFv2Vecino.ENVIAR_LINKSTATEUPDATE_A_VECINO.getInstance().execute(this.getContexto(), lsaAEnviar);
						
				    }else{
				    	FSMEvento eventoBadLSReq = new FSMEvento(FSMIdEventoOSPFv2Vecino.BADLSREQ, lectura);
						try {
							this.getColaEventos().put(eventoBadLSReq);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}				
						if (! this.esHilo())
							this.doTransicion();				    	
				    }
				}else{
					System.out.println("Error al comprobar coherencia de las listas de LinkStateRequestPacket");
				}								
			
			}
			break;
		case OSPFLinkStateUpdate: 
			//analizar los lsa, guardarlos en el database global y quitarlos de la LinkStateRequestList
			List<IMensajeOSPFv2LSA> listaLSA = ((MensajeOSPFv2LinkStateUpdate) lectura.getMensaje()).getLSAs();
			boolean error = false;
			for(IMensajeOSPFv2LSA lsa: listaLSA){
				//1. Validar checksum
				if (lsa.getIsLSChecksumOK()){
					//2. comprobar consistencia del LS type
					if(((lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.RouterLinks)) ||
						(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks)) ||
						(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.SumaryLinkIPNetwork)) ||
						(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.SumaryLinkASBR)) ||
						(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.ASExternalLink)))){
						//3. Si es AsExternalLink y el area es stub, descartar el mensaje
						
						//4. Si tiene LSAge = MaxAge y no hay instancia del mensaje en el database, entonces
						long claveLSA = contexto.getContextoInterfaz().getConfiguracion().claveLSA(lsa.getHeader().getLSType(), lsa.getHeader().getLinkStateID());
						if(lsa.getHeader().getLSAge()==contexto.getContextoInterfaz().getConfiguracion().MAX_AGE &&
							!contexto.getContextoInterfaz().getConfiguracion().database.containsKey(claveLSA)){
							//Enviar Acknowledge del mensaje
							List<IMensajeOSPFv2LinkStateAdvertisementHeader> lsaHAEnviar = new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
							lsaHAEnviar.add(lsa.getHeader());
							FSMIdAccionOSPFv2Vecino.ENVIAR_LINKSTATEACKNOWLEDGMENT_A_VECINO.getInstance().execute(this.getContexto(), lsaHAEnviar);
							//Quitar el lsa de la LinkStateRequestList
							contexto.getLinkStateRequestList().remove(claveLSA);
							//Si estamos en Exchange o Loading, meter el lsa en el database, sino se descarta
							estadoActual = (FSMEstadoOSPFv2Vecino) this.getEstadoActivo();
							if(estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXCHANGE) ||
								estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.LOADING)){
								contexto.getContextoInterfaz().getConfiguracion().agregarLSA(lsa);
							}		
						}else{
							//5. Si no hay copia del lsa en el database, o la que hay es más antigua, entonces
							IMensajeOSPFv2LSA lsaViejo = contexto.getContextoInterfaz().getConfiguracion().database.get(claveLSA);
							IMensajeOSPFv2LinkStateAdvertisementHeader lsaViejoHeader;
							if(lsaViejo==null){
								lsaViejoHeader = null;
							}else{
								lsaViejoHeader = lsaViejo.getHeader();
							}			
																
							if(determinarQueLSAEsMasReciente(contexto, lsa.getHeader(), lsaViejoHeader)==1){
								//5.a. Si hay una instancia en el database,  y tiene menos de MinLSinterval, se descarta el paquete
								if(!lsaViejo.equals(null)){
									if(lsaViejo.getHeader().getLSAge()>=contexto.getContextoInterfaz().getConfiguracion().MIN_LS_INTERVAL){
										//5.b. Sino,
										FSMIdAccionOSPFv2Vecino.DIFUNDIR_LSA_POR_INTERFACES.getInstance().execute(this.getContexto(), lsa);
										
										//5.c Eliminar el lsa de la LinkStateRetransmissionList de todos los vecinos
										for(FSMMaquinaEstadosOSPFv2Vecino fsmVecino: contexto.getContextoInterfaz().getListOfNeighbouringRouters().values()){
											((FSMContextoOSPFv2Vecino) fsmVecino.getContexto()).getLinkStateRetransmissionList().remove(claveLSA);
										}
										//5.d Instalar el lsa en el database y recalcular tabla de rutas
										contexto.getContextoInterfaz().getConfiguracion().agregarLSA(lsa);
										Dijkstra.recalcularTabla(contexto.getContextoInterfaz().getConfiguracion(), contexto.getContextoInterfaz().getConfiguracion().tablaRutas);
										
										//5.e Posiblemente enviar ack de recepcion del lsa
										List<IMensajeOSPFv2LinkStateAdvertisementHeader> lsaHAEnviar = new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
										lsaHAEnviar.add(lsa.getHeader());
										FSMIdAccionOSPFv2Vecino.ENVIAR_LINKSTATEACKNOWLEDGMENT_A_VECINO.getInstance().execute(this.getContexto(), lsaHAEnviar);
										
										//5.f Si es un lsa originado por el propio router, entonces
										if(lsaIsSelfGenerated(contexto,lsa)){
											//incrementar LSSequenceNumber en 1 más el del paquete
											contexto.getContextoInterfaz().getConfiguracion().setLsSequenceNumber(lsa.getHeader().getLSSequenceNumber()+1);
											//generar nueva instancia del LSA siempre que no
											//sea NetworkLinkLSA y ya no seamos DR de esa red
											if(!(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks) &&
													!contexto.getContextoInterfaz().getMaquinaEstados().getEstadoActivo().getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.DR))){
												//TODO:generar nueva instancia del LSA propio
											}
											
											
										}	
									}
								}

							//6. Sino, si hay instancia del lsa en la LinkStateRequestList, ha ocurrido un error en el proceso, asíque
							}else if(contexto.getLinkStateRequestList().containsKey(claveLSA)){								
								//se genera evento BadLSReq y se para de procesar el paquete
								error=true;
								FSMEvento eventoBadLSReq = new FSMEvento(FSMIdEventoOSPFv2Vecino.BADLSREQ, lectura);
								this.encolarEvento(eventoBadLSReq);
								break;
								
							//7. Si la istancia del database es igual a la del lsa, entonces	
							}else if(determinarQueLSAEsMasReciente(contexto, lsa.getHeader(), lsaViejoHeader)==0){
								//7.a. Si está en la LinkStateRetransmission list, se trata como un ack y se quita de la lista
								if(contexto.getLinkStateRetransmissionList().containsKey(claveLSA)){
									contexto.getLinkStateRetransmissionList().remove(claveLSA);
									//TODO: y se anota este evento
								}

								//7.b. Enviar acknowledgment directo
								List<IMensajeOSPFv2LinkStateAdvertisementHeader> lsaHAEnviar = new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
								lsaHAEnviar.add(lsa.getHeader());
								FSMIdAccionOSPFv2Vecino.ENVIAR_LINKSTATEACKNOWLEDGMENT_A_VECINO.getInstance().execute(this.getContexto(), lsaHAEnviar);						
								
							//8. Sino, la instancia del database es más reciente, descartar paquete y seguir leyendo
							}else{
								//Indicar evento unusual al administrador de la red
							}							
						}		
						
					}	
				}												
			}			
			
			if(!error){
				//Generar ExchangeDone
				FSMEvento eventoExchangeDone = new FSMEvento(FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, lectura);
				this.encolarEvento(eventoExchangeDone);
			}
			break;
		case OSPFLinkStateAcknowledgment: 
			//Sólo deben aceptarse si el estado actual es Exchange, Loading o Full, en el resto de estados el paquete se ignora
			estadoActual = (FSMEstadoOSPFv2Vecino) this.getEstadoActivo();
			if(estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXCHANGE) ||
			estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.LOADING) ||
			estadoActual.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.FULL)){
				//por cada header del paquete:
				List<IMensajeOSPFv2LinkStateAdvertisementHeader> listaHeaders= ((MensajeOSPFv2LinkStateAcknowledgment)lectura.getMensaje()).getLSAHeaders();
				for(IMensajeOSPFv2LinkStateAdvertisementHeader lsaH : listaHeaders){
					long claveLSAH = contexto.getContextoInterfaz().getConfiguracion().claveLSA(lsaH.getLSType(), lsaH.getLinkStateID());
					//si tiene la misma instancia en la LinkStateRetransmissionlist, quitarlo de la lista
					if(contexto.getLinkStateRetransmissionList().containsKey(claveLSAH)){
						//si la instancia es diferente, hacer log del acknowledgment questionable
						if(contexto.getLinkStateRetransmissionList().get(claveLSAH).getHeader().getLSSequenceNumber() ==
								lsaH.getLSSequenceNumber()){
							contexto.getLinkStateRetransmissionList().remove(claveLSAH);
						}else{
							System.out.println("FSMVecino, MensajeOSPFLinkStateAcknowledgment recivido, header con LinkStateID: " + 
									lsaH.getLinkStateID() + ", tiene instancia diferente a la requerida.");
							System.out.println("LSSequenceNumber: "+ lsaH.getLSSequenceNumber() + ", cuando debería ser: " + 
									contexto.getLinkStateRetransmissionList().get(claveLSAH).getHeader().getLSSequenceNumber() +".");
						}						
					}				
				}								
			}			
			break;
		default:
			throw new IllegalArgumentException("Evento mensaje OSPFv2 desconocido.");
		}

		return true;
	}	


	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		
	}
	
	
	private void procesarDatabaseDescriptionPacketDesdeExstart(Lectura lectura) {
		
		//Si se da uno de estos dos casos, se debe generar evento NegotiationDone Executed
		//esto provocará transicion al estado Exchange, y el mensaje debe procesarse como tal en este estado.
		boolean generarNegotiationDone = false;
		MensajeOSPFv2DatabaseDescription mensaje = (MensajeOSPFv2DatabaseDescription) lectura.getMensaje();
		FSMContextoOSPFv2Vecino contextoV =(FSMContextoOSPFv2Vecino) this.getContexto();
		byte imms=mensaje.getIMMS();
		
		//caso 1. Somos esclavo, si I=1, M=1, MS=1, los contenidos del paquete están vacios y el routerID es mayor que el nuestro
		if((imms & 1)==1 &&
			(imms & 2)==2 &&
			(imms & 4)==4 &&
			mensaje.getLSAHeaders().size()==0 &&
			mensaje.getRouterID()>contextoV.getContextoInterfaz().getRouterID()){
				contextoV.setMaster(false);
				contextoV.setDdSequenceNumber(mensaje.getDDSequenceNumber());
				generarNegotiationDone=true;
		}				
		//caso 2. Somos Máster, si I=0, MS=0, el DDSequenceNumber del paquete es igual al del router y el routerID es menor que el nuestro
		if((imms & 1)==0 &&
			(imms & 4)==0 &&
			mensaje.getDDSequenceNumber()==contextoV.getDdSequenceNumber() &&
			mensaje.getRouterID()<contextoV.getContextoInterfaz().getRouterID()){
				generarNegotiationDone=true;
		}
		
		
		if(generarNegotiationDone){
			//se genera el evento NegotiationDone Executed
			FSMEvento eventoNegotiationDone = new FSMEvento(FSMIdEventoOSPFv2Vecino.NEGOTIATIONDONE, lectura);
			try {
				this.getColaEventos().put(eventoNegotiationDone);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}				
			if (! this.esHilo())
				this.doTransicion();
			//se guarda el campo Options de este paquete en el contexto
			contextoV.setNeighborOptions(mensaje.getOptions());
			//se vuelve a procesar el paquete desde el estado Exchange
			procesarDatabaseDescriptionPacketDesdeExchange(lectura);
		} //si no, el paquete se ignora
		
	}
	
	
	private void procesarDatabaseDescriptionPacketDesdeExchange(Lectura lectura) {
				
		MensajeOSPFv2DatabaseDescription mensaje = (MensajeOSPFv2DatabaseDescription) lectura.getMensaje();
		FSMContextoOSPFv2Vecino contextoV =(FSMContextoOSPFv2Vecino) this.getContexto();
		byte imms=mensaje.getIMMS();
		boolean aceptarMensaje=false;
		//Comprobar consistencia con el bit MS (vecino máster y router esclavo o viceversa),
		//sino generar SeqNumberMismatch Executed y se acabar el proceso
		if(!(((imms & 1)==1 && !contextoV.isMaster()) ||
			((imms & 1)==0 && contextoV.isMaster()))){
			FSMEvento eventoSeqNumberMismatch = new FSMEvento(FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, lectura);
			try {
				this.getColaEventos().put(eventoSeqNumberMismatch);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}				
			if (! this.esHilo())
				this.doTransicion();
		}else{
			//Seguir con el resto de pasos			
			//Paso 1. Si el bit I=1, generar evento SeqNumberMismatch Executed y parar el proceso
			if((imms & 4)==4){
				FSMEvento eventoSeqNumberMismatch = new FSMEvento(FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, lectura);
				try {
					this.getColaEventos().put(eventoSeqNumberMismatch);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				if (! this.esHilo())
					this.doTransicion();
			}else{
				//Paso 2. Comprobar consistencia de Neighbor options, (sólo nos interesa los dos últimos bits)
				//sino generar SeqNumberMismatch Executed y se acabar el proceso
				if((mensaje.getOptions()&3)!=(contextoV.getNeighborOptions()&3)){
					FSMEvento eventoSeqNumberMismatch = new FSMEvento(FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, lectura);
					try {
						this.getColaEventos().put(eventoSeqNumberMismatch);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
					if (! this.esHilo())
						this.doTransicion();
				}else{
					//Paso 3. Si el router es máster y DDSequenceNumber del paquete es igual al del router, se acepta el mensaje
					//(es el siguiente de la secuancia)
					if(contextoV.isMaster() && mensaje.getDDSequenceNumber()==contextoV.getDdSequenceNumber()){
						//aceptar mensaje
						aceptarMensaje=true;
						
					//Paso 4. Si el router es máster y DDsequenceNumber del paquete es uno menor al del router, se descarta el mensaje
					//(duplicado)	
					}else if(contextoV.isMaster() && (mensaje.getDDSequenceNumber()-1)==contextoV.getDdSequenceNumber()){
						//descartar mensaje
						aceptarMensaje=false;
						
					//Paso 5. Si el router es slave y DDSequenceNumber del paquete es uno más que el del router, se acepta el mensaje
					//(es el siguiente de la secuancia)	
					}else if(!contextoV.isMaster() && (mensaje.getDDSequenceNumber()+1)==contextoV.getDdSequenceNumber()){
						//aceptar mensaje
						aceptarMensaje=true;
						
					//Paso 6. Si el router es slave y DDSequenceNumber del paquete es igual al del router,
					//Debemos reenviar último DatabaseDescriptionPacket que hayamos enviado al vecino.
					//(duplicado)
					}else if(!contextoV.isMaster() && mensaje.getDDSequenceNumber()==contextoV.getDdSequenceNumber()){
						//reenviar último DatabaseDescriptionPacket enviado
						FSMIdAccionOSPFv2Vecino.REENVIAR_ULTIMO_DATABASE_DESCRIPTION_PACKET.getInstance().execute(this.getContexto(), lectura);
						aceptarMensaje=false;
						
						
					//Paso 7. Si no se cumple alguno de los anteriores, generar SeqNumberMismatch Executed y se acabar el proceso
					}else{
						aceptarMensaje=false;
						FSMEvento eventoSeqNumberMismatch = new FSMEvento(FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, lectura);
						try {
							this.getColaEventos().put(eventoSeqNumberMismatch);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}				
						if (! this.esHilo())
							this.doTransicion();		
					}
					
					
					//aceptarMensaje
					if(aceptarMensaje){
						boolean errorTipoLS=false;
						//Por cada LSA se comprueba la validez del LSType,
						//si es ASExternalAdvertisment y el vecino está asociado a area stub o desconocido
						//generar SeqNumberMismatch Executed y se acabar el proceso
						List<IMensajeOSPFv2LinkStateAdvertisementHeader> listaHeaders = mensaje.getLSAHeaders();
						for(IMensajeOSPFv2LinkStateAdvertisementHeader lsaH: listaHeaders){
							if(!((lsaH.getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.RouterLinks)) ||
								(lsaH.getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks)) ||
								(lsaH.getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.SumaryLinkIPNetwork)) ||
								(lsaH.getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.SumaryLinkASBR)) ||
								(lsaH.getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.ASExternalLink)))){
								errorTipoLS=true;
							}	
						}
						if(errorTipoLS){						
							FSMEvento eventoSeqNumberMismatch = new FSMEvento(FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, lectura);
							this.encolarEvento(eventoSeqNumberMismatch);						
						}else{
							//Sino, se busca los lsa en el database por si tienen una instancia
							//si no la tienen o la que tiene es menos reciente, se añade a la LinkStateRequestList
							boolean lsaConocida;
							Map<Long,IMensajeOSPFv2LSA> databaseMap=contextoV.getContextoInterfaz().getConfiguracion().database;
							for(IMensajeOSPFv2LinkStateAdvertisementHeader lsaH: listaHeaders){
								lsaConocida=false;
								long claveLSAH = contextoV.getContextoInterfaz().getConfiguracion().claveLSA(lsaH.getLSType(), lsaH.getLinkStateID());
								if(databaseMap.containsKey(claveLSAH)){
									if(lsaH.getLSSequenceNumber()<=databaseMap.get(claveLSAH).getHeader().getLSSequenceNumber()){
										lsaConocida=true;
									}
								}
								
								if(!lsaConocida){
									contextoV.getLinkStateRequestList().put(claveLSAH, lsaH);
									//Enviar LinkStateRequestPacket?? fuera del bucle for?
								}									
							}							
							
							//Además si somos Máster o Slave, se realiza lo siguiente
							if(contextoV.isMaster()){
								//Master:
								//Incrementar DDSequenceNumber
								FSMIdAccionOSPFv2Vecino.INCREMENTAR_DDSEQUENCENUMBER_DEL_VECINO.getInstance().execute(contextoV, lectura);
								//Si se ha terminado de enviar la sequencia de DatabaseDescriptionPackets y este paquete tiene el bit M=0,
								//(se habrá terminado de enviar la secuencia si el ultimoDDPenviado tenia M=0
								//se genera el evento ExchangeDone
								//sino, debe enviarse un nuevo DatabaseDescriptionPacket
								if(((contextoV.getUltimoDDPEnviado().getIMMS() & 2)==0) && ((imms & 2)==0)){
									contextoV.setAllDatabaseDescriptionPacketSent(true);
									FSMEvento eventoExchangeDone = new FSMEvento(FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, lectura);
									this.encolarEvento(eventoExchangeDone);
								}else{
									FSMIdAccionOSPFv2Vecino.ENVIAR_SIGUIENTE_DATABASE_DESCRIPTION_PACKET.getInstance().execute(contextoV, lectura);
								}
					
							}else{
								//Slave:
								//Pone el DDSequence number con el que trae el paquete
								contextoV.setDdSequenceNumber(mensaje.getDDSequenceNumber());
								//Enviar DatabaseDescriptionPacket
								FSMIdAccionOSPFv2Vecino.ENVIAR_SIGUIENTE_DATABASE_DESCRIPTION_PACKET.getInstance().execute(contextoV, lectura);
								//si el paquete recibido tiene M=0 y el paquete que acabamos de enviar tambien tiene M=0,
								//se genera el evento ExchangeDone
								byte immsUltimoDDEnviado =contextoV.getUltimoDDPEnviado().getIMMS();
								if(((imms & 2)==0) && ((immsUltimoDDEnviado & 2)==0)){
									contextoV.setAllDatabaseDescriptionPacketSent(true);
									FSMEvento eventoExchangeDone = new FSMEvento(FSMIdEventoOSPFv2Vecino.EXCHANGEDONE, lectura);
									this.encolarEvento(eventoExchangeDone);
								}
	
								
							}
							
							
						}
					}
						
				}
			}
		}
	}
	
	
	private void procesarDatabaseDescriptionPacketDesdeLoadingOFull(Lectura lectura) {
		MensajeOSPFv2DatabaseDescription mensaje = (MensajeOSPFv2DatabaseDescription) lectura.getMensaje();
		FSMContextoOSPFv2Vecino contextoV =(FSMContextoOSPFv2Vecino) this.getContexto();
		byte imms=mensaje.getIMMS();
		boolean generarSeqNumberMismatch=false;
		//Se comprueban si el bit I=1, si las opciones no son iguales, o si no se trata de paqute duplicado,
		//en cualquiera de estos casos, generar evento SeqNumberMismatch Executed
		if((imms & 4)==4)
			generarSeqNumberMismatch=true;

		if((mensaje.getOptions()&3)!=(contextoV.getNeighborOptions()&3))
			generarSeqNumberMismatch=true;
		
		//El mensaje es duplicado si somos máster y DDsequenceNumber del paquete es uno menor al del router
		//o si somos slave y DDSequenceNumber del paquete es igual al del router
		//si se trata de un duplicado y somos slave, además debemos reenviar el último DatabaseDescriptionPacket
		if(contextoV.isMaster()){
			if(!((mensaje.getDDSequenceNumber()-1)==contextoV.getDdSequenceNumber()))
				generarSeqNumberMismatch=true;	
		}else{
			if(!(mensaje.getDDSequenceNumber()==contextoV.getDdSequenceNumber())){
				generarSeqNumberMismatch=true;
			}else{
				//reenviar último DatabaseDescriptionPacket enviado
				FSMIdAccionOSPFv2Vecino.REENVIAR_ULTIMO_DATABASE_DESCRIPTION_PACKET.getInstance().execute(this.getContexto(), lectura);				
			}				
		}
			
		if(generarSeqNumberMismatch){
			FSMEvento eventoSeqNumberMismatch = new FSMEvento(FSMIdEventoOSPFv2Vecino.SEQNUMBERMISMATCH, lectura);
			try {
				this.getColaEventos().put(eventoSeqNumberMismatch);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}				
			if (! this.esHilo())
				this.doTransicion();
		}
	}
	
	/**Determina que instancia de LSA se considera más reciente
	 * @param contexto
	 * @param lsa1Header a comparar
	 * @param lsa2Header a comparar
	 * @return 1 si es más reciente el lsa1, 2 si es más reciente el lsa2, o 0 si se consideran iguales  
	*/
	public int determinarQueLSAEsMasReciente(FSMContextoOSPFv2Vecino contexto, IMensajeOSPFv2LinkStateAdvertisementHeader lsaH1, IMensajeOSPFv2LinkStateAdvertisementHeader lsaH2){
		//si alguno es null, se considera más reciente el otro, si ambos lo son se consideran instancias iguales
		if(lsaH1.equals(null) && !lsaH2.equals(null)){
			return 2;
		}else if(!lsaH1.equals(null) && lsaH2.equals(null)){
			return 1;
		}else if(!lsaH1.equals(null) && !lsaH2.equals(null)){
			return 0;
		}else{
			//Ninguno es null
			//El que tenga el LSSequenceNumber más reciente (valor más alto)
			if(lsaH1.getLSSequenceNumber()>lsaH2.getLSSequenceNumber()){
				return 1;
			}else if(lsaH1.getLSSequenceNumber()<lsaH2.getLSSequenceNumber()){
				return 2;
			}else{
				//Si los LSSequencenumber son iguales, el que tenga LSChecksum mayor	
				if(lsaH1.getLSChecksum()>lsaH2.getLSChecksum()){
					return 1;
				}else if(lsaH1.getLSChecksum()<lsaH2.getLSChecksum()){
					return 2;
				}else{
					//Si el LSChecksum tambien es igual, se compara el LSAge
					//el que tenga LSAge menor, a no ser que la diferencia no supere MaxAgeDif, entonces se consideran iguales
					if(lsaH1.getLSAge()<lsaH2.getLSAge() &&
							lsaH2.getLSAge()-lsaH1.getLSAge() > contexto.getContextoInterfaz().getConfiguracion().MAX_AGE_DIF){
						return 1;
					}else if(lsaH1.getLSAge()>lsaH2.getLSAge() &&
							lsaH1.getLSAge()-lsaH2.getLSAge() > contexto.getContextoInterfaz().getConfiguracion().MAX_AGE_DIF){
						return 2;
					}else{
						//se Consideran iguales
						return 0;
					}
				}	
			}
		}
	}
	
	
	/**Determina si el LSA fue generado por nosotros
	 * @param contexto
	 * @param lsa a comparar
	 * @return true fue generado por este router 
	*/
	private boolean lsaIsSelfGenerated(FSMContextoOSPFv2Vecino contexto, IMensajeOSPFv2LSA lsa){
		//lo consideramos nuestro si el Advertising router es nuestro RouterID
		if(lsa.getHeader().getAdvertisingRouter()==contexto.getContextoInterfaz().getRouterID()){
			return true;
		}else{
			//tambien lo consideramos nuestro si es del tipo Network Links y su LinkStateID tiene una de nuestras direcciones IP
			if(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks)){
				int linkStateID = lsa.getHeader().getLinkStateID();
				//TODO: comparar este linkStateID con nuestras direcciones IP
			}
			return false;
		}	
	}
	
	
	
}

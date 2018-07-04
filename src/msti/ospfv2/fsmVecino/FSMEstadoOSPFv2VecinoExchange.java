/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.util.List;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.ospfv2.fsmInterfaz.FSMIdEventoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino.FSMIdAccionOSPFv2Vecino;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMEstadoOSPFv2VecinoExchange extends FSMEstadoOSPFv2Vecino {
	static {
		_instancia = new FSMEstadoOSPFv2VecinoExchange(FSMIdEstadoOSPFv2Vecino.EXCHANGE);
	}

	protected FSMEstadoOSPFv2VecinoExchange(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	@Override
	public FSMEstado procesarEventoKillNbr(
			FSMContexto contexto, FSMEvento evento) {

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoV.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoV);
		
		/** Obtiene y ejecuta acciones */
		//Notificar UpdateVecino
		FSMEvento eventoNeighborChange = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, contextoV.getNeighborID());
		((FSMMaquinaEstadosOSPFv2Interfaz)contextoV.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange);
		
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contextoV, evento.getArgumento());
		//Desactivar InactivityTimer
		FSMIdAccionOSPFv2Vecino.DESACTIVAR_TEMPORIZADOR_INACTIVITY.getInstance().execute(contextoV, evento.getArgumento());	
				
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoV);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoInactivityTimer(
			FSMContexto contexto, FSMEvento evento) {

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;

		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoV.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoV);
		
		/** Obtiene y ejecuta acciones */
		//Notificar UpdateVecino
		FSMEvento eventoNeighborChange = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, contextoV.getNeighborID());
		((FSMMaquinaEstadosOSPFv2Interfaz)contextoV.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange);
		
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contextoV, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoV);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoLLDown(FSMContexto contexto,
			FSMEvento evento) {

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoV.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoV);
		
		/** Obtiene y ejecuta acciones */
		//Notificar UpdateVecino
		FSMEvento eventoNeighborChange = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, contextoV.getNeighborID());
		((FSMMaquinaEstadosOSPFv2Interfaz)contextoV.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange);
		
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contextoV, evento.getArgumento());
		//Desactivar InactivityTimer
		FSMIdAccionOSPFv2Vecino.DESACTIVAR_TEMPORIZADOR_INACTIVITY.getInstance().execute(contextoV, evento.getArgumento());			
				
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoV);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoStart(
			FSMContexto contexto, FSMEvento evento) {
		
	
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Acciones */		
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoHelloReceived(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Acciones */
		//Iniciar InactivityTimer
		FSMIdAccionOSPFv2Vecino.INICIAR_TEMPORIZADOR_INACTIVITY.getInstance().execute(contexto, evento.getArgumento());
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEvento2WayReceived(
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
	public FSMEstado procesarEvento1WayReceived(
			FSMContexto contexto, FSMEvento evento) {

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoV.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoV);

		/** Obtiene y ejecuta acciones */
		//Notificar UpdateVecino
		FSMEvento eventoNeighborChange = new FSMEvento(FSMIdEventoOSPFv2Interfaz.NEIGHBORCHANGE, contextoV.getNeighborID());
		((FSMMaquinaEstadosOSPFv2Interfaz)contextoV.getContextoInterfaz().getMaquinaEstados()).encolarEvento(eventoNeighborChange);
		
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contextoV, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoV);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
	
	@Override
	public FSMEstado procesarEventoSeqNumberMismatch(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Obtiene y ejecuta acciones */
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contexto, evento.getArgumento());
		
		//Incrementar DDSequenceNumber
		FSMIdAccionOSPFv2Vecino.INCREMENTAR_DDSEQUENCENUMBER_DEL_VECINO.getInstance().execute(contexto, evento.getArgumento());
		//Declararse Master y enviar DatabaseDescriptionPackets con I M y MS a 1
		FSMIdAccionOSPFv2Vecino.COMO_MASTER_ENVIAR_DATABASEDESCRIPTIONPACKETS_VACIOS.getInstance().execute(contexto, evento.getArgumento());
		//esto hacerlo hasta que se produzca la siguiente transicion cada RxmtInterval (no lo pone pero ser� como el Init-2WayReceived
		
		//iniciar TemporizadorEnvioDDPconIMMS
		FSMIdAccionOSPFv2Vecino.INICIAR_TEMPORIZADOR_ENVIO_DDP_CON_IMMS.getInstance().execute(contexto, evento.getArgumento());

		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
	
	@Override
	public FSMEstado procesarEventoBadLSReq(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** Obtiene y ejecuta acciones */
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contexto, evento.getArgumento());
		
		//Incrementar DDSequenceNumber
		FSMIdAccionOSPFv2Vecino.INCREMENTAR_DDSEQUENCENUMBER_DEL_VECINO.getInstance().execute(contexto, evento.getArgumento());
		//Declararse Master y enviar DatabaseDescriptionPackets con I M y MS a 1
		FSMIdAccionOSPFv2Vecino.COMO_MASTER_ENVIAR_DATABASEDESCRIPTIONPACKETS_VACIOS.getInstance().execute(contexto, evento.getArgumento());
		//esto hacerlo hasta que se produzca la siguiente transicion cada RxmtInterval (no lo pone pero ser� como el Init-2WayReceived
		
		//iniciar TemporizadorEnvioDDPconIMMS
		FSMIdAccionOSPFv2Vecino.INICIAR_TEMPORIZADOR_ENVIO_DDP_CON_IMMS.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
	
	@Override
	public FSMEstado procesarEventoNegotiationDone(
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
	public FSMEstado procesarEventoExchangeDone(
			FSMContexto contexto, FSMEvento evento) {

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		/** Evalúa guarda */
		String guarda;		
		
		//Comprobar si la LinkStateRequestList esta vac�a
		if(contextoV.getLinkStateRequestList().size() == 0 ){
			guarda="LinkStateRequestList empty";
		}else{
			guarda="LinkStateRequestList not empty";
		}

			
		System.out.println("FSMEstadoOSPFv2VecinoExchange:eventoExchangeDone(): guarda= " + guarda);
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoV.getMaquinaEstados().getEstadoSiguiente(this, evento, guarda);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoV);

		/** Acciones */
		if (guarda.equals("LinkStateRequestList not empty")) {
			//Empezar a enviar (o continuar) LinkStateRequest al vecino (listados en la LinkStateRequestList)
			FSMIdAccionOSPFv2Vecino.ENVIAR_LINKSTATEREQUEST_A_VECINO.getInstance().execute(contextoV, evento.getArgumento());
			//Iniciar el temporizador para volver a enviar LinkStateRequest si este vence
			FSMIdAccionOSPFv2Vecino.INICIAR_TEMPORIZADOR_LINK_STATE_REQUEST_LIST.getInstance().execute(contextoV, evento.getArgumento());
			
		}
		// else if (guarda.equals("LinkStateRequestList empty"):  no hay acciones que ejecutar 

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoV);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
	
	@Override
	public FSMEstado procesarEventoLoadingDone(
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
	public FSMEstado procesarEventoAdjOK(
			FSMContexto contexto, FSMEvento evento) {

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		/** Acciones y evaluar guarda*/
		//Determiar si debe conserva la adjacencia con el vecino
		FSMIdAccionOSPFv2Vecino.DETERMINAR_SI_SE_CONSERVA_ADJACENCIA.getInstance().execute(contextoV, evento.getArgumento());
		//esta acci�n modifica el atributo EstadoAdjacencia del contexto
		
		String guarda;		
		guarda=contextoV.getEstadoAdjacencia();
		
		System.out.println("FSMEstadoOSPFv2VecinoExchange:eventoAdjOK(): guarda= " + guarda);

		if (guarda.equals("Destruir adjacencia")) {
			//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
			FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contextoV, evento.getArgumento());
			
		}
		// else if (guarda.equals("Conservar adjacencia"):  no hay acciones que ejecutar 
		
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoV.getMaquinaEstados().getEstadoSiguiente(this, evento, guarda);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoV);
 

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoV);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}
	
	@Override
	public FSMEstado procesarEventoDDPconIMMSTimer(
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

}
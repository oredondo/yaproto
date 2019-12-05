/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.io.Lectura;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino.FSMIdAccionOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.mensaje.IMensajeOSPFv2;
//import msti.rip.fsm.ruta.FSMMaquinaEstadosRIPRuta.FSMIdAccionRIPRuta;
//import msti.rip.mensaje.MensajeRIPRuta;
import msti.ospfv2.mensaje.MensajeOSPFv2;

public class FSMEstadoOSPFv2VecinoInit extends FSMEstadoOSPFv2Vecino {
	static {
		_instancia = new FSMEstadoOSPFv2VecinoInit(FSMIdEstadoOSPFv2Vecino.INIT);
	}

	protected FSMEstadoOSPFv2VecinoInit(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	@Override
	public FSMEstado procesarEventoKillNbr(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contexto, evento.getArgumento());
		//Desactivar InactivityTimer
		FSMIdAccionOSPFv2Vecino.DESACTIVAR_TEMPORIZADOR_INACTIVITY.getInstance().execute(contexto, evento.getArgumento());
				
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoInactivityTimer(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contexto, evento.getArgumento());

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoLLDown(FSMContexto contexto,
			FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		//Limpia de LSA las listas LinkStateRetransmissionList, DatabaseSummaryList y LinkStateRequestList
		FSMIdAccionOSPFv2Vecino.LIMPIAR_LISTAS.getInstance().execute(contexto, evento.getArgumento());
		//Desactivar InactivityTimer
		FSMIdAccionOSPFv2Vecino.DESACTIVAR_TEMPORIZADOR_INACTIVITY.getInstance().execute(contexto, evento.getArgumento());		
				
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

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
		
		/** No acción */
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

		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		/** Acciones y evaluar guarda*/
		//Determinar si se forma o no la adjacencia
		FSMIdAccionOSPFv2Vecino.DETERMINAR_SI_SE_FORMA_ADJACENCIA.getInstance().execute(contextoV, evento.getArgumento());
		//esta acci�n modifica el atributo EstadoAdjacencia del contexto
		
		String guarda;		
		guarda=contextoV.getEstadoAdjacencia();
		
		System.out.println("FSMEstadoOSPFv2VecinoInit:evento2WayReceived(): guarda= " + guarda);
		
		
		if (guarda.equals("Crear adjacencia")) {
			//Incrementar DDSequenceNumber al vecino o darle valor �nico si es el primero (hora y fecha actual)
			FSMIdAccionOSPFv2Vecino.INCREMENTAR_DDSEQUENCENUMBER_DEL_VECINO.getInstance().execute(contextoV, evento.getArgumento());
			
			//Declararse Master y enviar DatabaseDescriptionPackets con I M y MS a 1
			FSMIdAccionOSPFv2Vecino.COMO_MASTER_ENVIAR_DATABASEDESCRIPTIONPACKETS_VACIOS.getInstance().execute(contextoV, evento.getArgumento());
			//esto hacerlo hasta que se produzca la siguiente transicion cada RxmtInterval
			
			//iniciar TemporizadorEnvioDDPconIMMS
			FSMIdAccionOSPFv2Vecino.INICIAR_TEMPORIZADOR_ENVIO_DDP_CON_IMMS.getInstance().execute(contextoV, evento.getArgumento());	
		}
		// else if (guarda.equals("No crear adjacencia"):  no hay acciones que ejecutar 		
		
		
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
	public FSMEstado procesarEvento1WayReceived(
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
	public FSMEstado procesarEventoSeqNumberMismatch(
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
	public FSMEstado procesarEventoBadLSReq(
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
/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import java.util.Map;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz.FSMIdAccionOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMIdEventoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMEstadoOSPFv2InterfazDown extends FSMEstadoOSPFv2Interfaz {
	static {
		_instancia = new FSMEstadoOSPFv2InterfazDown(FSMIdEstadoOSPFv2Interfaz.DOWN);
	}

	protected FSMEstadoOSPFv2InterfazDown(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	@Override
	public FSMEstado procesarEventoLoopInd(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		//Resetea variables de interfaz y desactiva temporizadores
		FSMIdAccionOSPFv2Interfaz.RESETEAR_VARIABLES_INTERFAZ.getInstance().execute(contexto, evento.getArgumento());
		//Destruir conexiones con vecinos de esta interfaz (KillNbr)
		FSMIdAccionOSPFv2Interfaz.DESTRUIR_CONEXION_VECINOS.getInstance().execute(contexto, evento.getArgumento());
		
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoUnloopInd(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** No acción. */

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoInterfaceDown(FSMContexto contexto,
			FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */		
		//Resetea variables de interfaz y desactiva temporizadores
		FSMIdAccionOSPFv2Interfaz.RESETEAR_VARIABLES_INTERFAZ.getInstance().execute(contexto, evento.getArgumento());
		//Destruir conexiones con vecinos de esta interfaz (KillNbr)
		FSMIdAccionOSPFv2Interfaz.DESTRUIR_CONEXION_VECINOS.getInstance().execute(contexto, evento.getArgumento());
				
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoInterfaceUp(
			FSMContexto contexto, FSMEvento evento) {
		
		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		/** Evalúa guarda */
		String guarda;
		//Evaluar si la red es PTP o virtual link
		//Evaluar si router puede ser o no DrOther
		if (contextoI.isPointToPoint())  
			guarda = new String("PointToPoint");			
		else{
			if (contextoI.getRouterPriority() == (byte) 0)  
				guarda = new String("DrOther");
			else
				guarda = new String("Waiting");
		}
			
		System.out.println("FSMEstadoOSPFv2InterfazDown:eventoInterfaceUp(): guarda= " + guarda);

		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoI.getMaquinaEstados().getEstadoSiguiente(this, evento, guarda);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoI);
		
		/** Acciones */
		//Inicia temporizador Hello
		FSMIdAccionOSPFv2Interfaz.INICIAR_TEMPORIZADOR_HELLO.getInstance().execute(contextoI, evento.getArgumento());
		
		if (guarda.equals("Waiting")) {
			//Inicia temporizador Wait
			FSMIdAccionOSPFv2Interfaz.INICIAR_TEMPORIZADOR_WAIT.getInstance().execute(contextoI, evento.getArgumento());
			
			//Si nuestra red es non-broadcast, mandar Start a Vecinos de esta interfaz que puedan ser elegidos DR
			if (contextoI.isNetworkNonBroadcast()){
				Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters=contextoI.getListOfNeighbouringRouters();
				for (Map.Entry<Integer, FSMMaquinaEstadosOSPFv2Vecino> pair : listOfNeighbouringRouters.entrySet()) {					
					FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) pair.getValue().getContexto();
					
				    //Generar evento Start en vecinos que puedan ser elegidos DR
					if(contextoV.getNeighborPriority()!=0){
						FSMEvento eventoStart = new FSMEvento(FSMIdEventoOSPFv2Vecino.START, contextoV.getNeighborID());
						((FSMMaquinaEstadosOSPFv2Vecino)contextoV.getMaquinaEstados()).encolarEvento(eventoStart);
					}	    
				}			
			}			
		}
		
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoI);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoWaitTimer(
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
	public FSMEstado procesarEventoBackupSeen(
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
	public FSMEstado procesarEventoNeighborChange(
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
	public FSMEstado procesarEventoHelloTimer(
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
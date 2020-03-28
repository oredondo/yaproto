/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz.FSMIdAccionOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;

public class FSMEstadoOSPFv2InterfazBackup extends FSMEstadoOSPFv2Interfaz {
	static {
		_instancia = new FSMEstadoOSPFv2InterfazBackup(FSMIdEstadoOSPFv2Interfaz.BACKUP);
	}

	protected FSMEstadoOSPFv2InterfazBackup(FSMIdEstado id) {		
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

		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		/** Acciones */
		//Calculo de BDR y DR
		FSMIdAccionOSPFv2Interfaz.CALCULAR_BDR_Y_DR.getInstance().execute(contextoI, evento.getArgumento());
		//esta acci�n modifica el atributo EstadoDesignated del contexto		
		
		/** Evalúa guarda */
		String guarda;		
		guarda=contextoI.getEstadoDesignated();		
		
		System.out.println("FSMEstadoOSPFv2InterfazBackup:eventoNeighborChange(): guarda= " + guarda);
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contextoI.getMaquinaEstados().getEstadoSiguiente(this, evento, guarda);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contextoI);
			

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contextoI);

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

		/** Acciones */
		//Difundir paquetes Hello
		FSMIdAccionOSPFv2Interfaz.DIFUNDIR_HELLO_PACKETS.getInstance().execute(contexto, evento.getArgumento());
				
		//Inicia temporizador Hello
		FSMIdAccionOSPFv2Interfaz.INICIAR_TEMPORIZADOR_HELLO.getInstance().execute(contexto, evento.getArgumento());
				
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

}
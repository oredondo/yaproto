/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;

public interface IFSMEventoOSPFv2InterfazListener {

	/**
	 * LoopInd
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoLoopInd(FSMContexto contexto, FSMEvento evento);

	/**
	 * UnloopInd
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoUnloopInd(FSMContexto contexto, FSMEvento evento);

	/**
	 * InterfaceDown
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoInterfaceDown(FSMContexto contexto, FSMEvento evento);

	/**
	 * InterfaceUp
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoInterfaceUp(FSMContexto contexto, FSMEvento evento);

	/**
	 * WaitTimer
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoWaitTimer(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * BackupSeen
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoBackupSeen(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * NeighborChange
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoNeighborChange(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * HelloTimer
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoHelloTimer(FSMContexto contexto, FSMEvento evento);

}

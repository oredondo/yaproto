/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;

public interface IFSMEventoOSPFv2VecinoListener {

	/**
	 * KillNbr
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoKillNbr(FSMContexto contexto, FSMEvento evento);

	/**
	 * InactivityTimer
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoInactivityTimer(FSMContexto contexto, FSMEvento evento);

	/**
	 * LLDown
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoLLDown(FSMContexto contexto, FSMEvento evento);

	/**
	 * Start
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoStart(FSMContexto contexto, FSMEvento evento);

	/**
	 * HelloReceived
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoHelloReceived(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * 2WayReceived
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEvento2WayReceived(FSMContexto contexto, FSMEvento evento);

	/**
	 * 1WayReceived
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEvento1WayReceived(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * SeqNumberMismatch
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoSeqNumberMismatch(FSMContexto contexto, FSMEvento evento);

	/**
	 * BadLSReq
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoBadLSReq(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * NegotiationDone
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoNegotiationDone(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * ExchangeDone
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoExchangeDone(FSMContexto contexto, FSMEvento evento);

	/**
	 * LoadingDone
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoLoadingDone(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * AdjOK
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoAdjOK(FSMContexto contexto, FSMEvento evento);
	
	/**
	 * DDPconIMMSTimer
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoDDPconIMMSTimer(FSMContexto contexto, FSMEvento evento);
}

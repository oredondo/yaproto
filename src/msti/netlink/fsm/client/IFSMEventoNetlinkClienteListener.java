/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.rip.fsm.FSMContextoRIP;

public interface IFSMEventoNetlinkClienteListener {

	/**
	 * Solicitada orden de modificar ruta
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoOrdenModificarRuta(FSMContexto contexto, FSMEvento evento);

	/**
	 * Ha expirado el temporizador de espera de difusión por actualización (triggered-update), valor aleatorio 1-5 segundos
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoOrdenBorrarRuta(FSMContexto contexto, FSMEvento evento);

	/**
	 * Se recibe una petición RIP desde un encaminador vecino
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoRespuestaControlRecibida(FSMContexto contexto, FSMEvento evento);

	/**
	 * La tabla de rutas ha sufrido algún cambio
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoRespuestaNoControlRecibida(FSMContexto contexto, FSMEvento evento);

	/**
	 * Se recibe un mensaje RIP de respuesta
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoTemporizadorOpSincrona(FSMContexto contexto, FSMEvento evento);

}

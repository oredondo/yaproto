/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;

public interface IFSMEventoRIPRutaListener {

	/**
	 * Ha expirado el temporizador de tiempo de vida de ruta (180s)
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoExpiradoTemporizadorRutaExpirada(FSMContexto contexto, FSMEvento evento);

	/**
	 * Ha expirado el temporizador de eliminar ruta definitivamente (120s)
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoExpiradoTemporizadorRutaEliminar(FSMContexto contexto, FSMEvento evento);

	/**
	 * Se recibe una actualización de esta ruta RIP desde un encaminador vecino
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoActualizacionRuta(FSMContexto contexto, FSMEvento evento);

}

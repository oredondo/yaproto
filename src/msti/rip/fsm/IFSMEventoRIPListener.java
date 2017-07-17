/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;

public interface IFSMEventoRIPListener {

	/**
	 * Ha expirado el temporizador de difusión de rutas (30 segundos)
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoExpiradoTemporizadorDifusionPeriodica(FSMContexto contexto, FSMEvento evento);

	/**
	 * Ha expirado el temporizador de espera de difusión por actualización (triggered-update), valor aleatorio 1-5 segundos
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoExpiradoTemporizadorEsperaDifusionPorActualizacion(FSMContexto contexto, FSMEvento evento);

	/**
	 * Se recibe una petición RIP desde un encaminador vecino
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoPeticionDesdeVecino(FSMContexto contexto, FSMEvento evento);

	/**
	 * La tabla de rutas ha sufrido algún cambio
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoTablaRutasModificada(FSMContexto contexto, FSMEvento evento);

	/**
	 * Se recibe un mensaje RIP de respuesta
	 * @param contexto Contexto para el estado
	 * @param evento Evento recibido
	 * @return Estado siguiente.
	 */
	public FSMEstado procesarEventoRespuestaDesdeVecino(FSMContexto contexto, FSMEvento evento);

}

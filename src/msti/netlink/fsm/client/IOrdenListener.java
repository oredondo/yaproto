/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import java.util.concurrent.TimeoutException;

import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.rip.TablaRutas.Ruta;

public interface IOrdenListener {

	/**
	 * Solicita modificar una ruta vía netlink. Si la ruta existe se modificará, si no existe se creará.
	 * 
	 * El método retorna inmediatamente, tras encolar la petición en el canal netlink para su envío.
	 * 
	 * @ruta  ruta (nueva o existente) a modificar
	 */
	public void ordenModificarRuta(Ruta ruta);

	/**
	 * Solicita modificar una ruta vía netlink. Si la ruta existe se modificará, si no existe se creará.
	 * @ruta  ruta (nueva o existente) a modificar
	 */
	public void ordenBorrarRuta(Ruta ruta);

	/**
	 * Solicita modificar una ruta vía netlink. Si la ruta existe se modificará, si no existe se creará.
	 * 
	 * El método se bloquea , tras encolar la petición en el canal netlink para su envío, a que esta finalice
	 * Retorna el código de error suministrado desde el otro extremo, o bien una excepción si vence el tiempo
	 * de timeout indicado. Si timeout=0, se convierte en una orden asíncrona.
	 * 
	 * @ruta  ruta (nueva o existente) a modificar
	 * @timeout (en milisegundos)
	 * @return  Código de ack(0) o error para la operación
	 */
	public int ordenSincronaModificarRuta(Ruta ruta, long timeout) throws TimeoutException;

	/**
	 * Solicita borrar una ruta vía netlink.
	 * 
	 * El método se bloquea , tras encolar la petición en el canal netlink para su envío, a que esta finalice
	 * Retorna el código de error suministrado desde el otro extremo, o bien una excepción si vence el tiempo
	 * de timeout indicado. Si timeout=0, se convierte en una orden asíncrona.
	 * 
	 * @ruta  ruta (nueva o existente) a modificar
	 * @timeout (en milisegundos)
	 * @return  Código de ack(0) o error para la operación
	 */
	public int ordenSincronaBorrarRuta(Ruta ruta, long timeout) throws TimeoutException;


}

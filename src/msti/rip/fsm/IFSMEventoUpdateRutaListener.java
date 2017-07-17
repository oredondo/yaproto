/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.rip.mensaje.IMensajeRIPRuta;

public interface IFSMEventoUpdateRutaListener {

	/** 
	 * Invocado tras recibirse una actualización de algún prefijo de red en la máquina de estados RIP
	 */
	public void updateRuta(IMensajeRIPRuta ruta);

}

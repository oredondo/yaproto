/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;

public class FSMAccionNotificarFinOpSincrona implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionNotificarFinOpSincrona _instancia = new FSMAccionNotificarFinOpSincrona();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionNotificarFinOpSincrona() {
	}


	@Override
	public void execute(FSMContexto contexto, Object o) {
		// Obtener entrada de lista de operaciones síncronas
/*		mapa.get(idrequest) -> entry
		Si enrada con ese número de petición...
		si o.idEvent == temporizador_op_sincrona... ha sido un timeout (timeout=true)
		si o.idEvent == pdu de control -> sacar codigo de error y dejarlo en entry
		
		notificar (synchronized (entry) { notifyAll(); )
*/	
		throw new UnsupportedOperationException("no implementado.");
}

}
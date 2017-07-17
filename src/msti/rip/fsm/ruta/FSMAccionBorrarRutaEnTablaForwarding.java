/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.netlink.fsm.client.INetlinkOrden;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.Ruta;

public class FSMAccionBorrarRutaEnTablaForwarding implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionBorrarRutaEnTablaForwarding _instancia = new FSMAccionBorrarRutaEnTablaForwarding();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionBorrarRutaEnTablaForwarding() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		String clave = (String) o;
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		INetlinkOrden tablaForwarding = (INetlinkOrden) contexto.get("TablaForwarding");

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		if (tablaForwarding == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaForwarding' no existente en el contexto.");
		
		
		tablaForwarding.ordenBorrarRuta(tablaRutas.getRuta(clave));
	}

}
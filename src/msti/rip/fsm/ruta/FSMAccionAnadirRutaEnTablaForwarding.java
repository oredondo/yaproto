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
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMAccionAnadirRutaEnTablaForwarding implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionAnadirRutaEnTablaForwarding _instancia = new FSMAccionAnadirRutaEnTablaForwarding();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionAnadirRutaEnTablaForwarding() {
	}

	protected void mensajeRIPRuta2Ruta(MensajeRIPRuta mensaje, Ruta ruta) {
		ruta.destino = mensaje.getDireccionIP();
		ruta.prefijoRed = mensaje.getLongitudPrefijoRed();
		ruta.proximoSalto = mensaje.getDireccionProximoSalto();
		ruta.distancia = mensaje.getMetrica();
	}
	@Override
	public void execute(FSMContexto contexto, Object o) {
		MensajeRIPRuta mensajeRuta = (MensajeRIPRuta) o;
		INetlinkOrden tablaForwarding = (INetlinkOrden) contexto.get("TablaForwarding");
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas"); //TODO: temporal hasta que Ruta no sea subclase de TablaRutas

		if (tablaForwarding == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaForwarding' no existente en el contexto.");
		
		Ruta ruta = tablaRutas.new Ruta();
		mensajeRIPRuta2Ruta(mensajeRuta, ruta);
		tablaForwarding.ordenModificarRuta(ruta);
	}

}
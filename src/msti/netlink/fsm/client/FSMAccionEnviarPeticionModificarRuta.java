/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.SesionRawSocketNetlink;
import msti.netlink.mensaje.IMensajeNetlink.NetlinkMessageType;
import msti.netlink.mensaje.MensajeNetlinkRoute;
import msti.rip.TablaRutas.Ruta;

public class FSMAccionEnviarPeticionModificarRuta implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarPeticionModificarRuta _instancia = new FSMAccionEnviarPeticionModificarRuta();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionEnviarPeticionModificarRuta() {
	}

	public static MensajeNetlinkRoute generarPeticionModificarRuta(Ruta ruta) {
		MensajeNetlinkRoute.Builder mensaje = MensajeNetlinkRoute.Builder.crear();

		// Construye un mensaje Netlink para modificación de ruta
		mensaje.setMessageType(NetlinkMessageType.RTM_NEWROUTE);
		//TODO: rellenar mensaje!!
		return mensaje.build();

	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		SesionRawSocketNetlink sesion = (SesionRawSocketNetlink) contexto.get("SesionNetlinkCliente");
		MensajeNetlinkRoute mensaje;
		Ruta ruta = (Ruta) o;

		if (sesion == null)
			throw new IllegalArgumentException("Objeto con clave 'SesionNetlinkCliente' no existente en el contexto.");

		// Construye mensaje
   	// mensaje = generarPeticionModificarRuta(ruta);
		//
		// // Lo envía a través de la sesión
  	// Escritura escritura = new Escritura(mensaje);
    // sesion.escribir(escritura);
	}

}

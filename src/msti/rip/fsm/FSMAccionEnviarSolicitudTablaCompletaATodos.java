/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.net.InetSocketAddress;
import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.SesionDatagrama;
import msti.rip.mensaje.MensajeRIPPeticion;

public class FSMAccionEnviarSolicitudTablaCompletaATodos implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarSolicitudTablaCompletaATodos _instancia = new FSMAccionEnviarSolicitudTablaCompletaATodos();

	public FSMAccionEnviarSolicitudTablaCompletaATodos() {
	}
	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	public static MensajeRIPPeticion generarMensajeRIPPeticion() {
		MensajeRIPPeticion.Builder peticion = MensajeRIPPeticion.Builder.crear() ;
		
		peticion.setVersion((byte) 2);
		peticion.setPeticionTablaCompleta(true);
		return peticion.build();		
	}


	@Override
	public void execute(FSMContexto contexto, Object o) {
		MensajeRIPPeticion peticion;
		SesionDatagrama sesion = (SesionDatagrama) contexto.get("SesionRIP");

		if (sesion == null)
			throw new IllegalArgumentException("Objeto con clave 'SesionRIP' no existente en el contexto id=" + contexto.getId());

		peticion = generarMensajeRIPPeticion();
		
		/* si hay modificadas, lo transmite */
		// TODO: repetir por todas las interfaces de red
		Escritura escritura = new Escritura(peticion);
		escritura.setDireccionDestino(new InetSocketAddress("224.0.0.9", 520)); // TODO: constante
		sesion.escribir(escritura);
	}
}

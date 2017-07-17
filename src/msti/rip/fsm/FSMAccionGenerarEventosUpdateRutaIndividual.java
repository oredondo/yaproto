/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Lectura;
import msti.rip.mensaje.IMensajeRIPRuta;
import msti.rip.mensaje.MensajeRIPRespuesta;

public class FSMAccionGenerarEventosUpdateRutaIndividual implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionGenerarEventosUpdateRutaIndividual _instancia = new FSMAccionGenerarEventosUpdateRutaIndividual();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionGenerarEventosUpdateRutaIndividual() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		System.out.print(this.getClass().getName()+": execute()");

		Lectura lectura = (Lectura) o;
		MensajeRIPRespuesta mensaje = (MensajeRIPRespuesta) lectura.getMensaje();
		
		if (mensaje == null)
			throw new IllegalArgumentException("Objeto recibido no es de clase esperada MensajeRIPRespuesta.");

		// Para cada ruta recibida, genera un evento de ruta actualizada para que lo procese cualquier observador
		if (mensaje.hasRIPRutas() && (!mensaje.getRIPRutas().isEmpty())) {
			System.out.println(" genera " + mensaje.getRIPRutas().size() + " eventos.");
			for (IMensajeRIPRuta ruta: mensaje.getRIPRutas())
				((FSMMaquinaEstadosRIP) contexto.getMaquinaEstados()).notificarEventoUpdateRuta(ruta);
		}
		else
			System.out.print(" genera 0 eventos (no contiene rutas).");

		// TODO: difundir interfaz por la que se ha aprendido la ruta
	}

}
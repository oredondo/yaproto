/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.rip.fsm.ruta.FSMMaquinaEstadosRIPRuta.FSMIdAccionRIPRuta;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMEstadoRIPRutaNoExiste extends FSMEstadoRIPRuta {
	static {
		_instancia = new FSMEstadoRIPRutaNoExiste(FSMIdEstadoRIPRuta.NOEXISTERUTA);
	}

	protected FSMEstadoRIPRutaNoExiste(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	@Override
	public FSMEstado procesarEventoExpiradoTemporizadorRutaExpirada(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		// No debe ocurrir este evento en este estado TODO: excepción illegalstate?

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoExpiradoTemporizadorRutaEliminar(
			FSMContexto contexto, FSMEvento evento) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** No acción. */
		// No debe ocurrir este evento en este estado TODO: excepción illegalstate?
		
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoActualizacionRuta(FSMContexto contexto,
			FSMEvento evento) {

		/** Evalúa guarda */
		MensajeRIPRuta mensaje = (MensajeRIPRuta) evento.getArgumento();
		String guarda;

		if (mensaje.getMetrica() < 15)  //TODO: Constante RIP_INFINITO
			guarda = new String("D<15");			
		else
			guarda = new String("D>=15");
		
		System.out.println("FSMEstadoRIPRutaNoexiste:eventoActualizacionRuta(): guarda=" + guarda + ", "+ mensaje.getDireccionIP() + "/" + mensaje.getLongitudPrefijoRed());

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento, guarda);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		
		if (guarda.equals("D<15")) {

			// TODO: Podría cambiar de clase y construir ya una Ruta, de esta forma en las acciones, fuera de la máquina, nunca se vería un mensaje (formato de la máquina) pero por eficiencia, se reaprovecha.
			// Lo añade en tabla de rutas  D=D+1 y N
			// mensaje = mensaje.toBuilder().setMetrica(mensaje.getMetrica() + 1).build(); 
			MensajeRIPRuta mensajeActualizado = ((MensajeRIPRuta.Builder) mensaje.toBuilder())
					.setMetrica(mensaje.getMetrica() + 1)
					.build();
					
			FSMIdAccionRIPRuta.TABLARUTAS_ANADIR_RUTA.getInstance().execute(contexto, mensajeActualizado);
			// Crea entrada en tabla forwarding
			FSMIdAccionRIPRuta.TABLAFORWARDING_ANADIR_RUTA.getInstance().execute(contexto, mensajeActualizado);
			// Inicia temporizador expiración ruta
			FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAEXPIRADA.getInstance().execute(contexto, mensajeActualizado);
		}
		// Si guarda.equals("D>=15"): no se ejecuta ninguna acción

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

}

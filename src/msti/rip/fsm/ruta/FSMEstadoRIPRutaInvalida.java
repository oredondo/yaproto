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
import msti.util.TimerEventProducer;

public class FSMEstadoRIPRutaInvalida extends FSMEstadoRIPRuta {
	static {
		_instancia = new FSMEstadoRIPRutaInvalida(FSMIdEstadoRIPRuta.RUTAINVALIDA);
	}

	protected FSMEstadoRIPRutaInvalida(FSMIdEstado id) {		
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
		// No debe suceder esta transición

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
		
		/** Obtiene y ejecuta acciones */
		TimerEventProducer timer = (TimerEventProducer)evento.getArgumento();
		// El id del Timer trae formato TemporizadorRutaExpirada#ClaveRuta
		String clave = timer.getId().substring(timer.getId().indexOf("#") + 1);

		// Borra entrada en tabla forwarding
		FSMIdAccionRIPRuta.TABLARUTAS_BORRAR_RUTA.getInstance().execute(contexto, clave);

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

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		if (guarda.equals("N==Nexthop,D<15")) {
			// Actualiza tabla rutas: Dist=D+1, NextHop=N, Modificado=SÍ
			FSMIdAccionRIPRuta.TABLARUTAS_MODIFICAR_RUTA.getInstance().execute(contexto, 
					evento.getArgumento());
			// Modifica entrada en tabla forwarding NextHop=N
			FSMIdAccionRIPRuta.TABLAFORWARDING_MODIFICAR_RUTA.getInstance().execute(contexto, 
					evento.getArgumento());
			// Desactiva temporizador eliminación ruta
			FSMIdAccionRIPRuta.DESACTIVAR_TEMPORIZADOR_RUTAELIMINAR.getInstance().execute(contexto, 
					evento.getArgumento());
			// Inicia temporizador expiración ruta
			FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAEXPIRADA.getInstance().execute(contexto, 
					evento.getArgumento());
		}
		// else if (guarda.equals("N==Nexthop,D>=15"):  no hay acciones que ejecutar 
	
		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

}

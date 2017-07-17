/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.Ruta;
import msti.rip.fsm.ruta.FSMMaquinaEstadosRIPRuta.FSMIdAccionRIPRuta;
import msti.rip.mensaje.MensajeRIPRuta;
import msti.util.TimerEventProducer;

public class FSMEstadoRIPRutaAceptada extends FSMEstadoRIPRuta {
	static {
		_instancia = new FSMEstadoRIPRutaAceptada(FSMIdEstadoRIPRuta.RUTAACEPTADA);
	}

	protected FSMEstadoRIPRutaAceptada(FSMIdEstado id) {		
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
		TimerEventProducer timer = (TimerEventProducer)evento.getArgumento();
		// El id del Timer trae formato TemporizadorRutaExpirada#ClaveRuta
		String clave = timer.getId().substring(timer.getId().indexOf("#") + 1);

		// Modifica entrada en tabla rutas
		FSMIdAccionRIPRuta.TABLARUTAS_MODIFICAR_RUTA_ESTABLECER_DISTANCIA_INFINITO.getInstance().execute(contexto, clave);
		// Borra entrada en tabla forwarding
		FSMIdAccionRIPRuta.TABLAFORWARDING_BORRAR_RUTA.getInstance().execute(contexto, clave);
		// Inicia temporizador ruta eliminar
		FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAELIMINAR.getInstance().execute(contexto, clave);
		
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
		//throw new IllegalStateException("Evento temporizador ruta Eliminar en estado RIPRutaAceptada.");

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	@Override
	public FSMEstado procesarEventoActualizacionRuta(FSMContexto contexto,
			FSMEvento evento) {

		/** Evalúa guarda para decidir estadoActual.onSalida() si existe cambio estado */
		MensajeRIPRuta mensaje = (MensajeRIPRuta) evento.getArgumento();
		String guarda;

		/* Obtiene ruta actual */
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		String clave = TablaRutas.generarClaveTablaRutas(mensaje.getDireccionIP(), mensaje.getLongitudPrefijoRed());
		Ruta ruta = tablaRutas.getRuta(clave);
		if (ruta == null) {
			throw new IllegalStateException("Entregado evento a FSMMaquinaEstadosRIPRuta asociada a una ruta " + clave + " que no existe en la RIB y no debería tener máquina!");
		}
		
		/* Evalúa la guarda que se va aplicar en el Update(N,D), en base al actual Ruta(Nexthop,Dest)*/
		/* N==NextHop */
		if (ruta.proximoSalto.equals(mensaje.getDireccionProximoSalto())) {
			if (mensaje.getMetrica() >= 15)
				guarda = new String("N==Nexthop,D>=15");
			else if (mensaje.getMetrica() + 1 == ruta.distancia) 
				guarda = new String("N==Nexthop,D<15,D+1==Dist");
			else 
				guarda = new String("N==Nexthop,D<15,D+1!=Dist");
		}
		else { 
			if (mensaje.getMetrica() + 1 >= ruta.distancia)
				guarda = new String("N!=Nexthop,D+1>=Dist");
			else if (mensaje.getMetrica() < 15)
				guarda = new String("N!=Nexthop,D+1<Dist,D<15");
			else 
				guarda = null; //no ocurre, Si D>15, entonces seguro que D+1>=Dist
		}
		
		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, evento, guarda);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);
		
		/** Obtiene y ejecuta acciones */
		if (guarda.equals("N==Nexthop,D>=15")) {
			// Actualiza tabla rutas: Dist=16, NextHop=N, Modificado=SÍ
			FSMIdAccionRIPRuta.TABLARUTAS_MODIFICAR_RUTA_ESTABLECER_DISTANCIA_INFINITO.getInstance().execute(contexto, 
					clave);
			// Borra entrada en tabla forwarding
			FSMIdAccionRIPRuta.TABLAFORWARDING_BORRAR_RUTA.getInstance().execute(contexto, 
					clave);
			// Inicia temporizador eliminación ruta
			FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAELIMINAR.getInstance().execute(contexto, 
					clave);
		}
		else if (guarda.equals("N==Nexthop,D<15,D+1==Dist")) {
			// Inicia temporizador expiración ruta
			FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAEXPIRADA.getInstance().execute(contexto, 
					evento.getArgumento());
			
		}
		else if (guarda.equals("N==Nexthop,D<15,D+1!=Dist")) {
			// Actualiza tabla rutas: Dist=D+1, NextHop=N, Modificado=SÍ
			FSMIdAccionRIPRuta.TABLARUTAS_MODIFICAR_RUTA.getInstance().execute(contexto, 
					evento.getArgumento());
			// Inicia temporizador expiración ruta
			FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAEXPIRADA.getInstance().execute(contexto, 
					evento.getArgumento());
		}
		else if (guarda.equals("N!=Nexthop,D+1>=Dist")) {
			// No acción
		}
		else if (guarda.equals("N!=Nexthop,D+1<Dist,D<15")) {
			// Actualiza tabla rutas: Dist=D+1, NextHop=N, Modificado=SÍ
			FSMIdAccionRIPRuta.TABLARUTAS_MODIFICAR_RUTA.getInstance().execute(contexto, 
					evento.getArgumento());
			// Modifica entrada en tabla forwarding NextHop=N
			FSMIdAccionRIPRuta.TABLAFORWARDING_MODIFICAR_RUTA.getInstance().execute(contexto, 
					evento.getArgumento());
			// Inicia temporizador expiración ruta
			FSMIdAccionRIPRuta.REINICIAR_TEMPORIZADOR_RUTAEXPIRADA.getInstance().execute(contexto, 
					evento.getArgumento());
		}

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	

}

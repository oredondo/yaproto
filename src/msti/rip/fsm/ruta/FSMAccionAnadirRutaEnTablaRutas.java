/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.EstadoRuta;
import msti.rip.TablaRutas.Ruta;
import msti.rip.mensaje.MensajeRIPRuta;
import msti.util.ITimerListener;

public class FSMAccionAnadirRutaEnTablaRutas implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionAnadirRutaEnTablaRutas _instancia = new FSMAccionAnadirRutaEnTablaRutas();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionAnadirRutaEnTablaRutas() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		MensajeRIPRuta mensajeRuta = (MensajeRIPRuta) o;
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		// TODO: En lugar de FiltroFactoria, definir interfaz Productor con addListener,removeListener
		FiltroFactoria eventoUpdateRutaProductor = (FiltroFactoria) contexto.get("FSMEventoUpdateRutaProductor");

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		if (eventoUpdateRutaProductor == null)
			throw new IllegalArgumentException("Objeto con clave 'FSMEventoUpdateRutaProductor' no existente en el contexto.");

		String clave = TablaRutas.generarClaveTablaRutas(mensajeRuta.getDireccionIP(), 
				mensajeRuta.getLongitudPrefijoRed());

		Ruta ruta = tablaRutas.new Ruta();
		ruta.destino = mensajeRuta.getDireccionIP();
		ruta.prefijoRed = mensajeRuta.getLongitudPrefijoRed();
		ruta.proximoSalto = mensajeRuta.getDireccionProximoSalto();
		ruta.distancia = mensajeRuta.getMetrica();
		/* campos rip */
		
		// suscribe la máquina de estados de esta ruta a los eventos update ruta que genera la FSMRIP
		ruta.maquinaEstados = (FSMMaquinaEstadosRIPRuta) contexto.getMaquinaEstados();
		eventoUpdateRutaProductor.addRutaUpdateListener(ruta.maquinaEstados, ruta.destino, ruta.prefijoRed);
		
		ruta.estado = EstadoRuta.CREADA;

		tablaRutas.addRuta(clave, ruta); 
		
	}
}
/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMContexto;
import msti.io.FiltroNotificador;
import msti.io.ISesionCreadaListener;
import msti.io.Sesion;
import msti.netlink.fsm.client.INetlinkOrden;
import msti.rip.TablaRutas;
import msti.rip.fsm.ruta.FactoriaFSMMaquinaEstadosRIPRuta;
import msti.rip.fsm.ruta.FiltroFactoria;

public class FactoriaFSMMaquinaEstadosRIP implements ISesionCreadaListener {
	private final TablaRutas tablaRutas;
	private final INetlinkOrden tablaForwarding;
	private FSMContexto contexto;

	public FactoriaFSMMaquinaEstadosRIP(TablaRutas tablaRutas, INetlinkOrden tablaForwarding, FSMContexto contexto) {
		this.tablaRutas = tablaRutas;
		this.tablaForwarding = tablaForwarding;
		this.contexto = contexto;
	}

	public FSMMaquinaEstadosRIP getInstance() {
		contexto.put("TablaRutas", this.tablaRutas);
		contexto.put("TablaForwarding", this.tablaForwarding);
		return new FSMMaquinaEstadosRIP(contexto);
	}

	@Override
	public void sesionCreada(Sesion sesion) {
		System.out.println("FactoriaFSMRIP: sesionCreada()");
		// Instancia una máquina de estados RIP para cada nueva sesión
		FSMMaquinaEstadosRIP meRIP = this.getInstance();
		// Añade sesión para que pueda enviar desde estado Inicio
		meRIP.getContexto().put("SesionRIP",  sesion);

		// Suscribe la máquina a los eventos de lectura de la sesión para que pueda recibir mensajes
		FiltroNotificador filtroNotificador = (FiltroNotificador) sesion.getAceptador().getCadenaFiltros().getLast();
		filtroNotificador.addLecturaListener(meRIP, sesion.getId());

		// Suscribe la máquina a los eventos de tabla modificada que genera la tabla de rutas
		tablaRutas.addTablaRutasModificadaListener(meRIP);

		// Inscribe un filtro para instanciar máquinas estado de ruta, en ciertas transiciones
		INetlinkOrden tablaForwarding = (INetlinkOrden) meRIP.getContexto().get("TablaForwarding");
		FiltroFactoria filtroFactoria = new FiltroFactoria(new FactoriaFSMMaquinaEstadosRIPRuta(tablaRutas, tablaForwarding));
		meRIP.addRutaUpdateAllListener(filtroFactoria); // se suscribe a todas las rutas

		// Primera transición, desde pseudoestado Inicio, al primer estado
		// (puede necesitar sesión en el contexto para poder enviar)
		meRIP.init(meRIP.getContexto());

		// Lanza esta máquina de estados como hilo
		meRIP.setHilo(true);
		Thread hilo = new Thread(meRIP);
		hilo.start();
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import msti.fsm.FSMContexto;
import msti.io.FiltroNotificador;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.Sesion;
import msti.netlink.fsm.client.INetlinkOrden;
import msti.ospfv2.ConfiguracionOSPFv2;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FactoriaFSMMaquinaEstadosOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FiltroFactoria;

public class FactoriaFSMMaquinaEstadosOSPFv2Interfaz implements ISesionCreadaListener {
	private final TablaRutas tablaRutas;
	private final INetlinkOrden tablaForwarding;
	private final ConfiguracionOSPFv2 conf;

	public FactoriaFSMMaquinaEstadosOSPFv2Interfaz(TablaRutas tablaRutas, INetlinkOrden tablaForwarding, ConfiguracionOSPFv2 conf) {
		this.tablaRutas = tablaRutas;
		this.tablaForwarding = tablaForwarding;
		this.conf = conf;
	}

	public FSMMaquinaEstadosOSPFv2Interfaz getInstance() {
		FSMContextoOSPFv2Interfaz contexto = new FSMContextoOSPFv2Interfaz();
		contexto.put("TablaRutas", this.tablaRutas);
		contexto.put("TablaForwarding", this.tablaForwarding);
		
		//conf
		contexto.setRouterID(conf.routerID);
		contexto.setAreaID(conf.areaID);
		contexto.setAuType(conf.auType);
		contexto.setExternalRoutingCapability(conf.externalRoutingCapability);
		contexto.setConfiguracion(conf);
		
		return new FSMMaquinaEstadosOSPFv2Interfaz(contexto);
	}

	@Override
	public void sesionCreada(Sesion sesion) {
		System.out.println("FactoriaFSMOSPFv2Interfaz: sesionCreada()");
		// Instancia una máquina de estados OSPF para cada nueva sesión
		FSMMaquinaEstadosOSPFv2Interfaz meOSPFv2Interfaz = this.getInstance();
		// Añade sesión para que pueda enviar desde estado Inicio
		//meOSPFv2Interfaz.getContexto().put("SesionOSPFv2Interfaz",  sesion);
		((FSMContextoOSPFv2Interfaz) meOSPFv2Interfaz.getContexto()).setSesion(sesion);
		
		// Suscribe la máquina a los eventos de lectura de la sesión para que pueda recibir mensajes
		FiltroNotificador filtroNotificador = (FiltroNotificador) sesion.getAceptador().getCadenaFiltros().getLast();
		//no hace falta que lea, solo que env�e
		//filtroNotificador.addLecturaListener(meOSPFv2Interfaz, sesion.getId());
	
		// Suscribe la máquina a los eventos de tabla modificada que genera la tabla de rutas
		//tablaRutas.addTablaRutasModificadaListener(meOSPFv2Interfaz);
		
		
		//Vecinos
		// Inscribe un filtro para instanciar máquinas estado de vecino, en ciertas transiciones 
		INetlinkOrden tablaForwarding = (INetlinkOrden) meOSPFv2Interfaz.getContexto().get("TablaForwarding");		
		FiltroFactoria filtroFactoria = new FiltroFactoria(new FactoriaFSMMaquinaEstadosOSPFv2Vecino(tablaRutas, tablaForwarding), meOSPFv2Interfaz.getContexto());
		filtroNotificador.addLecturaListener(filtroFactoria, sesion.getId());


		// Primera transición, desde pseudoestado Inicio, al primer estado 
		// (puede necesitar sesión en el contexto para poder enviar)
		meOSPFv2Interfaz.init(meOSPFv2Interfaz.getContexto());
		
		// Lanza esta máquina de estados como hilo
		meOSPFv2Interfaz.setHilo(true);
		Thread hilo = new Thread(meOSPFv2Interfaz);
		hilo.start();
		
		//A�ade la m�quina al fichero global de configuracion
		conf.listaFSMInterfaz.add(meOSPFv2Interfaz);
	}

}

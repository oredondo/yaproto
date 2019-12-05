/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.fsm.FSMEstadoFin;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz.FSMIdAccionOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino.FSMIdAccionOSPFv2Vecino;

/**
 * Estado de la máquina de estados que gestiona el protocolo RIP (procesa request y genera response)
 *
 */
public abstract class FSMEstadoOSPFv2Interfaz extends FSMEstado implements IFSMEventoOSPFv2InterfazListener {

	/**
	 *  Id de los estados de la máquina de estados que gestiona el protocolo RIP (peticiones y respuestas)	 *
	 */
/*	public enum FSMIdEstadoRIP implements FSMIdEstado, Iterable<FSMIdEstadoRIP> {
		INICIO(FSMEstadoRIPInicio.getInstance()),
		FIN(FSMEstadoFin.getInstance()),
		TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO(FSMEstadoRIPTemporizadorTUInactivo.getInstance()),
		TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO(FSMEstadoRIPTemporizadorTUActivo.getInstance()),
		TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA(FSMEstadoRIPTemporizadorTUActivoYTablaRutasModificada.getInstance());
	
		private FSMEstado value;

		private FSMIdEstadoRIP(FSMEstado value) { 
			this.value = value; 
			}
		public FSMEstado getInstance() { return this.value; }
		public FSMIdEstadoRIP getByInstance(FSMEstado value) { 
			for (FSMIdEstadoRIP id: values())
				if (id.getInstance() == value)
					return id;
			return null;
		}
		public Iterator<FSMIdEstadoRIP> iterator() { return Collections.singleton(this).iterator(); }
	}
*/
	
	public enum FSMIdEstadoOSPFv2Interfaz implements FSMIdEstado {
		INICIO(FSMEstadoOSPFv2InterfazInicio.class),
		FIN(FSMEstadoFin.class),
		LOOPBACK(FSMEstadoOSPFv2InterfazLoopback.class),
		DOWN(FSMEstadoOSPFv2InterfazDown.class),
		POINTTOPOINT(FSMEstadoOSPFv2InterfazPointtopoint.class),
		WAITING(FSMEstadoOSPFv2InterfazWaiting.class),
		BACKUP(FSMEstadoOSPFv2InterfazBackup.class),
		DR(FSMEstadoOSPFv2InterfazDr.class),
		DROTHER(FSMEstadoOSPFv2InterfazDrother.class);
	
		private Class<? extends FSMEstado> clazz;
		private FSMEstado instance;

		private FSMIdEstadoOSPFv2Interfaz(Class<? extends FSMEstado> clazz) { this.clazz = clazz; }
		public FSMEstado getInstance() { return this.instance; }
		private Class<? extends FSMEstado> getSingletonClass() { return this.clazz; }
		private void setInstance(FSMEstado instance) { this.instance = instance; };
		public FSMIdEstadoOSPFv2Interfaz getByInstance(FSMEstado instance) { 
			for (FSMIdEstadoOSPFv2Interfaz id: values())
				if (id.getInstance() == instance)
					return id;
			return null;
		}
		// Los enumerados se construyen antes q static, así que no se puede hacer en constructor
		static {
			for (FSMIdEstadoOSPFv2Interfaz en: values()) {
				try {
					en.setInstance( (FSMEstado) en.getSingletonClass().getMethod("getInstance",
							(Class[])null).invoke(null));
				} catch (Exception e) {
				}
			}
		}
	}
	
	protected FSMEstadoOSPFv2Interfaz(FSMIdEstado id) {		
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	/**
	 * No se usa esta versión basada en procesar el evento directamente en el estado.
	 * Se sigue patrón Estado (GoF) y se incluye un método para procesar cada Evento, en una interfaz.
	 * De esta forma, no es posible olvidar procesar un evento, y no hay switch. 
	 */
	@Override
	public FSMEstado procesar(FSMContexto contexto, FSMEvento evento) {
		throw new UnsupportedOperationException("procesar genérico no implementado. Utilice los métodos especializados procesarEventoX() para cada evento X");
	}
	
	public void onSalida(FSMContexto contexto){
		//LLama a la accion de generacion de routerLinks
		FSMIdAccionOSPFv2Interfaz.GENERAR_LSA_ROUTER_LINKS.getInstance().execute(contexto, this.getId());
		
	}

}

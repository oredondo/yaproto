/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.fsm.FSMEstadoInicio;
import msti.fsm.FSMEstadoFin;

/**
 * Estado de la máquina de estados que gestiona el protocolo RIP (procesa request y genera response)
 *
 */
public abstract class FSMEstadoOSPFv2Vecino extends FSMEstado implements IFSMEventoOSPFv2VecinoListener {

	/**
	 *  Id de los estados de la máquina de estados que gestiona el protocolo RIP (peticiones y respuestas)	 *
	 */
	
	public enum FSMIdEstadoOSPFv2Vecino implements FSMIdEstado {
		INICIO(FSMEstadoOSPFv2VecinoInicio.class),
		FIN(FSMEstadoFin.class),
		DOWN(FSMEstadoOSPFv2VecinoDown.class),
		ATTEMPT(FSMEstadoOSPFv2VecinoAttempt.class),
		INIT(FSMEstadoOSPFv2VecinoInit.class),
		TWOWAY(FSMEstadoOSPFv2Vecino2Way.class),
		EXSTART(FSMEstadoOSPFv2VecinoExstart.class),
		EXCHANGE(FSMEstadoOSPFv2VecinoExchange.class),
		LOADING(FSMEstadoOSPFv2VecinoLoading.class),
		FULL(FSMEstadoOSPFv2VecinoFull.class);
	
		private Class<? extends FSMEstado> clazz;
		private FSMEstado instance;

		private FSMIdEstadoOSPFv2Vecino(Class<? extends FSMEstado> clazz) { this.clazz = clazz; }
		public FSMEstado getInstance() { return this.instance; }
		private Class<? extends FSMEstado> getSingletonClass() { return this.clazz; }
		private void setInstance(FSMEstado instance) { this.instance = instance; };
		public FSMIdEstadoOSPFv2Vecino getByInstance(FSMEstado instance) { 
			for (FSMIdEstadoOSPFv2Vecino id: values())
				if (id.getInstance() == instance)
					return id;
			return null;
		}
		// Los enumerados se construyen antes q static, así que no se puede hacer en constructor
		static {
			for (FSMIdEstadoOSPFv2Vecino en: values()) {
				try {
					en.setInstance( (FSMEstado) en.getSingletonClass().getMethod("getInstance",
							(Class[])null).invoke(null));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
	
	protected FSMEstadoOSPFv2Vecino(FSMIdEstado id) {		
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

}

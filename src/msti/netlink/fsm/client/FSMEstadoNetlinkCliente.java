/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.fsm.client;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.fsm.FSMEstadoInicio;
import msti.fsm.FSMEstadoFin;

/**
 * Estado de la máquina de estados que gestiona el protocolo RIP (procesa request y genera response)
 *
 */
public abstract class FSMEstadoNetlinkCliente extends FSMEstado implements IFSMEventoNetlinkClienteListener {

	/**
	 *  Id de los estados de la máquina de estados que gestiona el protocolo Netlink (peticiones y respuestas)	 *
	 */
	public enum FSMIdEstadoNetlinkCliente implements FSMIdEstado {
		INICIO(FSMEstadoInicio.class),
		FIN(FSMEstadoFin.class),
		PREPARADO(FSMEstadoNetlinkClientePreparado.class);
	
		private Class<? extends FSMEstado> clazz;
		private FSMEstado instance;

		private FSMIdEstadoNetlinkCliente(Class<? extends FSMEstado> clazz) { this.clazz = clazz; }
		public FSMEstado getInstance() { return this.instance; }
		private Class<? extends FSMEstado> getSingletonClass() { return this.clazz; }
		private void setInstance(FSMEstado instance) { this.instance = instance; };
		public FSMIdEstadoNetlinkCliente getByInstance(FSMEstado instance) { 
			for (FSMIdEstadoNetlinkCliente id: values())
				if (id.getInstance() == instance)
					return id;
			return null;
		}
		// Los enumerados se construyen antes q static, así que no se puede hacer en constructor
		static {
			for (FSMIdEstadoNetlinkCliente en: values()) {
				try {
					en.setInstance( (FSMEstado) en.getSingletonClass().getMethod("getInstance",
							(Class[])null).invoke(null));
				} catch (Exception e) {
				}
			}
		}

	}

	protected FSMEstadoNetlinkCliente (FSMIdEstado id) {		
		super(id);
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

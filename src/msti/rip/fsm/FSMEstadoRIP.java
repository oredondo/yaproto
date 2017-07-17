/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.fsm.FSMEstadoFin;

/**
 * Estado de la máquina de estados que gestiona el protocolo RIP (procesa request y genera response)
 *
 */
public abstract class FSMEstadoRIP extends FSMEstado implements IFSMEventoRIPListener {

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
	
	public enum FSMIdEstadoRIP implements FSMIdEstado {
		INICIO(FSMEstadoRIPInicio.class),
		FIN(FSMEstadoFin.class),
		TEMPORIZADOR_TRIGGEREDUPDATE_INACTIVO(FSMEstadoRIPTemporizadorTUInactivo.class),
		TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO(FSMEstadoRIPTemporizadorTUActivo.class),
		TEMPORIZADOR_TRIGGEREDUPDATE_ACTIVO_Y_TABLARUTAS_MODIFICADA(FSMEstadoRIPTemporizadorTUActivoYTablaRutasModificada.class);
	
		private Class<? extends FSMEstado> clazz;
		private FSMEstado instance;

		private FSMIdEstadoRIP(Class<? extends FSMEstado> clazz) { this.clazz = clazz; }
		public FSMEstado getInstance() { return this.instance; }
		private Class<? extends FSMEstado> getSingletonClass() { return this.clazz; }
		private void setInstance(FSMEstado instance) { this.instance = instance; };
		public FSMIdEstadoRIP getByInstance(FSMEstado instance) { 
			for (FSMIdEstadoRIP id: values())
				if (id.getInstance() == instance)
					return id;
			return null;
		}
		// Los enumerados se construyen antes q static, así que no se puede hacer en constructor
		static {
			for (FSMIdEstadoRIP en: values()) {
				try {
					en.setInstance( (FSMEstado) en.getSingletonClass().getMethod("getInstance",
							(Class[])null).invoke(null));
				} catch (Exception e) {
				}
			}
		}
	}
	
	protected FSMEstadoRIP(FSMIdEstado id) {		
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

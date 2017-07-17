/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;

/**
 * Singleton 
 * 
 * Codifica un estado de la máquina
 * 
 * @author Usuario
 *
 */
public abstract class FSMEstado {


	/*
	 * Identificador único de cada estado en un protocolo concreto
	 * 
	 * Codificar un enumerador que implemente esta interfaz.
	 * NOTA: El método toString() por defecto del enumerador ya lo convierte a cadena de caracteres
	 * 
	 * enum FSMIdEstadoRIP implements FSMIdEstado {
	 * 		Estado1(FSMEstado1.getInstance()),
	 * 		Estado2(FSMEstado2.getInstance());
	 * 
	 * 		private FSMEstado value;
	 * 		
	 * 		private FSMIdEstadoRIP(FSMEstado value) { this.value = value; }
	 * 		public FSMEstado getInstance() { return this.value; }
	 * 		public FSMIdEstadoRIP getByInstance(FSMEstado instance) { 
	 * 			for (FSMIdEstadoRIP id: values())
	 * 				if (id.getInstance() == instance)
	 * 					return id;
	 * 			return null;
	 *		}
	 * }
	 */
	public interface FSMIdEstado {
		 public FSMEstado getInstance();
		 public FSMIdEstado getByInstance(FSMEstado instance);	
	}

	/**
	 * Ids de estados genéricos.
	 */
	public enum FSMIdEstadoBase implements FSMIdEstado {
		INICIO(FSMEstadoInicio.class),		/* Pseudo-estado inicial */
		FIN(FSMEstadoInicio.class);			/* Pseudo-estado final */

		private Class<? extends FSMEstado> clazz;
		private FSMEstado instance;

		private FSMIdEstadoBase(Class<? extends FSMEstado> clazz) { this.clazz = clazz; }
		public FSMEstado getInstance() { return this.instance; }
		private Class<? extends FSMEstado> getSingletonClass() { return this.clazz; }
		private void setInstance(FSMEstado instance) { this.instance = instance; };
		public FSMIdEstadoBase getByInstance(FSMEstado instance) { 
			for (FSMIdEstadoBase id: values())
				if (id.getInstance() == instance)
					return id;
			return null;
		}
		// Los enumerados se construyen antes q static, así que no se puede hacer en constructor
		static {
			for (FSMIdEstadoBase en: values()) {
				try {
					en.setInstance( (FSMEstado) en.getSingletonClass().getMethod("getInstance",
							(Class[])null).invoke(null));
				} catch (Exception e) {
				}
			}
		}
	}

	/* El único objeto singleton (técnica: inicialización bajo demanda (en lugar de diferida en el getInstance() */
	protected static FSMEstado _instancia = null;

	/**
	 * Clase de estado: SIMPLE, COMPUESTO (compuesto por otros estados simples o compuestos)
	 */
	enum FSMTipoEstado {
		SIMPLE,
		COMPUESTO;  /* Compuesto por una submáquina de estados. El estado implementa interfaz IFSMMaquinaEstados */
	};
	private FSMTipoEstado tipoEstado = FSMTipoEstado.SIMPLE;
	
	/* Nombre amigable para el estado, de cara a log, etc. */
	private String nombreEstado;

	/* Tipo de estado */
	private FSMIdEstado idEstado;

	protected FSMEstado() {
		
	}
	protected FSMEstado(FSMIdEstado idEstado) {
		this.idEstado = idEstado;
		this.nombreEstado = idEstado.toString();
	}
	protected FSMEstado(FSMIdEstado idEstado, String nombreEstado) {
		this.idEstado = idEstado;
		this.nombreEstado = nombreEstado;
	}

	/*
	 * Nombre del estado
	 */
	protected void setNombre(String nombreEstado)  {
		this.nombreEstado = nombreEstado;
	}
	
	public String getNombre()  {
		return this.nombreEstado;
	}

	protected void setId(FSMIdEstado idEstado) {
		this.idEstado = idEstado;
	}

	public FSMIdEstado getId()  {
		return this.idEstado;
	}

	/**
	 *  Obtiene la instancia del estado 
	 *  
	 *  Por defecto está codificada usando la técnica de inicialización bajo demanda, por lo que con sólo añadir
	 *  en una subclase llamada "EstadoSubclase" un: 
	 *     static {
	 *     		_instancia = new EstadoSubclase(); 
	 *     };
	 *  la instanciación es segura y no hace falta sobreescribir este método.
	 *  
	 *  Si se desea una inicialización diferida, incluir el siguiente código
	 *  en esta subclase en lugar del static { ... } anterior:
	 *  
	 *	public static FSMEstado getInstance() {
	 *       if (_instancia == null) {
	 *          // Sólo se requiere sincronización cuando la instancia no existe
	 *         synchronized(FSMEstadoSubclase.class) {
	 *            // Necesario volver a comprobar por si otro hilo entró antes a la zona sincro mientras esperábamos
	 *           if (_instancia == null) { 
	 *              _instancia = new FSMEstadoSubclase();
	 *         }
	 *      }
	 *       return _instancia;
	 *  }
	 */
	public static FSMEstado getInstance() {
		return _instancia;
	}
	
	/** 
	 * Procesa un evento.
	 * Recibe el contexto y el evento.
	 * Verifica las posibles condiciones de guarda. Si las verifica, realiza las acciones.
	 * (en realidad, si sabe que va a cambiar de estado, además de las acciones debería invocar antes su 
	 * propio método onExit(), y una vez realizadas las acciones y obtenido el objeto estado siguiente, invocar
	 * su estado.onStart(). Al no estar definidos esos métodos estas acciones quedan antes y después de las
	 * acciones.
	 * 
	 * Por defecto, esta clase sólo obtiene el estado siguiente y lo devuelve.
	 * Serán las subclases las que definar procesos diferentes
	 * 
	 * @return  Estado siguiente
	 */
	public FSMEstado procesar(FSMContexto contexto, FSMEvento evento) {
		return contexto.getMaquinaEstados().getEstadoSiguiente(this);
	}

	public void setTipoEstado(FSMTipoEstado tipoEstado) {
		this.tipoEstado = tipoEstado;
	}

	public FSMTipoEstado getTipoEstado() {
		return this.tipoEstado;
	}

	/**
	 *  Se ejecuta una vez al entrar en un estado 
	 */
	public void onEntrada(FSMContexto contexto) {
		
	}

	/**
	 *  Se ejecuta una vez al salir de un estado 
	 */
	public void onSalida(FSMContexto contexto) {
		
	}

}

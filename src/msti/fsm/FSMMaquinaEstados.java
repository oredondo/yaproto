/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import msti.fsm.FSMEstado.FSMIdEstado;
import msti.fsm.FSMEstado.FSMIdEstadoBase;
import msti.fsm.FSMEvento.FSMIdEvento;
import msti.fsm.FSMEvento.FSMIdEventoBase;

public class FSMMaquinaEstados implements IFSMMaquinaEstados, Runnable {


	/* Atributos TODO: pasar a Context y así que pueda ser singleton? */
	
	/* Mapa de transiciones. Permite obtener, a partir de un estado, e
	public static final 

	/** Estado actual */    //TODO: Pasar a contexto y esta clase podrá ser singleton y tener un getInstance()
	private FSMEstado estado;

	/** 
	 * Cola de eventos a procesar en esta máquina
	 */
	private final BlockingQueue<FSMEvento> colaEventos;

	/** Contexto que comparten los estados */
	private final FSMContexto contexto;   // TODO: Evaluar si sacar todo de la máquina hacia el contexto, y hacer la máquina de estados singleton
	
	/** Indica si esta máquina se ejecuta en hilo aparte o no */
	private boolean esHilo = false;
	
	/** Mapa de transiciones */
	private Map<String, FSMIdEstado> mapaTransiciones; 

	/**
	 * Instancia una máquina de estados. En los objetos del contexto deberían pasarse los
	 * diferentes elementos iniciales de cada máquina de estado que sean imprescindibles, si no se
	 * desea generar un nuevo constructor en las clases derivadas
	 * @param contexto  Contexto específico para esta máquina de estados (habitualmente diferente del
	 * de otras máquinas).
	 */
	public FSMMaquinaEstados(FSMContexto contexto)
	{
		// Almacena contexto
		this.contexto = contexto;
		contexto.setMaquinaEstados(this);  // this no está construido aún (finalizado constructor), cuidado con lo que se hacen en set que referencie esta máquina
											// TODO: pasar a objeto interno del contexto como el resto ¿?
		// Crea una cola bloqueante
		this.colaEventos = new LinkedBlockingQueue<FSMEvento>();
		
		// Establece un estado inicial (debería hacerse en init())
		estado = FSMIdEstadoBase.INICIO.getInstance();
		
		// Crea mapa de transiciones
		mapaTransiciones = new HashMap<String, FSMIdEstado>();

		// Configura mapa de transiciones
		configurarTransiciones();
	}
	
	/**
	 * Inicializa la máquina de estados (hace la primera transición desde estado INICIO al primer
	 * estado configurado
	 */
	public void init(FSMContexto contexto) {
		// Obtiene el inicio
		setEstado(FSMIdEstadoBase.INICIO.getInstance());
		// Realiza la primera transición desde INICIO
		setEstado(((FSMEstadoInicio) getEstadoActivo()).procesarEventoInicio(contexto));		
	}

	/** 
	 * Construye la clave de la tabla de transiciones:
	 * idEstado,idEvento,idGuarda -> idEstado destino

	 * @param idEstado
	 * @param idEvento
	 * @param idGuarda
	 * @return idEstado destino de la transición
	 */
	protected String generarClaveMapaTransiciones(FSMIdEstado idEstado, FSMIdEvento idEvento, String idGuarda) {
		StringBuilder sb = new StringBuilder();

		sb.append(idEstado.toString());
		sb.append(idEvento != null ? idEvento.toString() : idEvento);
		sb.append(idGuarda != null ? idGuarda : idGuarda);
		return sb.toString();		
	}
	public void anadirTransicion(FSMIdEstado idEstadoOrigen, FSMIdEvento idEvento, String idGuarda, FSMIdEstado idEstadoDestino) {
		mapaTransiciones.put( generarClaveMapaTransiciones(idEstadoOrigen, idEvento, idGuarda), idEstadoDestino);
	}
	public void borrarTransicion(FSMIdEstado idEstadoOrigen, FSMIdEvento idEvento, String idGuarda, FSMIdEstado idEstadoDestino) {
		mapaTransiciones.put( generarClaveMapaTransiciones(idEstadoOrigen, idEvento, idGuarda), idEstadoDestino);
	}

	/**
	 * Configuración de transacciones
	 * EstadoOrigen,Evento,guarda -> EstadoFinal
	 */
	protected void configurarTransiciones()	{
		anadirTransicion(FSMIdEstadoBase.INICIO, null, null, FSMIdEstadoBase.FIN);
	}

	public FSMContexto getContexto() {
		return contexto;
	}
	
	protected BlockingQueue<FSMEvento> getColaEventos() {
		return colaEventos;
	}

	/**
	 * Factoría para algunos singleton especiales, de forma que los objetos no requieran conocer los nombres de clase
	 * 
	 * @param Tipo de estado(sólo válidos INICIO, FIN)
	 */
	protected FSMEstado getEstadoPorId(FSMIdEstado idEstado) {
		FSMIdEstadoBase _idEstado = (FSMIdEstadoBase)idEstado; 

		switch (_idEstado) {
		case INICIO:
			return FSMEstadoInicio.getInstance();
		case FIN:
			return FSMEstadoFin.getInstance();
		default: //imposible
			return null;
		}
	}

	
	/**
	 * Establece el estado actual de la máquina.  Si no se usa, establece un estado de tipo Inicial
	 * Útil si la máquina no comienza en el estado inicial por algún motivo: máquinas que instancien otras
	 * máquinas inicializadas durante la acción de la transición, etc.
	 * @param estado
	 */
	public void setEstado(FSMEstado estado) {
		this.estado = estado;
	}

	/**
	 * Utilizada por los objeto Estado para, de forma desacoplada, obtener el objeto siguiente.
	 * Esta forma puede ser utilizada cuando tras un estado todas las transiciones se dirigen a un mismo estado
	 * Ej: estado de inicio, o una máquina que avanza paso a paso en cadena lineal.
	 */
	public FSMEstado getEstadoSiguiente(FSMEstado estado) {
		return getEstadoSiguiente(estado, null, null);
	}

	/**
	 * Utilizada por los objeto Estado para, de forma desacoplada, obtener el estado siguiente.
	 */
	public FSMEstado getEstadoSiguiente(FSMEstado estado, FSMEvento evento) {
		return getEstadoSiguiente(estado, evento, null);
	}

	/**
	 * Utilizada por los objeto Estado para, de forma desacoplada, obtener el estado siguiente.
	 * Si existe una condición de guarda para el evento, indica la condición de guarda que se ha cumplido.
	 * La guarda es una marca de texto que identifica la guarda (no vuelve a evaluarla). Esta marca para identificar
	 * la guarda sin tener que evaluarla se establece al definir la máquina y el estado.
	 */
	public FSMEstado getEstadoSiguiente(FSMEstado estado, FSMEvento evento, String guarda) {
		// TODO Utiliza el mapa de transiciones para seleccionar el siguiente estado
		// Obtiene estado siguiente, a partir de estado actual, evento (y si existe guarda), evento#guarda, para obtener el siguiente
		
		 // Temporal: único estado. Debería haber una factoría de singleton
		FSMIdEstado idEstado = (FSMIdEstado) mapaTransiciones.get( 
				generarClaveMapaTransiciones(estado.getId(), evento != null ? evento.getIdEvento() : null, guarda));
		// Si existe devuelve instancia de estado
		if (idEstado != null) // existe
			return idEstado.getInstance();
		else
			return null;
	}		

	public FSMEstado getEstadoActivo() {
		return estado;
	}
	
	public void setHilo(boolean esHilo) {
		this.esHilo = esHilo;
	}

	public boolean esHilo() {
		return esHilo;
	}

	/** 
	 * Realiza una transición en la máquina de estados. Es decir:
	 *    - Recoge un evento de la cola de evento
	 *    - Pasa el evento al estado actual (que realiza, en su caso teniendo en cuenta la condición de guarda, las acciones establecidas)
	 *    - Pasa a un nuevo estado.
	 *    
	 * Este método puede ser bloqueante si la cola de eventos estuviese vacía, en espera de un evento.
	 */
	public void doTransicion() {
		try { 
			// 1. Recoge(espera, si no existe) un evento
			FSMEvento evento = (FSMEvento)colaEventos.take();

			// 2. Descomponer en eventos
			switch((FSMIdEventoBase) evento.getIdEvento()) {
			default:
				throw new IllegalStateException("Id de evento desconocido.");
			}
		} catch (InterruptedException e1) {
			// TODO colaEventos ha generado una excepci�n...
			e1.printStackTrace();
		}
	}
	
	/**
	 * Cuando la máquina se ejecuta como un hilo, este es su programa principal
	 * Un bucle ejecutando transiciones, hasta llegar al estado final (idEstado == FIN)
	 */
	@Override
	public void run() {

		// TODO: Dudoso suponer que quien invoca a run lo hace con hilo... podría ponerse como método público set
		// y aquí verificar si no lo es, porque es bucle infinito... pero esto puede ser lo que realmente quieran.
		// así que en tal caso el concepto de esHilo es indiferente pues deja bloqueado el método llamante
		setHilo(true);

		// Avance en bucle de la máquina de estados, hasta alcanzar el estado final
		while (estado.getId() != FSMIdEstadoBase.FIN) {
			doTransicion();
		}
	}
			
}

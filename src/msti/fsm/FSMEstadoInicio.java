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
public class FSMEstadoInicio extends FSMEstado {
	/* Instancia el singleton */
	static {
		_instancia = new FSMEstadoInicio(FSMIdEstadoBase.INICIO);
	}

	protected FSMEstadoInicio (FSMIdEstado id) {
		super(id);
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	/**
	 * Pseudo-Evento inicial de arranque de la máquina de estados.
	 * Realiza la transición desde el pseudo-estado Inicio (punto gráfico) y el primer estado.
	 * Si hay acciones, se pueden poner en el método onSalida() del estado Inicio, en lugar de sobreescribir
	 * este método, pues el resultado es el mismo.
	 *
	 * @param contexto
	 * @return
	 */
	public FSMEstado procesarEventoInicio(FSMContexto contexto) {

		/** Obtiene el estado siguiente */
		FSMEstado estadoSiguiente = contexto.getMaquinaEstados().getEstadoSiguiente(this, null);

		/** Si esta transición cambia de estado, ejecuta método de salida del estado anterior */
		if (estadoSiguiente != this)
			this.onSalida(contexto);

		/** 
		 * No acción (introducir acciones en onSalida del estado Inicio para no tener que sobreescribir este método */

		/** Ejecuta método de entrada de estado anterior */
		if (estadoSiguiente != this)
			estadoSiguiente.onEntrada(contexto);

		/** Devuelve el siguiente estado */
		return estadoSiguiente;
	}

	/**
	 * Implementación por defecto del Inicio.
	 * 
	 * Para cualquier evento (puede ser null), obtiene de la máquina el estado inicial
	 */
	@Override
	public FSMEstado procesar(FSMContexto contexto, FSMEvento evento) {
		return contexto.getMaquinaEstados().getEstadoSiguiente(this);
	}
	
	
}

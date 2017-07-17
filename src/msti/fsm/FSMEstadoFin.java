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
public class FSMEstadoFin extends FSMEstado {
	/* Instancia el singleton */
	static {
		_instancia = new FSMEstadoFin(FSMIdEstadoBase.FIN);
	}

	protected FSMEstadoFin(FSMIdEstado id) {
		super();
	}

	public static FSMEstado getInstance() {
		return _instancia;
	}

	/**
	 * Implementación por defecto del Fin.
	 * 
	 * Para cualquier evento (puede ser null), genera un IllegalStateException, pues el evento final no 
	 * procesa eventos
	 */
	@Override
	public FSMEstado procesar(FSMContexto contexto, FSMEvento evento) {
		throw new IllegalStateException("El estado final no puede procesar eventos");
	}
	
	
}

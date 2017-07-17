/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;


/**
 * Acción. Implementa un patrón command. 
 * 
 * Para que pueda ser reutilizable, almacena sus variables globales en el contexto (tabla hash)
 */
public interface FSMAccion {
	/**
	 * Ejecuta una acción de la máquina de estados
	 * @param contexto  Contexto de la máquina de estados actual. Incluye tabla hash <String,Object> donde puede
	 * depositar/recuperar variables globales. De esta forma, la acción es reutilizable en otros protocolos.
	 * @param o  Objeto recibido asociado al evento que ha provocado la transición de la máquina de estados.
	 */
	public void execute(FSMContexto contexto, Object o);
	
}

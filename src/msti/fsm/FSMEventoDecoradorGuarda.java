/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;

/**
 * Decorador que incorpora a un evento, una guarda aplicada en una etapa previa, para que 
 * posteriores etapas puedan decidir en base a ambos elementos
 * 
 */
public class FSMEventoDecoradorGuarda extends FSMEventoDecorador {

	protected String guarda;
	
	public FSMEventoDecoradorGuarda(FSMEvento evento, String guarda) {
		super(evento);
		this.guarda = guarda;
	}
	public String getGuarda() {
		return this.guarda;
	}
}

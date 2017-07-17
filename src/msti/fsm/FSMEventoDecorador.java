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
public class FSMEventoDecorador extends FSMEvento {

	protected FSMEvento evento;
	
	public FSMEventoDecorador(FSMEvento evento) {
		this.evento  = evento;
	}
	public FSMIdEvento getIdEvento() {
		return evento.getIdEvento();
	}
	public Object getArgumento() {
		return evento.getArgumento();
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;

public class FSMEvento {

	protected FSMIdEvento idEvento;
	protected Object argumento;
	
	/** Eventos de la máquina Base */
	public enum FSMIdEventoBase implements FSMIdEvento {
	}
	
	/** Interfaz enum id Evento que todas las subclase deben cumplir para sus enum id Evento particular */
	public interface FSMIdEvento {
	}
	
	protected FSMEvento() {		
	}

	public FSMEvento(FSMIdEvento idEvento, Object argumento) {
		this.idEvento = idEvento;
		this.argumento = argumento;
	}
	public FSMIdEvento getIdEvento() {
		return this.idEvento;
	}

	public Object getArgumento() {
		return this.argumento;
	}

}

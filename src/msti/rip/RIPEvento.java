/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip;

public class RIPEvento extends java.util.EventObject {

	// Serializable
	private static final long serialVersionUID = 1L;

	private Object arg; //argumento
	private int tipo; //tipo de evento

	public static final int RIPEventoPDU = 1;
	public static final int RIPEventoTimer = 2;

	public RIPEvento(Object oOrigen, int tipo) {
		super(oOrigen);
		this.tipo = tipo;
	}

	public RIPEvento(Object oOrigen, int tipo, Object arg) {
		super(oOrigen);
		this.tipo = tipo;
		this.arg = arg;
	}

	public void setArg(Object arg) {
		this.arg = arg;
	}

	public Object getArg() {
		return arg;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public int getTipo() {
		return tipo;
	}

}

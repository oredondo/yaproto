package msti.ospfv2;


public class OSPFv2Evento extends java.util.EventObject {

	// Serializable
	private static final long serialVersionUID = 1L;

	private Object arg; //argumento
	private int tipo; //tipo de evento

	public static final int OSPFv2EventoPDU = 1;
	public static final int OSPFv2EventoTimer = 2;

	public OSPFv2Evento(Object oOrigen, int tipo) {
		super(oOrigen);
		this.tipo = tipo;
	}

	public OSPFv2Evento(Object oOrigen, int tipo, Object arg) {
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
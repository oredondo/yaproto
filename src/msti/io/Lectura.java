/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.InputStream;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class Lectura {

	protected Object mensajeOriginal; /* original entregado sin procesar por filtros */
	protected Object mensaje;

	protected InputStream inputStream; 

	protected long id;

	private static final AtomicLong generadorId = new AtomicLong(0);
	protected SocketAddress direccionOrigen;
	protected SocketAddress direccionLocal;

	protected boolean estaDecodificada = false;
	
	public enum Prioridad {
	    ALTA(3), MEDIA(2), BAJA(1);
	    private final int codigo;
	    private Prioridad (int codigo) { this.codigo = codigo; }
	    public int getCodigo() { return codigo; }
	}
	protected Prioridad prioridad;
	
	public Lectura() {
		id = generadorId.incrementAndGet();
		this.prioridad = Prioridad.MEDIA;		
		this.estaDecodificada = false;
	}

	public Lectura(Object mensaje) {
		this();
		this.mensaje = mensaje;
	}

	public Lectura(Object mensaje, Prioridad prioridad) {
		this(mensaje);
		this.prioridad = prioridad;
	}

	public SocketAddress getDireccionLocal() {
		return direccionLocal;
	}

	public void setDireccionLocal(SocketAddress direccionLocal) {
		this.direccionLocal = direccionLocal;
	}

	public void setMensaje(Object mensaje) {
		// Si el mensajeOriginal está vacío, este es el mensaje original
		if (this.mensajeOriginal == null)
			this.mensajeOriginal = mensaje;
		this.mensaje = mensaje;
	}

	public Object getMensaje() {
		return mensaje;
	}

	public Object getMensajeOriginal() {
		return mensajeOriginal;
	}

	public void setMensajeOriginal(Object mensajeOriginal) {
		this.mensajeOriginal = mensajeOriginal;
	}

	public long getId() {
		return id;
	}

	public Prioridad getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(Prioridad prioridad) {
		this.prioridad = prioridad;
	}

	/**
	 * Indica si la lectura ha sido decodificada alguna vez (ha pasado por algún MensajeCodec)
	 * @return cierto/falso
	 */
	public boolean estaDecodificada() {
		return estaDecodificada;
	}

	public void setDecodificada(boolean estaDecodificada) {
		this.estaDecodificada = estaDecodificada;
	}

	public void setDireccionOrigen(SocketAddress direccionOrigen) {
		this.direccionOrigen = direccionOrigen;
	}

	/**
	 * Proporciona la dirección origen del mensaje. 
	 * @return Dirección origen (socket) 
	 */
	public SocketAddress getDireccionOrigen() {
		return direccionOrigen;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public boolean isPrimerFragmento() {
		// TODO Auto-generated method stub
		return true;
	}

}

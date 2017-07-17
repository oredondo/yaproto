/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class Escritura {

	protected Object mensajeOriginal; /* original entregado sin procesar por filtros */
	protected Object mensaje;
	protected long id;
	protected Sesion sesion;
	protected OutputStream outputStream; 

	private static final AtomicLong generadorId = new AtomicLong(0);
	protected SocketAddress direccionDestino;

	protected boolean estaCodificada = false;
	
	public enum Prioridad {
	    ALTA(3), MEDIA(2), BAJA(1);
	    private final int codigo;
	    private Prioridad (int codigo){this.codigo = codigo;}
	    public int getCodigo(){return codigo;}
	}
	protected Prioridad prioridad;
	
	public Escritura(Object mensaje) {
		id = generadorId.incrementAndGet();
		this.prioridad = Prioridad.MEDIA;		

		this.mensaje = mensaje;
	}

	public Escritura(Object mensaje, Prioridad prioridad) {
		this(mensaje);
		this.prioridad = prioridad;
	}

	public Object getMensaje() {
		return mensaje;
	}

	public void setMensaje(Object mensaje) {
		this.mensaje = mensaje;
		
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
	 * Indica si la escritura ha sido codificada alguna vez (ha pasado por algún MensajeCodec)
	 * @return cierto/falso
	 */
	public boolean estaCodificada() {
		return estaCodificada;
	}

	public void setCodificada(boolean estaCodificada) {
		this.estaCodificada = estaCodificada;
	}

	public void setDireccionDestino(SocketAddress direccionDestino) {
		this.direccionDestino = direccionDestino;
	}

	/**
	 * Proporciona la dirección destino del mensaje. 
	 * @return Dirección destino (socket) o null para destino por defecto de la sesión
	 */
	public SocketAddress getDireccionDestino() {
		return direccionDestino;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void setSesion(Sesion sesion) {
		this.sesion = sesion;
	}

	public Sesion getSesion() {
		return sesion;
	}


}

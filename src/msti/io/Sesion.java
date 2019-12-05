/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import msti.ospfv2.ConfiguracionOSPFv2;

public class Sesion {
	
	private final SesionConfiguracion sesionConfiguracion;
	private Aceptador aceptador;

	/** Fecha de creaci�n de la sesi�n */
	protected final long fechaCreacion;
	
	/** Id de la sesi�n */
	protected long idSesion;
	
	/** Nombre de la sesión (lo recibe del aceptador que la creó */
	protected String nombre;
	
    /** Generador de id para generar ids �nicos para la sesi�n */
    private static AtomicLong generadorId = new AtomicLong(0);

	protected long bytesRecibidos;
	protected long bytesEnviados;
	protected long mensajesRecibidos;
	protected long mensajesEnviados;
	
	private Lector lector;
	private Escritor escritor;
	
	/** Modo del transporte: flujo(no estructurado), datagrama(estructurado) */
	protected final boolean esModoFlujo;
	

	public Sesion(Aceptador aceptador, SesionConfiguracion sesionConfiguracion, boolean esModoFlujo) {
		// Guarda aceptador
		this.setAceptador(aceptador);
		this.sesionConfiguracion = sesionConfiguracion;
		this.esModoFlujo = esModoFlujo;
		// Inicializa fechas
        fechaCreacion = System.currentTimeMillis();
        // Obtiene Id �nico
        idSesion = generadorId.incrementAndGet();
    }
	
	public synchronized long getId() {
		return idSesion;
	}
	
	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
/* 
	 * Forzar entrega de datos
	 */
	public void flush() {
		
	}

	public SesionConfiguracion getSesionConfiguracion() {
		return sesionConfiguracion;
	}

	public boolean getModoFlujo() {
		return esModoFlujo;
	}

	public void setAceptador(Aceptador aceptador) {
		this.aceptador = aceptador;
	}

	public Aceptador getAceptador() {
		return aceptador;
	}

	public Lector getLector() {
		return this.lector;
	}

	public Escritor getEscritor() {
		return this.escritor;
	}

	public void setLector(Lector lector) {
		this.lector = lector;
	}

	public void setEscritor(Escritor escritor) {
		this.escritor = escritor;
	}


	/*
	 * Obtener un outputstream sobre el que escribir
	 */
	public synchronized OutputStream getOutputStream() {
		// TODO
		throw new UnsupportedOperationException("No implementado el getOutputStream() de la sesion. Ir al getOutputStream del filtro anterior");
	}
	
	/**
	 * Escribe un mensaje saliente
	 * @param mensaje
	 * @throws UnknownHostException 
	 */
	public void escribir(Escritura escritura){

		// Configura el mensaje como original
		escritura.setMensajeOriginal(escritura.getMensaje());
		// mensaje no codificado
		escritura.estaCodificada = false;
		// Se lo pasa al Escritor que nos de el aceptador TODO: posible par�metro idSesion al getEscritor() para que seleccione entre varios...
		this.getAceptador().getEscritor().escribir(this, escritura);

		
	}


}
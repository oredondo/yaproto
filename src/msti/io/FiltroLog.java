/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiltroLog extends Filtro {

	/** logger */
	private final Logger logger;

	/* Niveles para cada evento */
	public enum NivelLog {
		TRACE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5); 

		private int codigo;
		NivelLog(int codigo) { this.codigo = codigo; }
		public int getCodigo() { return this.codigo; }
	};
	private NivelLog nivelMensajeRecibido;
	private NivelLog nivelExcepcionCapturadaLectura;
	private NivelLog nivelSesionCreada;
	private NivelLog nivelSesionInactiva;
	private NivelLog nivelSesionCerrada;
	private NivelLog nivelEscribir;
	private NivelLog nivelExcepcionCapturadaEscritura;

	/* Nivel m�nimo de log. Por debajo, no se imprime, ni llegan a construirse los mensajes de log */
	private NivelLog nivelLogMinimo;

	public FiltroLog(String nombre) {
		super(nombre);

		// Instancia un logger para este filtro
        logger = LoggerFactory.getLogger(this.getNombre());
        
        // Niveles por defecto
        nivelMensajeRecibido = NivelLog.INFO;     
        nivelExcepcionCapturadaLectura = NivelLog.WARN;     
        nivelSesionCreada = NivelLog.INFO;     
        nivelSesionInactiva = NivelLog.INFO;     
        nivelSesionCerrada = NivelLog.INFO;     
        nivelEscribir = NivelLog.INFO;     
        nivelExcepcionCapturadaEscritura = NivelLog.WARN;     
        
        // Nivel m�nimo de log
        nivelLogMinimo = NivelLog.INFO;
	}

	@Override
	public int getMaxInputBytes() {
		// TODO: Relacionar al Filtro con la futura cadenaFiltros, y solicitar al filtro siguiente esta operaci�n
		throw new UnsupportedOperationException("getMaxInputBytes() en FiltroLog no soportada");
	}

	@Override
	public void init() {
		log(NivelLog.DEBUG, "Init FiltroLog");
	}

	@Override
	public void destroy() {
		log(NivelLog.DEBUG, "Destroy FiltroLog");
	}

    /**
     * 
     * Hace log de un mensaje y una excepci�n, con un determinado nivel
     * 
     * @param nivelLog Nivel de log solicitado
     * @param mensaje Cadena de texto
     * @param e Excepci�n
     */
	private void log(NivelLog nivelLog, String mensaje, Throwable e) {
		if (nivelLog.getCodigo() > this.nivelLogMinimo.getCodigo())
			switch (nivelLog) {
			case TRACE: 
				logger.trace(mensaje, e); return;
			case DEBUG: 
				logger.debug(mensaje, e); return;
			case INFO: 
				logger.info(mensaje, e); return;
			case WARN: 
				logger.warn(mensaje, e); return;
			case ERROR: 
				logger.error(mensaje, e); return;
			default: 
				return;
			}
	}

    /**
     * 
     * Hace log de un mensaje , con un determinado nivel
     * 
     * @param nivelLog nivel de log solicitado por el usuario
     * @param mensaje 
     */
	private void log(NivelLog nivelLog, String mensaje) {
		if (nivelLog.getCodigo() >= this.nivelLogMinimo.getCodigo())
			switch (nivelLog) {
			case TRACE: 
				logger.trace(mensaje); return;
			case DEBUG: 
				logger.debug(mensaje); return;
			case INFO: 
				logger.info(mensaje); return;
			case WARN: 
				logger.warn(mensaje); return;
			case ERROR: 
				logger.error(mensaje); return;
			default: 
				return;
			}
	}

    /**
     * 
     * Hace log de un mensaje formateado, con argumento como par�metro, con un determinado nivel
     * 
     * @param nivelLog nivel de log solicitado por el usuario
     * @param mensaje Mensaje de texto
     * @param Object Argumento
     */
	private void log(NivelLog nivelLog, String mensaje, Object argumento) {
    	if (nivelLog.getCodigo() > this.nivelLogMinimo.getCodigo())
    		switch (nivelLog) {
    		case TRACE: 
    			logger.trace(mensaje, argumento); return;
    		case DEBUG: 
    			logger.debug(mensaje, argumento); return;
    		case INFO: 
    			logger.info(mensaje, argumento); return;
    		case WARN: 
    			logger.warn(mensaje, argumento); return;
    		case ERROR: 
    			logger.error(mensaje, argumento); return;
    		default: 
    			return;
    		}
    }

	public void setNivelLogMinimo(NivelLog nivelLogMinimo) {
		this.nivelLogMinimo = nivelLogMinimo;
	}

	public NivelLog getNivelLogMinimo() {
		return nivelLogMinimo;
	}

	public NivelLog getNivelMensajeRecibido() {
		return nivelMensajeRecibido;
	}

	public void setNivelMensajeRecibido(NivelLog nivelMensajeRecibido) {
		this.nivelMensajeRecibido = nivelMensajeRecibido;
	}

	public NivelLog getNivelExcepcionCapturadaLectura() {
		return nivelExcepcionCapturadaLectura;
	}

	public void setNivelExcepcionCapturadaLectura(
			NivelLog nivelExcepcionCapturadaLectura) {
		this.nivelExcepcionCapturadaLectura = nivelExcepcionCapturadaLectura;
	}

	public NivelLog getNivelSesionCreada() {
		return nivelSesionCreada;
	}

	public void setNivelSesionCreada(NivelLog nivelSesionCreada) {
		this.nivelSesionCreada = nivelSesionCreada;
	}

	public NivelLog getNivelSesionInactiva() {
		return nivelSesionInactiva;
	}

	public void setNivelSesionInactiva(NivelLog nivelSesionInactiva) {
		this.nivelSesionInactiva = nivelSesionInactiva;
	}

	public NivelLog getNivelSesionCerrada() {
		return nivelSesionCerrada;
	}

	public void setNivelSesionCerrada(NivelLog nivelSesionCerrada) {
		this.nivelSesionCerrada = nivelSesionCerrada;
	}

	public NivelLog getNivelEscribir() {
		return nivelEscribir;
	}

	public void setNivelEscribir(NivelLog nivelEscribir) {
		this.nivelEscribir = nivelEscribir;
	}

	public NivelLog getNivelExcepcionCapturadaEscritura() {
		return nivelExcepcionCapturadaEscritura;
	}

	public void setNivelExcepcionCapturadaEscritura(
			NivelLog nivelExcepcionCapturadaEscritura) {
		this.nivelExcepcionCapturadaEscritura = nivelExcepcionCapturadaEscritura;
	}

	
	public void sesionCreada(Sesion sesion) {
		log(this.nivelSesionCreada, "FiltroLog:sesionCreada() Id={}", sesion.getId());
		if (sesion instanceof SesionDatagrama) {
			SesionDatagrama _sesion = (SesionDatagrama)sesion;
			log(this.nivelSesionCreada, "      Remoto={}", _sesion.getSocket().getRemoteSocketAddress());
		}
		super.sesionCreada(sesion);
	}
	
	public  void sesionInactiva(Sesion sesion) {
		log(this.nivelSesionInactiva, "Sesion inactiva");
		super.sesionInactiva(sesion);
	}	
	public  void sesionCerrada(Sesion sesion) {	
		log(this.nivelSesionCerrada, "Sesion cerrada");
		super.sesionCerrada(sesion);
	}

	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		log(this.nivelMensajeRecibido, "FiltroLog:mensajeRecibido(): {}", lectura.getMensaje().toString() );
		return super.mensajeRecibido(sesion, lectura);
	}

	public  void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) { 
		log(this.nivelMensajeRecibido, "Excepción capturada en lectura", e);
		super.excepcionCapturada(sesion, lectura, e);
	}

	/** 
	 * Escritura
	 */
	public  void escribir(Sesion sesion, Escritura escritura) {
		log(this.nivelEscribir, "FiltroLog:escribir(): {}", escritura.getMensaje().toString());
		super.escribir(sesion, escritura);
	}

	/* TODO: A�adir excepcionCapturadaEscritura a la cadena de filtros */
/*	public  void excepcionCapturada(Sesion sesion, Escritura escritura, Throwable e) { 
		log(this.nivelMensajeRecibido, "Excepci�n capturada en escritura", e);
		super.excepcionCapturada(sesion, escritura, e);
	}
*/

}

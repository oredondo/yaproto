/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class FiltroMQTT extends Filtro {

	/** logger */
	private MqttClient logger;

	/* Niveles para cada evento */
	public enum NivelLogMQTT {
		TRACE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5);

		private int codigo;
		NivelLogMQTT(int codigo) { this.codigo = codigo; }
		public int getCodigo() { return this.codigo; }
	};
	private NivelLogMQTT nivelMensajeRecibido;
	private NivelLogMQTT nivelExcepcionCapturadaLectura;
	private NivelLogMQTT nivelSesionCreada;
	private NivelLogMQTT nivelSesionInactiva;
	private NivelLogMQTT nivelSesionCerrada;
	private NivelLogMQTT nivelEscribir;
	private NivelLogMQTT nivelExcepcionCapturadaEscritura;
	private MqttMessage message;
	private MqttMessage message2;
	private MqttMessage message3;

	/* Nivel m�nimo de log. Por debajo, no se imprime, ni llegan a construirse los mensajes de log */
	private NivelLogMQTT nivelLogMinimo;

	public FiltroMQTT(String nombre, MqttClient mqtt) {
		super(nombre);
		logger = mqtt;
		// Instancia un logger para este filtro
        // Niveles por defecto
    nivelMensajeRecibido = NivelLogMQTT.INFO;
    nivelExcepcionCapturadaLectura = NivelLogMQTT.WARN;
    nivelSesionCreada = NivelLogMQTT.INFO;
    nivelSesionInactiva = NivelLogMQTT.INFO;
    nivelSesionCerrada = NivelLogMQTT.INFO;
    nivelEscribir = NivelLogMQTT.INFO;
    nivelExcepcionCapturadaEscritura = NivelLogMQTT.WARN;

    // Nivel m�nimo de log
    nivelLogMinimo = NivelLogMQTT.INFO;
	}

	@Override
	public int getMaxInputBytes() {
		// TODO: Relacionar al Filtro con la futura cadenaFiltros, y solicitar al filtro siguiente esta operaci�n
		throw new UnsupportedOperationException("getMaxInputBytes() en FiltroLog no soportada");
	}

	@Override
	public void init() {
		log(NivelLogMQTT.DEBUG, "Init_FiltroLog");
	}

	@Override
	public void destroy() {
		log(NivelLogMQTT.DEBUG, "Destroy_FiltroLog");
		try{
			logger.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
	}

    /**
     *
     * Hace log de un mensaje y una excepci�n, con un determinado nivel
     *
     * @param nivelLog Nivel de log solicitado
     * @param mensaje Cadena de texto
     * @param e Excepci�n
     */
	private void log(NivelLogMQTT nivelLog, String mensaje, Throwable e) {
		if (nivelLog.getCodigo() > this.nivelLogMinimo.getCodigo()){
				try {
				message = new MqttMessage((mensaje + e.toString()).getBytes());
				switch (nivelLog) {
    		case TRACE:
    			logger.publish("TRACE", message); return;
    		case DEBUG:
    			logger.publish("DEBUG", message); return;
    		case INFO:
    			logger.publish("INFO", message); return;
    		case WARN:
    			logger.publish("WARN", message); return;
    		case ERROR:
    			logger.publish("ERROR", message); return;
    		default:
    			return;
    		}

			} catch (MqttException ex) {
						ex.printStackTrace();
					}
			}
	}

    /**
     *
     * Hace log de un mensaje , con un determinado nivel
     *
     * @param nivelLog nivel de log solicitado por el usuario
     * @param mensaje
     */
	private void log(NivelLogMQTT nivelLog, String mensaje) {
		if (nivelLog.getCodigo() >= this.nivelLogMinimo.getCodigo()){
				try {
					message2 = new MqttMessage(mensaje.getBytes());
					switch (nivelLog) {
	    		case TRACE:
	    			logger.publish("TRACE", message2); return;
	    		case DEBUG:
	    			logger.publish("DEBUG", message2); return;
	    		case INFO:
	    			logger.publish("INFO", message2); return;
	    		case WARN:
	    			logger.publish("WARN", message2); return;
	    		case ERROR:
	    			logger.publish("ERROR", message2); return;
	    		default:
	    			return;
	    		}
					} catch (MqttException e) {
							e.printStackTrace();
				}
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
	private void log(NivelLogMQTT nivelLog, String mensaje, Object argumento) {
		if( argumento == null ){
			return;
		}
    	if (nivelLog.getCodigo() > this.nivelLogMinimo.getCodigo()){
				try {
					message3 = new MqttMessage((mensaje +argumento.toString()).getBytes());
					switch (nivelLog) {
	    		case TRACE:
	    			logger.publish("TRACE", message3); return;
	    		case DEBUG:
	    			logger.publish("DEBUG", message3); return;
	    		case INFO:
	    			logger.publish("INFO", message3); return;
	    		case WARN:
	    			logger.publish("WARN", message3); return;
	    		case ERROR:
	    			logger.publish("ERROR", message3); return;
	    		default:
	    			return;
	    		}
					} catch (MqttException e) {
							e.printStackTrace();
				}
			}
    }

	public void setNivelLogMinimo(NivelLogMQTT nivelLogMinimo) {
		this.nivelLogMinimo = nivelLogMinimo;
	}

	public NivelLogMQTT getNivelLogMinimo() {
		return nivelLogMinimo;
	}

	public NivelLogMQTT getNivelMensajeRecibido() {
		return nivelMensajeRecibido;
	}

	public void setNivelMensajeRecibido(NivelLogMQTT nivelMensajeRecibido) {
		this.nivelMensajeRecibido = nivelMensajeRecibido;
	}

	public NivelLogMQTT getNivelExcepcionCapturadaLectura() {
		return nivelExcepcionCapturadaLectura;
	}

	public void setNivelExcepcionCapturadaLectura(
			NivelLogMQTT nivelExcepcionCapturadaLectura) {
		this.nivelExcepcionCapturadaLectura = nivelExcepcionCapturadaLectura;
	}

	public NivelLogMQTT getNivelSesionCreada() {
		return nivelSesionCreada;
	}

	public void setNivelSesionCreada(NivelLogMQTT nivelSesionCreada) {
		this.nivelSesionCreada = nivelSesionCreada;
	}

	public NivelLogMQTT getNivelSesionInactiva() {
		return nivelSesionInactiva;
	}

	public void setNivelSesionInactiva(NivelLogMQTT nivelSesionInactiva) {
		this.nivelSesionInactiva = nivelSesionInactiva;
	}

	public NivelLogMQTT getNivelSesionCerrada() {
		return nivelSesionCerrada;
	}

	public void setNivelSesionCerrada(NivelLogMQTT nivelSesionCerrada) {
		this.nivelSesionCerrada = nivelSesionCerrada;
	}

	public NivelLogMQTT getNivelEscribir() {
		return nivelEscribir;
	}

	public void setNivelEscribir(NivelLogMQTT nivelEscribir) {
		this.nivelEscribir = nivelEscribir;
	}

	public NivelLogMQTT getNivelExcepcionCapturadaEscritura() {
		return nivelExcepcionCapturadaEscritura;
	}

	public void setNivelExcepcionCapturadaEscritura(
			NivelLogMQTT nivelExcepcionCapturadaEscritura) {
		this.nivelExcepcionCapturadaEscritura = nivelExcepcionCapturadaEscritura;
	}


	public void sesionCreada(Sesion sesion) {
		log(this.nivelSesionCreada, "FiltroLog:sesionCreada()_Id=", sesion.getId());
		if (sesion instanceof SesionDatagrama) {
			SesionDatagrama _sesion = (SesionDatagrama)sesion;
			log(this.nivelSesionCreada, "Remoto=", _sesion.getSocket().getRemoteSocketAddress());
		}
		super.sesionCreada(sesion);
	}

	public  void sesionInactiva(Sesion sesion) {
		log(this.nivelSesionInactiva, "Sesion_inactiva");
		super.sesionInactiva(sesion);
	}
	public  void sesionCerrada(Sesion sesion) {
		log(this.nivelSesionCerrada, "Sesion_cerrada");
		super.sesionCerrada(sesion);
	}

	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		log(this.nivelMensajeRecibido, "FiltroLog:mensajeRecibido():", lectura.getMensaje().toString() );
		return super.mensajeRecibido(sesion, lectura);
	}

	public  void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		log(this.nivelMensajeRecibido, "Excepciona", e);
		super.excepcionCapturada(sesion, lectura, e);
	}

	/**
	 * Escritura
	 */
	public  void escribir(Sesion sesion, Escritura escritura) {
		log(this.nivelEscribir, "FiltroLog:escribir():", escritura.getMensaje().toString());
		super.escribir(sesion, escritura);
	}

	/* TODO: A�adir excepcionCapturadaEscritura a la cadena de filtros */
/*	public  void excepcionCapturada(Sesion sesion, Escritura escritura, Throwable e) {
		log(this.nivelMensajeRecibido, "Excepci�n capturada en escritura", e);
		super.excepcionCapturada(sesion, escritura, e);
	}
*/

}

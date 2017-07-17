/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class ConectadorDatagrama extends Aceptador {

	protected DatagramSocket socket; //socketAceptador (es el �nico)

	protected boolean isMulticast;
	
	protected Thread hiloEscritor;
	
	public ConectadorDatagrama() {
		super();
		// Instancia un lector y un escritor datagrama
		this.lector = new LectorDatagrama();
		this.escritor = new EscritorDatagrama();
		isMulticast = false;
	}

	public Runnable getHiloEscritor(Sesion sesion) {
		return hiloEscritor;
	}

	public Runnable getHiloLector(Sesion sesion) {
		return this;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	/**
	 * Asocia el socket a una direccion/puerto local.
	 * @param address Direccion/puerto local. Si es null, el sistema elige un puerto libre.
	 * @throws SocketException
	 */
	public void bind(SocketAddress address) throws SocketException{
		this.socket = new DatagramSocket(address);		
	}

	public boolean isMulticast() {
		return isMulticast;
	}

	@Override
	public void run() {
		// Verifica bind()
		if (this.socket == null) {
			throw new IllegalStateException("Ejecutado run() en ConectadorDatagrama, sin realizar bind()");
		}
		// Verifica filtros
		if (this.getCadenaFiltros().size() == 0)
			//TODO: quiz� cadenaFiltros podr�a a�adir siempre el filtro final (notificador)
			throw new IllegalStateException("Ejecutado run() en ConectadorDatagrama, con cadenafiltros vac�a");

		// Propaga evento Inicializar a los filtros
		for (Filtro filtro: this.getCadenaFiltros())
			filtro.init();

		// Lanza un escritor (hilo aparte)
		escritor.setAceptador(this);
		((EscritorDatagrama)escritor).setSocket(socket);
		hiloEscritor = new Thread(escritor);
		hiloEscritor.start();

		// En TCP ser�a accept y para cada socket generado, generar nuevo par Sesion/Lector e invocar a sesionCreada() de cadena de filtros
		// En UDP esto s�lo se hace una vez, y para aprovechar el hilo
		
		// Instancia una configuración que usará para todas las sesiones como base
		this.setSesionConfiguracion(new SesionConfiguracion());
		
		// Instancia una sesión (al ser datagrama, una única sesión)
		SesionDatagrama sesion = new SesionDatagrama(this, this.getSesionConfiguracion());		
		
		// Configura el lector
		sesion.setLector(lector);
		sesion.setEscritor(escritor);
		sesion.setSocket(socket);

		// Asigna la sesión a un lector (la única sesión al único lector existente)
		lector.setAceptador(this);
		lector.setSesion(sesion); //TODO: Podr�a dejarse sin configurar, almacenarla el aceptador, y dejar que el lector la consulte para adjuntarla a la nueva Lectura
		((LectorDatagrama)lector).setSocket(socket);  //TODO: lector cuando se crea no tiene sesion de la que sacarlo

		// Propaga evento sesionCreada a los filtros
		this.getCadenaFiltros().getFirst().sesionCreada(sesion);

		// No crea hilo aparte para el lector, dado que el aceptadorDatagrama no requiere m�s actividad, 
		// as� que ejecuta el lector.run() en el hilo actual
		lector.run();  
	}
	
}

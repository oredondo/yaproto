/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.IOException;
import java.net.SocketException;

import msti.util.RawSocketNetlink;
import msti.util.RawSocketNetlink.NetlinkAddress;


public class AceptadorRawSocketNetlink extends Aceptador {

	public static final int TIMEOUT = 0;
	protected RawSocketNetlink socket;
	private Thread hiloEscritor;

	public AceptadorRawSocketNetlink() {
		super();
		// Instancia un lector y un escritor datagrama
		this.lector = new LectorRawSocketNetlink();
		this.escritor = new EscritorRawSocketNetlink();
	}
	
	public void crear(int familiaProtocolos, int protocolo) throws SocketException {
		try {
			this.socket = new RawSocketNetlink();
			socket.open(familiaProtocolos, protocolo);

			try {
				socket.setSendTimeout(TIMEOUT);
				socket.setReceiveTimeout(TIMEOUT);
			} catch(SocketException e) {
				socket.setUseSelectTimeout(true);
				socket.setSendTimeout(TIMEOUT);
				socket.setReceiveTimeout(TIMEOUT);
			}
		}
		catch(IOException e) {
			throw new SocketException(e.getMessage());
		}
		
	}
	
	public void bind(NetlinkAddress socketAddress) throws IOException {
//		if (socketAddress instanceof InetSocketAddress)
		socket.bind(socketAddress);
//		else if (socketAddress instanceof RawSocketAddress) 
//			socket.bind(((RawSocketAddress)socketAddress).toByteArray());
	}

	@Override
	public void run() {
		// Verifica bind()
		if (this.socket == null) {
			throw new IllegalStateException("Ejecutado run() en AceptadorRawSocketNetlink, sin realizar bind()");
		}
		// Verifica filtros
		if (this.getCadenaFiltros().size() == 0)
			//TODO: quiz� cadenaFiltros podr�a a�adir siempre el filtro final (notificador)
			throw new IllegalStateException("Ejecutado run() en AceptadorRawSocketNetlink, con cadenafiltros vac�a");

		// Propaga evento Inicializar a los filtros
		for (Filtro filtro: this.getCadenaFiltros())
			filtro.init();

		// Lanza un escritor (hilo aparte)
		escritor.setAceptador(this);
		((EscritorRawSocketNetlink)escritor).setSocket(socket);
		hiloEscritor = new Thread(escritor);
		hiloEscritor.start();

		// En TCP ser�a accept y para cada socket generado, generar nuevo par Sesion/Lector e invocar a sesionCreada() de cadena de filtros
		// En UDP esto s�lo se hace una vez, y para aprovechar el hilo
		
		// Instancia una configuraci�n que usar� para todas las sesiones como base
		this.setSesionConfiguracion(new SesionConfiguracion());
		
		// Instancia una sesi�n (al ser datagrama, una �nica sesi�n)
		SesionRawSocketNetlink sesion = new SesionRawSocketNetlink(this, this.getSesionConfiguracion());		
		sesion.setNombre(this.getNombre()); // pasa el nombre asignado

		// Configura el lector
		sesion.setLector(lector);
		sesion.setEscritor(escritor);
		sesion.setSocket(socket);

		// Asigna la sesi�n a un lector (la �nica sesi�n al �nico lector existente)
		lector.setAceptador(this);
		lector.setSesion(sesion); //TODO: Podr�a dejarse sin configurar, almacenarla el aceptador, y dejar que el lector la consulte para adjuntarla a la nueva Lectura
		((LectorRawSocketNetlink)lector).setSocket(socket);  //TODO: lector cuando se crea no tiene sesion de la que sacarlo

		// Propaga evento sesionCreada a los filtros
		this.getCadenaFiltros().getFirst().sesionCreada(sesion);

		// No crea hilo aparte para el lector, dado que el aceptadorDatagrama no requiere m�s actividad, 
		// as� que ejecuta el lector.run() en el hilo actual
		lector.run();  
	}

}

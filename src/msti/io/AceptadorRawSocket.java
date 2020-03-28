/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.savarese.rocksaw.net.RawSocket;

public class AceptadorRawSocket extends Aceptador {

	public static final int TIMEOUT = 0;
	protected RawSocket socket;
	protected boolean isMulticast;
	protected int familiaProtocolos = 0;
	protected int protocolo = 0;
	private Thread hiloEscritor;
	private InetAddress dirIPAceptador;
	
	
	/** 
	 * No se usa un CopyOnWriteArrayList porque esta configuraci�n se realiza antes de poner en marcha el aceptador
	 * sin concurrencia.
	 */
	protected final ArrayList<InetAddress> grupos;
	
	public AceptadorRawSocket() {
		super();
		// Instancia un lector y un escritor sobre rawsocket
		this.lector = new LectorRawSocket();
		this.escritor = new EscritorRawSocket();
		
		grupos = new ArrayList<InetAddress>();
		isMulticast = false; //marca como AceptadorDatagrama de tipo multicast	
	}
	
	public void crear(int familiaProtocolos, int protocolo) throws SocketException {
		try {
			this.socket = new RawSocket();
			socket.open(familiaProtocolos, protocolo);
			this.familiaProtocolos = familiaProtocolos;
			this.protocolo = protocolo;

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
	
	public void bind(InetAddress inetAddress) throws IOException {
//		if (socketAddress instanceof InetSocketAddress)
		if (socket==null){
			System.out.println("AceptadorRawSocket.bind: socket es null");
		}else{
			System.out.println("AceptadorRawSocket.bind: socket no es null");
		}
		socket.bind(inetAddress);
		this.setDirIPAceptador(inetAddress);
//		else if (socketAddress instanceof RawSocketAddress) 
//			socket.bind(((RawSocketAddress)socketAddress).toByteArray());
	}

	public void unirGrupo(InetAddress grupo) throws IOException {
		this.socket.joinGroup(grupo, InetAddress.getByAddress(new byte[] {0,0,0,0}));  //0: Let the system choose the interface.
		grupos.add(grupo);
	}

	public void abandonarGrupo(InetAddress grupo) throws IOException {
		// TODO: leaveGroup en rawSocket
		throw new UnsupportedOperationException("No disponible leaveGroup en rawsocket por el momento");
		/*
		 * this.socket.leaveGroup(grupo);
		 
		
		Iterator<InetAddress> iterador = grupos.iterator();
		while (iterador.hasNext()) {
		    if (Arrays.equals(((InetAddress)iterador.next()).getAddress(), grupo.getAddress()))
		    		iterador.remove();  // �nica forma segura de borrar mientras se itera :)
		}
		*/				
	}
	
	public boolean isMulticast(){
		return isMulticast;
	}
	
	public ArrayList<InetAddress> getGrupos(){
		return grupos;
	}
	
	public int getFamiliaProtocolos() { return familiaProtocolos; }
	public int getProtocolo() { return protocolo; }
	
	@Override
	public void run() {
		// Verifica bind()
		if (this.socket == null) {
			throw new IllegalStateException("Ejecutado run() en AceptadorRawSocket, sin realizar bind()");
		}
		// Verifica filtros
		if (this.getCadenaFiltros().size() == 0)
			//TODO: quiz� cadenaFiltros podr�a a�adir siempre el filtro final (notificador)
			throw new IllegalStateException("Ejecutado run() en AceptadorRawSocket, con cadenafiltros vac�a");

		// Propaga evento Inicializar a los filtros
		for (Filtro filtro: this.getCadenaFiltros())
			filtro.init();

		// Lanza un escritor (hilo aparte)
		escritor.setAceptador(this);
		((EscritorRawSocket)escritor).setSocket(socket);
		hiloEscritor = new Thread(escritor);
		hiloEscritor.start();

		// En TCP ser�a accept y para cada socket generado, generar nuevo par Sesion/Lector e invocar a sesionCreada() de cadena de filtros
		// En UDP esto s�lo se hace una vez, y para aprovechar el hilo
		
		// Instancia una configuraci�n que usar� para todas las sesiones como base
		this.setSesionConfiguracion(new SesionConfiguracion());
		
		// Instancia una sesi�n (al ser datagrama, una �nica sesi�n)
		SesionRawSocket sesion = new SesionRawSocket(this, this.getSesionConfiguracion());		
		sesion.setNombre(this.getNombre()); // pasa el nombre asignado

		// Configura el lector
		sesion.setLector(lector);
		sesion.setEscritor(escritor);
		sesion.setSocket(socket);

		// Asigna la sesi�n a un lector (la �nica sesi�n al �nico lector existente)
		lector.setAceptador(this);
		lector.setSesion(sesion); //TODO: Podr�a dejarse sin configurar, almacenarla el aceptador, y dejar que el lector la consulte para adjuntarla a la nueva Lectura
		((LectorRawSocket)lector).setSocket(socket);  //TODO: lector cuando se crea no tiene sesion de la que sacarlo

		// Propaga evento sesionCreada a los filtros
		this.getCadenaFiltros().getFirst().sesionCreada(sesion);

		// No crea hilo aparte para el lector, dado que el aceptadorDatagrama no requiere m�s actividad, 
		// as� que ejecuta el lector.run() en el hilo actual
		lector.run();  
	}

	public InetAddress getDirIPAceptador() {
		return dirIPAceptador;
	}

	public void setDirIPAceptador(InetAddress dirIPAceptador) {
		this.dirIPAceptador = dirIPAceptador;
	}

}

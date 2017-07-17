/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class EscritorDatagrama extends Escritor {

	private DatagramSocket socket;

	// Si nos solicitan output  
	// TODO: lo deber�amos incluir en la escritura que ha usado el output y no usar el mensaje?
	private boolean solicitadoOutputStream;
	private ByteArrayOutputStream baos;  // outputStream proporcionado

	public EscritorDatagrama() {
		super();
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(java.net.DatagramSocket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		Escritura escritura;

		// Verifica que hay socket asignado
		if (this.socket == null) 
			throw new IllegalArgumentException("Invocado run() en Escritor sin previamente asignar un socket por donde enviar");


		//  Obtiene el �ltimo filtro de la cadena asociada a la sesi�n
		Filtro filtro = this.getAceptador().getCadenaFiltros().getLast();

		while (true) {
			try {
				// Espera (bloquea) hasta nueva escritura en la cola
				escritura = this.colaEscrituras.take();

				// Se lo pasa al �ltimo filtro (TODO: pasarlo a cadena y que gestione ella la entrega y los saltos)
				filtro.escribir(escritura.getSesion(), escritura);


			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// TODO IEscritorListener para indicar op realizada (idEscritura), op cancelada por errores???
				// que el �ltimo filtro se suscriba? que la aplicaci�n se suscriba?
			}
		}
		
	}

	@Override
	public OutputStream getOutputStream(Sesion sesion, int numBytesEscritura) {
		// TODO: instanciar... o reutilizar (por si invocan dos veces a getOutputStream...?)
		if (baos == null)
			baos = new ByteArrayOutputStream(); // TODO: numBytesEscritura no es necesario al escribir
			
		solicitadoOutputStream = true;
		return baos;
	}

	@Override
	public void escribir(Sesion sesion, Escritura escritura) {
		byte[] bufer;

		// Obtiene el bufer a enviar
		if (solicitadoOutputStream) {
			bufer = baos.toByteArray();
			solicitadoOutputStream = false;
			baos.reset(); // reutiliza el baos para la próxima escritura
		}
		else if (escritura.mensaje instanceof byte[]) {
			bufer = (byte[])escritura.mensaje;
		}
		else {
			// No deber�a ocurrir. Usa toString y a continuaci�n env�a esto como byte[]
			bufer = escritura.mensaje.toString().getBytes();
		}
		
		// Construye datagrama
		DatagramPacket datagrama = new DatagramPacket(bufer, bufer.length);
		datagrama.setSocketAddress(escritura.getDireccionDestino());  // direccion destino + puerto

 		try {
			System.out.println("<---- Envío: " + datagrama.getLength() + " octetos. Destino:" + escritura.getDireccionDestino().toString());
 			// Env�o del datagrama
			socket.send(datagrama);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Invocar a propagar excepci�n por filtro
		}
	}
	
	@Override
	public void escribirAsincrono(Sesion sesion, Escritura escritura) {
		escritura.setSesion(sesion); //Asegura enlace
		try {
			this.colaEscrituras.put(escritura);
		} catch (InterruptedException e) {
			// TODO Pasar excepcion a la cadena o reanudar intento
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void escribirAsincrono(Sesion sesion, 
			Escritura escritura, IEscrituraListener escrituraListener) {
		// TODO: Almacenar el listener en un mapa indexado por idEscritura <Integer, IEscrituraListener> 
		// Puede ser viable poner un addEscrituraListener por si hay m�s interesados y convertirlo en <Integer,CopyOnWriteArrayList<IEscrituraListener>> 
		throw new UnsupportedOperationException("escribirAsincrono() no implementado");
		
	}

	@Override
	public void escribirSincrono(SesionDatagrama sesionDatagrama,
			Escritura escritura, boolean canalAsincrono,
			IEscrituraListener escrituraListener) {
		// TODO: Almacenar un mutex (vale un object con synchronized) en un mapa indexado por idEscritura.
		throw new UnsupportedOperationException("escribirSincrono() no implementado");		
	}
	

}

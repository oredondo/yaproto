/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicInteger;

public class LectorDatagrama extends Lector {

	private static int numRecv = 0;
	private DatagramSocket socket;
	
	/* inputstream temporal sobre bufer que crea si se le requiere */
	private ByteArrayInputStream bais;


	private byte[] bufer; // Lectura en construcci�n
	
	public LectorDatagrama() {
		super();
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	/**
	 * Espera bloqueante en lectura de datagramas desde el socket asociado a la sesion.
	 * Para cada datagrama genera un objeto Lectura (si la anterior Lectura estaba completada), e invoca al primer
	 * filtro de la cadena.
	 */
	public void run() {
	
		boolean lecturaAnteriorFinalizada = true;
		DatagramPacket datagrama;  //datagrama que recibe
		Lectura lectura = null;

		// Verifica que hay sesi�n asignada
		if (this.socket == null) 
			throw new IllegalArgumentException("Invocado run() en Lector sin asignar un socket previamente");
		//socket = ((SesionDatagrama)this.getSesion()).getSocket();
		
		try {
			while (true) {
				// Crea una nueva lectura o TODO: reutiliza a anterior si era lectura parcial
				if (lecturaAnteriorFinalizada) {
					lectura = new Lectura();
				}	

				// TODO: new y pasar byte[] nuevo, o reutilizar byte[]?
				bufer = new byte[this.getAceptador().getCadenaFiltros().getFirst().getMaxInputBytes()]; //TODO:aqu� o filtro siguiente?

				datagrama = new DatagramPacket(bufer, bufer.length);
				this.socket.receive(datagrama); // espera bloqueante

				System.out.println("LectorDatagrama:run()---> receive() -> " + numRecv++ + " " + datagrama.getLength() + " octetos. Origen:" + datagrama.getAddress());
		
				// TODO: si no se hace sesi�n nueva en base al origen (en datagrama), la segunda lectura puede
				// venir de otro origen diferente �generar sesi�n diferente por par origen-destino en datagrama?
				// Construye una lectura
				lectura.setDireccionOrigen(datagrama.getSocketAddress());
				lectura.setDireccionLocal(socket.getLocalSocketAddress());
				lectura.setMensaje(datagrama.getData());
				lectura.setDecodificada(false);
				bais = new ByteArrayInputStream(bufer, 0, datagrama.getLength());
				lectura.setInputStream(bais);

				// Entrega la lectura a todos los filtros. Si alg�n filtro devuelve false, se detiene y vuelve a 
				// suministrar m�s datos a la misma lectura. Comienza de nuevo por principio de filtro.
				lecturaAnteriorFinalizada = true;
				for (Filtro filtro : this.getAceptador().getCadenaFiltros()) {				
					lecturaAnteriorFinalizada = filtro.mensajeRecibido(getSesion(), lectura); // TODO: Si un lector procesar m�ltiples sesiones, s�lo tendr�a que solicitar la sesi�n al aceptador en base a su id y que las almacene quien las cree (el aceptador)
					if (lecturaAnteriorFinalizada) {
						bais = null;  //libera el posible inputstream
						break; // Interrumpe la cadena de filtros
					}		
				}
			}
		} catch (IOException e) { //receive()
			// TODO Auto-generated catch block
			//e.printStackTrace();
			for (Filtro f: getSesion().getAceptador().getCadenaFiltros())
				f.excepcionCapturada(getSesion(), lectura, e);		//TODO: si grave, cerrar		
		} 
	}

	@Override
	public InputStream getInputStream(Sesion sesion, int numBytesMaxLectura) {
		// Devuelve una vista inputstream sobre el b�fer de lectura actual (�ltima lectura efectuada)
		// Mantiene el inputstream activo hasta que la lectura finaliza sobre este trozo
		if (bais == null) 
			bais = new ByteArrayInputStream(bufer);
		return bais;			
	}
}

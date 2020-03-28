/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import com.savarese.rocksaw.net.RawSocket;

import msti.util.RawSocketNetlink;
import msti.util.RawSocketNetlink.NetlinkAddress;

public class LectorRawSocket extends Lector {

	private static int numRecv = 0;
	private RawSocket socket;
	
	/* inputstream temporal sobre bufer que crea si se le requiere */
	private ByteArrayInputStream bais;


	private byte[] bufer; // Lectura en construcci�n
	
	public LectorRawSocket() {
		super();
	}

	public RawSocket getSocket() {
		return socket;
	}

	public void setSocket(RawSocket socket) {
		this.socket = socket;
	}

	/**
	 * Espera bloqueante en lectura de datagramas desde el socket asociado a la sesion.
	 * Para cada datagrama genera un objeto Lectura (si la anterior Lectura estaba completada), e invoca al primer
	 * filtro de la cadena.
	 */
	public void run() {
	
		boolean lecturaAnteriorFinalizada = true;
	//	DatagramPacket datagrama;  //datagrama que recibe
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

				//datagrama = new DatagramPacket(bufer, bufer.length);
				// Crea dirección para la familia de direcciones del socket: PF_INET, PF_INET6
				byte[] buferDireccionOrigen = new byte[((AceptadorRawSocket)this.getAceptador()).getFamiliaProtocolos() == RawSocket.PF_INET ? 4: 16];
				//int leidos = this.socket.read(bufer, buferDireccionOrigen); // espera bloqueante  ���verificar que no he puesto timeout??
				int leidos = this.socket.read(bufer, 0, bufer.length, buferDireccionOrigen);
				System.out.println("LectorRawSocket:run()---> Recepción #" + numRecv++ + " " + bufer.length + " octetos. Origen:" + 
									InetAddress.getByAddress(buferDireccionOrigen));

				// Ajusta tama�o de bufer al l�mite de octetos le�dos
				
				// TODO: si no se hace sesi�n nueva en base al origen (en datagrama), la segunda lectura puede
				// venir de otro origen diferente �generar sesi�n diferente por par origen-destino en datagrama?
				// Construye una lectura
				lectura.setDireccionOrigen((SocketAddress)new InetSocketAddress(InetAddress.getByAddress(buferDireccionOrigen), 0 /*dummy port*/)); //TODO: puerto dummy para mantener SockAddress
				lectura.setMensaje(bufer);
				// lectura.setMensajeLength(leidos);  TODO: Pasar offset,length del bufer, si mensaje es un byte[]
				lectura.setDecodificada(false);
				bais = new ByteArrayInputStream(bufer, 0, leidos);  // en datagrama era datagrama.getLength
				lectura.setInputStream(bais);

				
				/*
				//datagrama = new DatagramPacket(bufer, bufer.length);
				// Crea dirección para la familia de direcciones del socket: PF_INET, PF_INET6
				byte[] buferDireccionOrigen = new byte[((AceptadorRawSocket)this.getAceptador()).getFamiliaProtocolos() == RawSocket.PF_INET ? 4: 16];
				byte[] buferDireccionDestino = new byte[((AceptadorRawSocket)this.getAceptador()).getFamiliaProtocolos() == RawSocket.PF_INET ? 4: 16];
				//int leidos = this.socket.read(bufer, buferDireccionOrigen); // espera bloqueante  ���verificar que no he puesto timeout??
				int leidos = this.socket.read(bufer, 0, bufer.length, buferDireccionOrigen, buferDireccionDestino);
				System.out.println("LectorRawSocket:run()---> Recepción #" + numRecv++ + " " + bufer.length + " octetos. Origen:" + 
									InetAddress.getByAddress(buferDireccionOrigen) + "Destino:" + InetAddress.getByAddress(buferDireccionDestino));

				// Ajusta tama�o de bufer al l�mite de octetos le�dos
				
				// TODO: si no se hace sesi�n nueva en base al origen (en datagrama), la segunda lectura puede
				// venir de otro origen diferente �generar sesi�n diferente por par origen-destino en datagrama?
				// Construye una lectura
				lectura.setDireccionOrigen((SocketAddress)new InetSocketAddress(InetAddress.getByAddress(buferDireccionOrigen), 0 )); //TODO: puerto dummy para mantener SockAddress
				lectura.setDireccionLocal((SocketAddress)new InetSocketAddress(InetAddress.getByAddress(buferDireccionDestino), 0 ));
				lectura.setMensaje(bufer);
				// lectura.setMensajeLength(leidos);  TODO: Pasar offset,length del bufer, si mensaje es un byte[]
				lectura.setDecodificada(false);
				bais = new ByteArrayInputStream(bufer, 0, leidos);  // en datagrama era datagrama.getLength
				lectura.setInputStream(bais);
*/
				
				
				
				
				
				
				
				
				
				
				
				
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

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import msti.io.Escritura;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeCodificador;
import msti.io.mensaje.IMensajeDecodificador;

public class MensajeOSPFv2Codec implements IMensajeCodificador, IMensajeDecodificador {

	@Override
	public boolean decodificar(Sesion sesion, Lectura lectura) throws IOException {
		InputStream inputStream;
		
		if (!lectura.isPrimerFragmento()) {
			// TODO: es un fragmento posterior en una lectura fragmentada 
			throw new IllegalArgumentException("Aún no implementada decodificación de varios chunks");
		}
		else {
			// Obtiene inputstream si disponible, o bien lo configura sobre el byte[] recibido en el mensaje
			inputStream = lectura.getInputStream();
			if (inputStream == null) {
				if (lectura.getMensaje() instanceof byte[])
					inputStream = new ByteArrayInputStream((byte[])lectura.getMensaje());
				else 
					throw new UnsupportedOperationException("MensajeOSPFv2Codec: decodificando no se recibe inputstream y el mensaje no es un byte[]: camino no implementado");
			}
		
			//El rawSocket entrega cabecera IP asique leemos sus 20 bytes y los descartamos antes de nada
			DataInputStream dis = new DataInputStream(inputStream);			
			dis.readLong();
			dis.readLong();
			dis.readInt();
			
			// Obtiene un mensaje OSPFv2 (MensajeOSPFv2 es una unión de mensajes). Devuelve una instancia concreta subclase de MensajeOSPFv2.
			MensajeOSPFv2 mensajeOSPFv2 = MensajeOSPFv2.Builder.crear()
										.mezclarDesde(inputStream,null)
										.build();
	
			// Almacena el mensaje construido en el objeto Lectura
			lectura.setMensaje(mensajeOSPFv2);
		}		

		return false;
	}

	@Override
	public boolean codificar(Sesion sesion, Escritura escritura) throws IOException {

		byte[] bufer;
		OutputStream outputStream = null;
		boolean esOutputStreamInterno = false;
		
		IMensaje mensaje = (IMensaje)escritura.getMensaje();

		// Obtiene outputstream si disponible, o bien lo configura sobre el byte[] recibido en el mensaje
		outputStream = escritura.getOutputStream();
		if (outputStream == null) {
				outputStream = new ByteArrayOutputStream(mensaje.getLongitudSerializado());
				esOutputStreamInterno = true;
		}
	
		// Solicita al mensaje que se serialice
		mensaje.writeToOutputStream(outputStream);
		
		if (esOutputStreamInterno) {
			bufer = ((ByteArrayOutputStream)outputStream).toByteArray();
			escritura.setMensaje(bufer);
		}

		return true;
	}

	@Override
	public int getMaxBytes() {
		return 65000;
	}	
}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io.mensaje;

import java.io.IOException;
import java.io.OutputStream;

public interface IMensaje {

	/**
	 *  Obtener un builder para un mensaje de la misma clase que la clase que implementa esta interfaz
	 * @return builder
	 */
	public IMensajeBuilder newBuilder();
	
	/**
	 * Construye un builder inicializado con los valores del objeto mensaje actual (q implementa esta interfaz)
	 */
	public IMensajeBuilder toBuilder();
	
	/**
	 * Serialización sobre array de octetos
	 */
	public byte[] writeToByteArray();
	
	/**
	 * Serialización sobre outputstream
	 */
	public void writeToOutputStream(OutputStream output) throws IOException;
	
	/**
	 * Obtiene el tamaño que ocupará como resultado de la serialización
	 */
	public int getLongitudSerializado();

}

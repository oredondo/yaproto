/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io.mensaje;

import java.io.IOException;
import java.io.InputStream;

public interface IMensajeBuilder {
	/**
	 * Obtiene el objeto final construido (static)
	 * @return Objeto construido con el builder
	 */
	public IMensaje build();
	
	/**
	 * Incorpora al mensaje en construcción, los datos extraídos desde un array de octetos
	 */
	public IMensajeBuilder mezclarDesde(byte[] datos);

	/**
	 * Incorpora al mensaje en construcción, los datos extraídos desde un inputStream
	 */
	public IMensajeBuilder mezclarDesde(InputStream inputStream) throws IOException;

	/**
	 * Incorpora al mensaje en construcción, los datos que han sido modificados en un objeto de la misma clase
	 */
	public IMensajeBuilder mezclarDesde(IMensaje mensaje);
	
	/**
	 * Indica si está completo (el mensaje en construcción tiene rellenos todos los campos obligatorios)
	 */
	public boolean estaCompleto();
	
}

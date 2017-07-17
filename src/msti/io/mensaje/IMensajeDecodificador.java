/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io.mensaje;

import java.io.IOException;

import msti.io.Lectura;
import msti.io.Sesion;

public interface IMensajeDecodificador {

	public boolean decodificar(Sesion sesion, Lectura lectura) throws IOException;
	
	/**
	 * Máximo de octetos que puede consumir cada decodificación (0 si es desconocido)
	 * Se usa para que los filtros anteriores puedan adecuar los búferes a los tamaños adecuados
	 * @return Máximo de octetos que utiliza la codificación más larga
	 */
	public int getMaxBytes();
	
}

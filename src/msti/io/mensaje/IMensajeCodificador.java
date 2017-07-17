/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io.mensaje;

import java.io.IOException;

import msti.io.Escritura;
import msti.io.Sesion;

public interface IMensajeCodificador {

		public boolean codificar(Sesion sesion, Escritura escritura) throws IOException;
		
}

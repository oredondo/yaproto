/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io.mensaje;


public interface IMensajeCodecFactoria {
	public IMensajeDecodificador getDecodificador();
	public IMensajeCodificador getCodificador();
}

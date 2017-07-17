/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

public interface IEscrituraListener {

	/**
	 * Cuando una escritura ha finalizado y ha sido enviada por el canal en su totalidad
	 * TODO: entregar sólo idEscritura, o Object mensajeOriginal y no dejar ver la Escritura hacia fuera?
	 */
	public void escrituraFinalizada(Sesion sesion, Escritura escritura);
	
	/**
	 * Cuando se produce algún error en el proceso de escrituras o canal de salida
	 * TODO: entregar sólo idEscritura o object mensaje (el original) y no dejar ver la Escritura hacia exterior?
	 */
	public void excepcionCapturada(Sesion sesion, Escritura escritura, Throwable e);
}

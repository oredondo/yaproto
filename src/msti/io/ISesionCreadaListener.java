/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

public interface ISesionCreadaListener {
	/** 
	 * Invocado tras crearse una nueva sesión
	 */
	public void sesionCreada(Sesion sesion);
}

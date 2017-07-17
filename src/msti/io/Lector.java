/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.InputStream;

public abstract class Lector implements Runnable {

	private Sesion sesion;
	
	private Aceptador aceptador;
	
	public Lector() {
	}
	
	public abstract InputStream getInputStream(Sesion sesion, int numBytesMaxLectura);

	public abstract void run();

	public void setSesion(Sesion sesion) {
		this.sesion = sesion;
	}

	public Sesion getSesion() {
		return sesion;
	}

	public void setAceptador(Aceptador aceptador) {
		this.aceptador = aceptador;
	}

	public Aceptador getAceptador() {
		return aceptador;
	}

}


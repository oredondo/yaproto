/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;


import msti.util.HashMappedDoublyLinkedList;


public abstract class Conectador implements Runnable {

	protected SesionConfiguracion sesionConfiguracion;

	protected HashMappedDoublyLinkedList<String, Filtro> cadenaFiltros;
	
	protected Lector lector;
	
	protected Escritor escritor;
	
	public Conectador() {
		this.cadenaFiltros = new HashMappedDoublyLinkedList<String, Filtro>();
		this.lector = null;
		this.escritor = null;
	}

	public HashMappedDoublyLinkedList<String, Filtro> getCadenaFiltros() {
		return cadenaFiltros;
	}

	public void setSesionConfiguracion(SesionConfiguracion sesionConfiguracion) {
		this.sesionConfiguracion = sesionConfiguracion;
	}

	public SesionConfiguracion getSesionConfiguracion() {
		return sesionConfiguracion;
	}
	

	public Escritor getEscritor() {
		return escritor;
	}

	public Lector getLector() {
		return lector;
	}	
	
	@Override
	public abstract void run();

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;


import msti.util.HashMappedDoublyLinkedList;


public abstract class Aceptador implements Runnable {

	protected SesionConfiguracion sesionConfiguracion;

	protected HashMappedDoublyLinkedList<String, Filtro> cadenaFiltros;
	
	protected Lector lector;
	
	protected Escritor escritor;
	
	/**  Nombre que el aceptador asignará a todas las sesiones que cree */
	protected String nombre;
	
	public Aceptador() {
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

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

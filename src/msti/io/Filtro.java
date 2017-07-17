/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.InputStream;
import java.io.OutputStream;


public abstract class Filtro implements IFiltro {


	//	protected List<Filtro> cadenaFiltros;
	protected String nombre;
	


	public Filtro(String nombre) {
		this.nombre = nombre;
	}

	/**
     * Devuelve el nombre del filtro
     */
    public String getNombre() {
    	return nombre;
    }

	public abstract void init();
	
	public abstract void destroy();

	public void sesionCreada(Sesion sesion) {
		sesion.getAceptador().getCadenaFiltros().getNext(this.getNombre()).sesionCreada(sesion);
	}
	
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		return sesion.getAceptador().getCadenaFiltros().getNext(this.getNombre()).mensajeRecibido(sesion, lectura);
	}

	public  void sesionInactiva(Sesion sesion) {
		sesion.getAceptador().getCadenaFiltros().getNext(this.getNombre()).sesionInactiva(sesion);
	}	
	public  void sesionCerrada(Sesion sesion) {	
		sesion.getAceptador().getCadenaFiltros().getNext(this.getNombre()).sesionCerrada(sesion);
	}

	public  void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) { 
		sesion.getAceptador().getCadenaFiltros().getNext(this.getNombre()).excepcionCapturada(sesion, lectura, e);
	}

	/** 
	 * Escritura
	 */
	public  void escribir(Sesion sesion, Escritura escritura) {
		// Si es primer filtro de la cadena, al Escritor, sino al filtro anterior
		Filtro filtroAnterior = sesion.getAceptador().getCadenaFiltros().getPrevious(this.nombre);

		if (filtroAnterior == null) {
			// configura un outputStream en la escritura
			escritura.setOutputStream(sesion.getEscritor().getOutputStream(sesion, 0));  //no indica numBytesEscritura
			sesion.getEscritor().escribir(sesion, escritura);
		}
		else {
			// configura un outputStream en la escritura
			escritura.setOutputStream(filtroAnterior.getOutputStream(sesion, 0));  //no indica numBytesEscritura
			filtroAnterior.escribir(sesion, escritura);
		}
	}
	/**
	 * Proporciona un outputStream al filtro siguiente.
	 * Habitualmente pasará la llamada al filtro anterior sin interceptarla, pero puede hacerlo para, p.ej., 
	 * ofrecer un ByteArrayOutputStream byte[] al filtro siguiente para algún tipo de proceso del byte[] antes de
	 * continuar su envío por la cadena.
	 * 
	 * @param sesion
	 * @param numBytesEscritura -> número de octetos que tiene previsto escribir
	 * @return OutputStream
	 */
	public OutputStream getOutputStream(Sesion sesion, int numBytesEscritura) {
		Filtro filtroAnterior = sesion.getAceptador().getCadenaFiltros().getPrevious(this.nombre);

		if (filtroAnterior == null)
			// Si no hay filtro anterior, dirigirse al lector
			return sesion.getEscritor().getOutputStream(sesion, numBytesEscritura);
		else
			return filtroAnterior.getOutputStream(sesion, numBytesEscritura);
	}

	/**
	 * Proporciona un inputStream al filtro siguiente.
	 * Habitualmente pasará la llamada al filtro anterior sin interceptarla, pero puede hacerlo para, p.ej., 
	 * ofrecer un ByteArrayInputStream byte[] para algún tipo de proceso del byte[] antes de
	 * continuar su envío por la cadena.
	 * 
	 * @param sesion
	 * @param numBytesEscritura -> número de octetos que tiene previsto escribir
	 * @return OutputStream
	 */
	public InputStream getInputStream(Sesion sesion, int numBytesMaxLectura) {
		Filtro filtroAnterior = sesion.getAceptador().getCadenaFiltros().getPrevious(this.nombre);

		if (filtroAnterior == null)
			// Si no hay filtro anterior, es el lector
			return sesion.getLector().getInputStream(sesion, numBytesMaxLectura);
		else
			return filtroAnterior.getInputStream(sesion, numBytesMaxLectura);
	}
}
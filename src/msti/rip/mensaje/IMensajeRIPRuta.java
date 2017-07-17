/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.mensaje;

import java.net.InetAddress;

public interface IMensajeRIPRuta {

	public short getIdFamiliaDirecciones();
	public boolean hasIdFamiliaDirecciones();

	public short getEtiquetaRuta();
	public boolean hasEtiquetaRuta();

	public InetAddress getDireccionIP();
	public boolean hasDireccionIP();
	
	public int getLongitudPrefijoRed();
	public boolean hasLongitudPrefijoRed();

	public InetAddress getDireccionProximoSalto();
	public boolean hasDireccionProximoSalto();

	public int getMetrica();
	public boolean hasMetrica();

	/**
	 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
	 */
	public interface Build {
		public Build setIdFamiliaDirecciones(short idFamiliaDirecciones);

		public Build setEtiquetaRuta(short etiquetaRuta);

		public Build setDireccionIP(InetAddress direccionIP);

		public Build setLongitudPrefijoRed(int longitudPrefijoRed);

		public Build setDireccionProximoSalto(InetAddress direccionProximoSalto);

		public Build setMetrica(int metrica);	
	}
}
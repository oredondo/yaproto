/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.mensaje;

import java.util.List;

public interface IMensajeRIPRespuesta extends IMensajeRIP {

	public List<IMensajeRIPRuta> getRIPRutas();
	public boolean hasRIPRutas();

	/**
	 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
	 */
	public interface Build extends IMensajeRIP.Build {

		public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);

		public Build removeRIPRutas();

		public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);

	}
}


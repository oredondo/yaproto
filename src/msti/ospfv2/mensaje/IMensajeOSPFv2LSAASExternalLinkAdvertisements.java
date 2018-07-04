/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.List;

import msti.ospfv2.mensaje.IMensajeOSPFv2LSARouterLinksLinks.Build;

public interface IMensajeOSPFv2LSAASExternalLinkAdvertisements extends IMensajeOSPFv2LSA {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();
		
		public int getNetworkMask();
		public boolean hasNetworkMask();
		
		public List<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields> getTOSFields();
		public boolean hasTOSFields();
			
		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2LSA.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setNetworkMask(int networkMask);
			
			public Build setTOSFields(List<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields> tosFields);
			
			public Build addTOSField(IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields tosField);
			public Build removeTOSFields();


			

		}
}
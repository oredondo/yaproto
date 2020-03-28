/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.List;

public interface IMensajeOSPFv2LinkStateUpdate extends IMensajeOSPFv2 {
	
		public int getAdvertisements();
		public boolean hasAdvertisements();
		

		public List<IMensajeOSPFv2LSA> getLSAs();
		public boolean hasLSAs(); 
		

		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setAdvertisements(int advertisements);
			
			public Build setLSAs(List<IMensajeOSPFv2LSA> lsas);			
			public Build addLSA(IMensajeOSPFv2LSA lsa);	
			public Build removeLSAs();

			

		}
}
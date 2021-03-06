/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.List;

public interface IMensajeOSPFv2DatabaseDescription extends IMensajeOSPFv2 {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();
		
		public byte getOptions();
		public boolean hasOptions();
		
		public byte getIMMS();
		public boolean hasIMMS();
		
		public int getDDSequenceNumber();
		public boolean hasDDSequenceNumber(); 
		
		public List<IMensajeOSPFv2LinkStateAdvertisementHeader> getLSAHeaders();
		public boolean hasLSAHeaders(); 
		

		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setOptions(byte options);
			public Build setIMMS(byte imms);
			public Build setDDSequenceNumber(int dDSequenceNumber);
			public Build setLSAHeaders(List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders);
			
			public Build removeLSAHeaders();
			public Build addLSAHeader(IMensajeOSPFv2LinkStateAdvertisementHeader lSAHeader);

			

		}
}

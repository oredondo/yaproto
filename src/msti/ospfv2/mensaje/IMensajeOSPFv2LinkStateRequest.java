/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.List;

public interface IMensajeOSPFv2LinkStateRequest extends IMensajeOSPFv2 {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();
		
		public List<Integer> getLSTypes();
		public boolean hasLSTypes();
		
		public List<Integer> getLSIDs();
		public boolean hasLSIDs();
		
		public List<Integer> getAdvertisingRouters();
		public boolean hasAdvertisingRouters();
		

		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			
			public Build setLSTypes(List<Integer> lSTypes);			
			public Build addLSTypes(int lSTypes);
			
			public Build setLSIDs(List<Integer> lSIDs);			
			public Build addLSIDs(int lSIDs);
			
			public Build setAdvertisingRouters(List<Integer> advertisingRouters);			
			public Build addAdvertisingRouters(int advertisingRouter);
			
			public Build removeLSAdv();

			

		}
}
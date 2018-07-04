/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.Collections;
import java.util.Iterator;



public interface IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields extends IMensajeOSPFv2LSA {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();		
		
		public byte getETOS();
		public boolean hasETOS();
		
		public short getMetric();
		public boolean hasMetric();
		
		public int getForwardingAddress();
		public boolean hasForwardingAddress();
		
		public int getExternalRouteTag();
		public boolean hasExternalRouteTag();
		
		

		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2LSA.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setETOS(byte etos);
			public Build setMetric(short metric);
			public Build setForwardingAddress(int forwardingAddress);
			public Build setExternalRouteTag(int externalRouteTag);
			

		}
}
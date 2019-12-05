/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.List;

public interface IMensajeOSPFv2LSARouterLinks extends IMensajeOSPFv2LSA {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();
		public byte getVEB();
		public boolean hasVEB();
		
		public short getNLinks();
		public boolean hasNLinks();
		
		public List<IMensajeOSPFv2LSARouterLinksLinks> getRouterLinks();
		public boolean hasRouterLinks(); 

		
		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2LSA.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setVEB(byte veb);
			public Build setNLinks(short nLinks);
			public Build setRouterLinks(List<IMensajeOSPFv2LSARouterLinksLinks> routerLinks);
			
			public Build removeRouterLinks();
			public Build addRouterLink(IMensajeOSPFv2LSARouterLinksLinks routerLink);

			

		}
}

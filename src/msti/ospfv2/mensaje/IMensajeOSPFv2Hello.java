/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.List;

public interface IMensajeOSPFv2Hello extends IMensajeOSPFv2 {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();
		public int getNetworkMask();
		public boolean hasNetworkMask();
		
		public short getHelloInterval();
		public boolean hasHelloInterval();
		
		public byte getOptions();
		public boolean hasOptions(); 
		
		public byte getRtrPri();
		public boolean hasRtrPri(); 
		
		public int getRouterDeadInterval();
		public boolean hasRouterDeadInterval(); 
		
		public int getDesignatedRouter();
		public boolean hasDesignatedRouter();
		
		public int getBackupDesignatedRouter();
		public boolean hasBackupDesignatedRouter(); 
		
		public List<Integer> getNeighbors();
		public boolean hasNeighbors(); 
		

		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setNetworkMask(int networkmask);
			public Build setHelloInterval(short helloInterval);
			public Build setOptions(byte options);
			public Build setRtrPri(byte rtrPri);
			public Build setRouterDeadInterval(int routerDeadInterval);
			public Build setDesignatedRouter(int designatedRouter);
			public Build setBackupDesignatedRouter(int backupDesignatedRouter);
			public Build setNeighbors(List<Integer> neighbors);
			
			public Build removeNeighbors();
			public Build addNeighbor(int neighbor);

			

		}
}


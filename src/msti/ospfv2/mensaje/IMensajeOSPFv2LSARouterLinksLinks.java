/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateRequest.Build;



public interface IMensajeOSPFv2LSARouterLinksLinks extends IMensajeOSPFv2LSA {
	
		//public List<IMensajeRIPRuta> getRIPRutas();
		//public boolean hasRIPRutas();
	
		enum Type {
			PTPConnetctionToAnotherRouter((byte)1), 
			ConnectionToATransistNetwork((byte)2),
			ConnectionToAStubNetwork((byte)3),
			VirtualLink((byte)4);
			
			private byte codigo;
			private Type(byte codigo) { this.codigo = codigo; }
			public byte getCodigo() { return codigo; }
			public void setCodigo(byte codigo) { this.codigo = codigo; }
			public Iterator<Type> iterator() { return Collections.singleton(this).iterator(); }
			public static Type getByValue(int value){
			    for(Type en : values())
			        if( en.getCodigo() == value)
			            return en;
			    return null;
			}
		}

		
		public int getLinkID();
		public boolean hasLinkID();
		
		public int getLinkData();
		public boolean hasLinkData();
				
		public Type getType();
		public boolean hasType();
		
		public byte getNTOS();
		public boolean hasNTOS();

		public short getTOS0Metric();
		public boolean hasTOS0Metric();
		
		public List<Byte> getTOSs();
		public boolean hasTOSs();
		
		public List<Short> getMetrics();
		public boolean hasMetrics();
		

		//public boolean esPeticionTablaCompleta();
		/**
		 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
		 */
		public interface Build extends IMensajeOSPFv2LSA.Build {

			//public Build setRIPRutas(List<IMensajeRIPRuta> ripRutas);
			
			//public Build removeRIPRutas();

			//public Build addRIPRuta(IMensajeRIPRuta mensajeRIPRuta);
			
			public Build setLinkID(int linkID);
			public Build setLinkData(int linkData);
			public Build setType(Type type);
			public Build setNTOS(byte ntos);
			public Build setTOS0Metric(short tos0Metric);
			
			public Build setTOSs(List<Byte> toss);
			public Build setMetrics(List<Short> metrics);
			
			public Build addTOS(Byte tos);
			public Build addMetric(Short metric);
			public Build removeTOSs();
			public Build removeMetrics();


		}
}

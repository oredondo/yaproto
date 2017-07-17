/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

public interface IMensajeNetlinkRouteAttributePriority extends IMensajeNetlinkRouteAttribute {
	
		public int getPriority();
		public boolean hasPriority();
		
		/**
		 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
		 */
		public interface Build extends IMensajeNetlinkRouteAttribute.Build {
			
			public Build setPriority(int priority);
			
		}
}


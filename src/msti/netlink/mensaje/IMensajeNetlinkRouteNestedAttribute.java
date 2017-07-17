/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.util.List;

public interface IMensajeNetlinkRouteNestedAttribute extends IMensajeNetlinkRouteAttribute {
	
		public List<IMensajeNetlinkRouteAttribute> getNestedAttributes();
		public boolean hasNestedAttributes();
		
		/**
		 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
		 */
		public interface Build extends IMensajeNetlinkRouteAttribute.Build {
			
			public Build setNestedAttributes(List<IMensajeNetlinkRouteAttribute> nestedAttributes);
			
			public Build removeNestedAttributes();

			public Build addNestedAttribute(IMensajeNetlinkRouteAttribute nestedAttribute);

		}
}


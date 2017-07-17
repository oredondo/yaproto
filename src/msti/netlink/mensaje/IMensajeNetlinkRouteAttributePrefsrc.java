/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.net.InetAddress;

public interface IMensajeNetlinkRouteAttributePrefsrc extends IMensajeNetlinkRouteAttribute {
	
	public InetAddress getSrc();
	public boolean hasSrc();

	/**
	 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
	 */
	public interface Build extends IMensajeNetlinkRouteAttribute.Build {
		
		public Build setSrc(InetAddress src);

	}
}


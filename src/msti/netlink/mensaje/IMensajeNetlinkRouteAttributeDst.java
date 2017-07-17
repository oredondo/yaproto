/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.net.InetAddress;

/**
 * Aunque en netlink se denomina Dst, en realidad es la pareja de PrefDst, es decir, es un destino con
 * prefijo asociado.
 *
 */
public interface IMensajeNetlinkRouteAttributeDst extends IMensajeNetlinkRouteAttribute {
	
	public InetAddress getDst();
	public boolean hasDst();

	public byte getPrefixLength();
	public boolean hasPrefixLength();
	/**
	 * M�todos de modificaci�n de atributos. La clase IMensaje, una vez construida, es de s�lo lectura
	 */
	public interface Build extends IMensajeNetlinkRouteAttribute.Build {
		
		public Build setDst(InetAddress dst);
		
		public Build setPrefixLength(byte prefixLength);

	}
}


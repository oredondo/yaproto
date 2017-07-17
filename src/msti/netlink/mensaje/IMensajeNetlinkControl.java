/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.util.Collections;
import java.util.Iterator;

public interface IMensajeNetlinkControl extends IMensajeNetlink {

	/**
	 * Devuelve el código del mensaje de control 
	 * @return
	 */
	enum NetlinkControlCode {
		NLMSG_NOERROR(0),
		/** Nothing */
		NLMSG_NOOP(0x1),
		/** Error */
		NLMSG_ERROR(0x2),
		/** End of a dump */
		NLMSG_DONE(0x3),
		/** Data lost */
		NLMSG_OVERRUN(0x4);

		public int value;

		NetlinkControlCode(int value) { this.value = value; }
		public void setValue(int value) { this.value = value; }
		public int getValue() { return this.value; }
		public Iterator<NetlinkControlCode> iterator() { return Collections.singleton(this).iterator(); }
		public static NetlinkControlCode getByValue(int value){
			for(NetlinkControlCode en : values())
				if( en.getValue() == value)
					return en;
			return null;
		}
	}
	public NetlinkControlCode getCode();
	public boolean hasCode();

	/**
	 * Devuelve la cabecera del mensaje netlink al que se refiere el código.
	 * @return
	 */
	public MensajeNetlink getReferencedNetlinkMessage();
	public boolean hasReferencedNetlinkMessage();

	/**
	 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
	 */
	public interface Build extends IMensajeNetlink.Build {

		public Build setCode(NetlinkControlCode code);

		/*
		 * Mensaje al que hace referencia el código de control.
		 * 
		 * Si es una instancia de una subclase de MensajeNetlink, sólo se queda con su cabecera
		 */
		public Build setReferencedNetlinkMessage(MensajeNetlink referencedNetlinkMessage);
	}
}


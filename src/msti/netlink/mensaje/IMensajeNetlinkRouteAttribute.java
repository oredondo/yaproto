/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.util.Collections;
import java.util.Iterator;

public interface IMensajeNetlinkRouteAttribute {

	// TODO: RouteMetric es de un tipo de atributo, llevar a su clase hija después
	public enum RouteMetric {
		RTAX_UNSPEC(0),
		RTAX_LOCK(1),
		RTAX_MTU(2),
		RTAX_WINDOW(3),
		RTAX_RTT(4),
		RTAX_RTTVAR(5),
		RTAX_SSTHRESH(6),
		RTAX_CWND(7),
		RTAX_ADVMSS(8),
		RTAX_REORDERING(9),
		RTAX_HOPLIMIT(10),
		RTAX_INITCWND(11),
		RTAX_FEATURES(12),
		RTAX_RTO_MIN(13);

		public int value;

		RouteMetric(int value) { this.value = value; }
		public void setValue(int value) { this.value = value; }
		public int getValue() { return this.value; }
		public int maxValue() { return RTAX_RTO_MIN.getValue(); }
		public Iterator<RouteMetric> iterator() { return Collections.singleton(this).iterator(); }
	};

	/** 
	 * Máscara aplicable al tipo de atributos 
	 * nla_type (16 bits)
	 * +---+---+-------------------------------+
	 * | N | O | Attribute Type                |
	 * +---+---+-------------------------------+
	 * N := Carries nested attributes
	 * O := Payload stored in network byte order
	 *
	 * Note: The N and O flag are mutually exclusive.
	 * */
	/** Indica atributo anidado */
	public static final short NLA_F_NESTED = (short)(1 << 15);
	/** Indica atributo tiene orden de red  */
	public static final short NLA_F_NET_BYTEORDER = (short) 1 << 14;
	/** M�scara aplicable para obtener el valor de NetlinkRouteAttributeType */
	public static final short NLA_TYPE_MASK = (short) ~((short)(NLA_F_NESTED | NLA_F_NET_BYTEORDER));
	
	public enum NetlinkRouteAttributeType {
		RTA_UNSPEC(0),
		RTA_DST(1),
		RTA_SRC(2),
		RTA_IIF(3),
		RTA_OIF(4),
		RTA_GATEWAY(5),
		RTA_PRIORITY(6),
		RTA_PREFSRC(7),
		RTA_METRICS(8),
		RTA_MULTIPATH(9),
		/** no longer used */
		RTA_PROTOINFO(10),
		RTA_FLOW(11),
		RTA_CACHEINFO(12),
		/** no longer used */
		RTA_SESSION(13),
		/** no longer used */
		RTA_MP_ALGO(14),
		RTA_TABLE(15),
		/* Atributo dummy. En ocasiones se reciben atributos inexistentes. En tal caso, se asigna tipo RTA_MAX */
		RTA_MAX(16);
		public int value;

		NetlinkRouteAttributeType(int value) { this.value = value; }
		public void setValue(int value) { this.value = value; }
		public int getValue() { return this.value; }
		public int maxValue() { return RTA_TABLE.getValue(); }
		public Iterator<NetlinkRouteAttributeType> iterator() { return Collections.singleton(this).iterator(); }
		public static NetlinkRouteAttributeType getByValue(int value){
		    for(NetlinkRouteAttributeType en : values())
		        if( en.getValue() == value)
		            return en;
		    return null;
		}
	};

	public NetlinkRouteAttributeType getAttributeType();
	public boolean hasAttributeType();
	
	public short getLength();
	public boolean hasLength();

	/**
	 * Clase que implementarán sus builder
	 */
	public interface Build {

		public Build setAttributeType(NetlinkRouteAttributeType attributeType);

	}
}
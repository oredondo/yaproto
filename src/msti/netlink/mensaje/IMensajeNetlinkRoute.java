/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface IMensajeNetlinkRoute extends IMensajeNetlink {
	
		public byte getRouteAddressFamily();
		public boolean hasRouteAddressFamily();
		
		public byte getRouteTypeOfService();
		public boolean hasRouteTypeOfService();

		public enum RoutingTableId {
			RT_TABLE_UNSPEC(0),
			/// User defined values
			RT_TABLE_COMPAT(252),
			RT_TABLE_DEFAULT(253),
			RT_TABLE_MAIN(254),
			RT_TABLE_LOCAL(255);

			public int value;

			RoutingTableId(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return 0xFFFFFFFF; }
			public Iterator<RoutingTableId> iterator() { return Collections.singleton(this).iterator(); }
			public static RoutingTableId getByValue(int value){
			    for(RoutingTableId en : values())
			        if( en.getValue() == value)
			            return en;
			    return null;
			}
		};

		public RoutingTableId getRoutingTableId();
		public boolean hasRoutingTableId();
		
		public enum RoutingProtocol {
			RTPROT_UNSPEC(0),
			/** Route installed by ICMP redirects; not used by current IPv4 */
			RTPROT_REDIRECT(1), 			
			/** Route installed by kernel		*/
			RTPROT_KERNEL(2),
			/** Route installed during boot		*/
			RTPROT_BOOT(3),
			/** Route installed by administrator	*/
			/**
			 * Values of protocol >= RTPROT_STATIC are not interpreted by kernel;<br>
			 * they are just passed from user and back as is.<br>
			 * It will be used by hypothetical multiple routing daemons.<br>
			 * Note that protocol values should be standardized in order to<br>
			 * avoid conflicts.
			 */
			RTPROT_STATIC(4),
			// Next values of protocol >= RTPROT_STATIC are not interpreted by kernel;
			/** Apparently, GateD */
			RTPROT_GATED(8),
			/** RDISC/ND router advertisements */
			RTPROT_RA(9),
			/** Merit MRT */
			RTPROT_MRT(10),
			RTPROT_ZEBRA(11),
			RTPROT_BIRD(12),
			/** DECnet routing daemon */
			RTPROT_DNROUTED(13),
			RTPROT_XORP(14),
			/** Netsukuku */
			RTPROT_NTK(15),
			/** DHCP client */
			RTPROT_DHCP(16);

			public int value;

			RoutingProtocol(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return RTPROT_DHCP.getValue(); }
			public Iterator<RoutingProtocol> iterator() { return Collections.singleton(this).iterator(); }
			public static RoutingProtocol getByValue(int value){
			    for(RoutingProtocol en : values())
			        if( en.getValue() == value)
			            return en;
			    return null;
			}
		};

		public RoutingProtocol getRoutingProtocol();
		public boolean hasRoutingProtocol();
		
		public enum RouteScope {
			RT_SCOPE_UNIVERSE(0),
			/** User defined values between UNIVERSE and SITE */
			RT_SCOPE_SITE(200),
			RT_SCOPE_LINK(253),
			RT_SCOPE_HOST(254),
			RT_SCOPE_NOWHERE(255);

			public int value;

			RouteScope(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return RT_SCOPE_NOWHERE.getValue(); }
			public Iterator<RouteScope> iterator() { return Collections.singleton(this).iterator(); }
			public static RouteScope getByValue(int value){
			    for(RouteScope en : values())
			        if( en.getValue() == value)
			            return en;
			    return null;
			}
		};

		public RouteScope getRouteScope();
		public boolean hasRouteScope();

		public enum RouteType {
			RTN_UNSPEC(0),
			/** Gateway or direct route */
			RTN_UNICAST(1),
			/** Accept locally */
			RTN_LOCAL(2),
			/**
			 * Accept locally as broadcast,<br>
			 * send as broadcast
			 */
			RTN_BROADCAST(3),
			/**
			 * Accept locally as broadcast,<br>
			 * but send as unicast
			 */
			RTN_ANYCAST(4),
			/** Multicast route */
			RTN_MULTICAST(5),
			/** Drop */
			RTN_BLACKHOLE(6),
			/** Destination is unreachable */
			RTN_UNREACHABLE(7),
			/** Administratively prohibited */
			RTN_PROHIBIT(8),
			/** Not in this table */
			RTN_THROW(9),
			/** Translate this address */
			RTN_NAT(10),
			/** Use external resolver */
			RTN_XRESOLVE(11);

			public int value;

			RouteType(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return RTN_XRESOLVE.getValue(); }
			public Iterator<RouteType> iterator() { return Collections.singleton(this).iterator(); }
			public static RouteType getByValue(int value){
			    for(RouteType en : values())
			        if( en.getValue() == value)
			            return en;
			    return null;
			}
		};

		public RouteType getRouteType();
		public boolean hasRouteType();
		
		public enum RouteFlag {
			/** Notify user of route change	*/
			RTM_F_NOTIFY(0x100),
			/** This route is cloned		*/
			RTM_F_CLONED(0x200),
			/** Multipath equalizer: NI	*/
			RTM_F_EQUALIZE(0x400),
			/** Prefix addresses		*/
			RTM_F_PREFIX(0x800);

			public int value;

			RouteFlag(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return RTM_F_PREFIX.getValue(); }
			public Iterator<RouteFlag> iterator() { return Collections.singleton(this).iterator(); }
			public static RouteFlag getByValue(int value){
			    for(RouteFlag en : values())
			        if( en.getValue() == value)
			            return en;
			    return null;
			}
		};
		public RouteFlag getRouteFlag();
		public boolean hasRouteFlag();
		
		public List<IMensajeNetlinkRouteAttribute> getNetlinkAttributes();
		public boolean hasNetlinkAttributes();

		public enum NextHopFlag {
			RTNH_F_DEAD(1),
			RTNH_F_PERVASIVE(2),
			RTNH_F_ONLINK(4);

			public int value;

			NextHopFlag(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return RTNH_F_ONLINK.getValue(); }
			public Iterator<NextHopFlag> iterator() { return Collections.singleton(this).iterator(); }
			public static NextHopFlag getByValue(int value){
			    for(NextHopFlag en : values())
			        if( en.getValue() == value)
			            return en;
			    return null;
			}
		};

		
		/**
		 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
		 */
		public interface Build extends IMensajeNetlink.Build {

			public Build setRouteAddressFamily(byte routeAddressFamily);
			
			public Build setRouteTypeOfService(byte routeTypeOfService);

			public Build setRoutingTableId(RoutingTableId routingTableId);
			
			public Build setRoutingProtocol(RoutingProtocol routingProtocol);
			
			public Build setRouteScope(RouteScope routeScope);
			
			public Build setRouteType(RouteType routeType);
			
			public Build setRouteFlag(RouteFlag routeFlag);

			public Build setNetlinkAttributes(List<IMensajeNetlinkRouteAttribute> netlinkAttributes);
			
			public Build removeNetlinkAttributes();

			public Build addNetlinkAttribute(IMensajeNetlinkRouteAttribute netlinkAttribute);

		}
}


/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.util.Collections;
import java.util.Iterator;

public interface IMensajeNetlink {

	/**
	 * Números de protocolo netlink (al construir el socket) 
	 */
	public enum NetlinkProtocol {
		/// Routing/device hook
		NETLINK_ROUTE(0),
		/// Unused number
		NETLINK_UNUSED(1),
		/// Reserved for user mode socket protocols
		NETLINK_USERSOCK(2),
		/// Firewalling hook
		NETLINK_FIREWALL(3),
		/// INET socket monitoring
		NETLINK_INET_DIAG(4),
		/// netfilter/iptables ULOG
		NETLINK_NFLOG(5),
		/// ipsec
		NETLINK_XFRM(6),
		/// SELinux event notifications
		NETLINK_SELINUX(7),
		/// Open-iSCSI
		NETLINK_ISCSI(8),
		/// auditing
		NETLINK_AUDIT(9),
		NETLINK_FIB_LOOKUP(10),
		NETLINK_CONNECTOR(11),
		/// netfilter subsystem
		NETLINK_NETFILTER(12),
		NETLINK_IP6_FW(13),
		/// DECnet routing messages
		NETLINK_DNRTMSG(14),
		/// Kernel messages to userspace
		NETLINK_KOBJECT_UEVENT(15),
		NETLINK_GENERIC(16),
		/// SCSI Transports
		NETLINK_SCSITRANSPORT(18),
		NETLINK_ECRYPTFS(19);

		public int value;

		NetlinkProtocol(int value) { this.value = value; }
		public void setValue(short value) { this.value = value; }
		public int getValue() { return this.value; }
		public int MaxValue() { return 32; } // Son 32 protocolos el límite para netlink (.h)
		public Iterator<NetlinkProtocol> iterator() { return Collections.singleton(this).iterator(); }
	};

	/**
	 * Tipo de mensajes Netlink 
	 */
	public enum NetlinkMessageType {
		/** Reservado para mensajes de control (< 0x10) */
		/** Nothing */
		NLMSG_NOOP(0x1),
		/** Error */
		NLMSG_ERROR(0x2),
		/** End of a dump (ej: multipart) */
		NLMSG_DONE(0x3),	
		/** Data lost */
		NLMSG_OVERRUN(0x4),

		/* Mensajes >= 0x10 (16), son mensajes netlink */
		RTM_BASE(0x10),
		RTM_NEWLINK(16),
		RTM_DELLINK(17),
		RTM_GETLINK(18),
		RTM_SETLINK(19),
		RTM_NEWADDR(20),
		RTM_DELADDR(21),
		RTM_GETADDR(22),
		RTM_NEWROUTE(24),
		RTM_DELROUTE(25),
		RTM_GETROUTE(26),
		RTM_NEWNEIGH(28),
		RTM_DELNEIGH(29),
		RTM_GETNEIGH(30),
		RTM_NEWRULE(32),
		RTM_DELRULE(33),
		RTM_GETRULE(34),
		RTM_NEWQDISC(36),
		RTM_DELQDISC(37),
		RTM_GETQDISC(38),
		RTM_NEWTCLASS(40),
		RTM_DELTCLASS(41),
		RTM_GETTCLASS(42),
		RTM_NEWTFILTER(44),
		RTM_DELTFILTER(45),
		RTM_GETTFILTER(46),
		RTM_NEWACTION(48),
		RTM_DELACTION(49),
		RTM_GETACTION(50),
		RTM_NEWPREFIX(52),
		RTM_GETMULTICAST(58),
		RTM_GETANYCAST(62),
		RTM_NEWNEIGHTBL(64),
		RTM_GETNEIGHTBL(66),
		RTM_SETNEIGHTBL(67),
		RTM_NEWNDUSEROPT(68),
		RTM_NEWADDRLABEL(72),
		RTM_DELADDRLABEL(73),
		RTM_GETADDRLABEL(74),
		RTM_GETDCB(78),
		RTM_SETDCB(79);

		public int value;

		NetlinkMessageType(int value) { this.value = value; }
		public void setValue(int value) { this.value = value; }
		public int getValue() { return this.value; }
		public int MaxValue() { return RTM_SETDCB.getValue(); } // último valor 
		public Iterator<NetlinkMessageType> iterator() { return Collections.singleton(this).iterator(); }
		public static NetlinkMessageType getByValue(int value){
		    for(NetlinkMessageType en : values())
		        if( en.getValue() == value)
		            return en;
		    return null;
		}
	};

	/**
	 * Netlink flags.
	 */
	public enum NetlinkFlag {
		NLM_F_REQUEST(1), 				// It is request message.
		NLM_F_MULTI(2),					// Multipart message, terminated by NLMSG_DONE
		NLM_F_ACK(4),					// Reply with ack, with zero or error code
		NLM_F_ECHO(8),					// Echo this request
		/// Modifiers to GET request
		NLM_F_ROOT(0x100),				// Specify tree	root	
		NLM_F_MATCH(0x200),				// Return all matching	
		NLM_F_ATOMIC(0x400),			// atomic GET
		NLM_F_DUMP((NetlinkFlag.NLM_F_ROOT.getValue() | NetlinkFlag.NLM_F_MATCH.getValue())),
		/// Modifiers to NEW request	
		NLM_F_REPLACE(0x100),			// Override existing
		NLM_F_EXCL(0x200),				// Do not touch if it exists
		NLM_F_CREATE(0x400),			// Create, if it does not exist
		NLM_F_APPEND(0x800);			// Add to end of list

		public int value;

		NetlinkFlag(int value) { this.value = value; }
		public void setValue(int value) { this.value = value; }
		public int getValue() { return this.value; }
		public Iterator<NetlinkFlag> iterator() { return Collections.singleton(this).iterator(); }
		public static NetlinkFlag getByValue(int value){
		    for(NetlinkFlag en : values())
		        if( en.getValue() == value)
		            return en;
		    return null;
		}
	};

	public NetlinkMessageType getMessageType();
	public boolean hasMessageType();
	
	public short getFlags();
	public boolean hasFlags();

	public int getSequenceNumber();
	public boolean hasSequenceNumber();

	public int getProcessId();
	public boolean hasProcessId();

	/**
	 * Interfaz de escritura del mensaje (lo implementará un builder)
	 */
	public interface Build {

		public Build setMessageType(NetlinkMessageType messageType);	

		public Build setFlag(NetlinkFlag flag);

		public Build clearFlag(NetlinkFlag flag);

		/* TODO: Duda sobre si publicarlo o dejarlo protected para uso interno */
		public Build setFlags(short flags);

		public Build setSequenceNumber(int sequenceNumber);

		public Build setProcessId(int processId);

	}
}

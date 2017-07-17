/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;

import com.savarese.rocksaw.net.RawSocket;

public class RawSocketNetlink extends RawSocket {

	  protected native static int __PF_NETLINK();

	  /**
	   * A protocol family constant for {@link #open} indicating NETLINK.
	   */
	  public static final int PF_NETLINK;
	  public static final boolean isBigEndianNativeArchitecture;

	  /**
	   * Se ejecuta previamente el de la superclase, que carga la biblioteca din�mica nativa
	   */
	  static {
		    PF_NETLINK = __PF_NETLINK();
			isBigEndianNativeArchitecture = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
	  }


		public enum NetlinkMulticastGroup {
			RTMGRP_LINK(1),
			RTMGRP_NOTIFY(2),
			RTMGRP_NEIGH(4),
			RTMGRP_TC(8),
			RTMGRP_IPV4_IFADDR(0x10),
			RTMGRP_IPV4_MROUTE(0x20),
			RTMGRP_IPV4_ROUTE(0x40),
			RTMGRP_IPV4_RULE(0x80),
			RTMGRP_IPV6_IFADDR(0x100),
			RTMGRP_IPV6_MROUTE(0x200),
			RTMGRP_IPV6_ROUTE(0x400),
			RTMGRP_IPV6_IFINFO(0x800),
			RTMGRP_DECnet_IFADDR(0x1000),
			RTMGRP_DECnet_ROUTE(0x4000),
			RTMGRP_IPV6_PREFIX(0x20000);
			public int value;

			NetlinkMulticastGroup(int value) { this.value = value; }
			public void setValue(int value) { this.value = value; }
			public int getValue() { return this.value; }
			public int maxValue() { return RTMGRP_IPV6_PREFIX.getValue(); }
			public Iterator<NetlinkMulticastGroup> iterator() { return Collections.singleton(this).iterator(); }
		};

	  protected native static int __bind(int socket, int family, byte[] address);
	  
	  /**
	   * Asocia una m�scara de grupos multicast a un socket netlink
	   * 
	   * Si m�scara == 0, este socket s�lo se podr� usar para enviar comandos (y recibir sus respuestaS)
	   * Si m�scara != 0, este socket adem�s recibir� los multicast a dicho grupo enviados desde el n�cleo (pid=0) 
	   * u otros procesos.
	   * 
	   * @param groups M�scara (32 bit=32 grupos) de grupos a suscribir
	   * @exception IllegalStateException If the socket has not been opened first.
	   * @exception IOException If the address cannot be bound.
	   */
	  
	  public void bind(NetlinkAddress direccion)
	    throws IllegalStateException, IOException
	  {
	    if(!isOpen())
	      throw new IllegalStateException();

	    System.out.println("RawsocketNetlink::bind  direccion=" + direccion.pid + " g=" + direccion.grupos + "byte[]=" + direccion.toByteArray());
	    if(__bind(__socket, __family, direccion.toByteArray()) != 0)
	      __throwIOException();
	  }


	  protected native static int __recvfrom1(int socket, byte[] data, int offset,
			  int length, int family);
	  protected native static int __recvfrom2(int socket, byte[] data, int offset,
			  int length, int family, byte[] address);

	  /**
	   * Reads packet data from the socket.  
	   *
	   * @param data The buffer in which to store the packet data.
	   * @param offset The offset into the buffer where the data should
	   *               be stored.
	   * @param length The number of bytes to read.
	   * @param address A byte array in which to store the source address
	   * of the received packet.  It may be null if you don't want to
	   * retrieve the source address.  Otherwise, it must be the right
	   * size to store the address (e.g., 4 bytes for an IPv4 address).
	   * @exception IllegalArgumentException If the offset or lengths are
	   * invalid or if the address parameter is the wrong length.
	   * @exception IOException If an I/O error occurs.
	   * @exception InterruptedIOException If the read operation times out.
	   * @return The number of bytes read.
	   */
	  public int read(byte[] data, int offset, int length, NetlinkAddress address)
	    throws IllegalArgumentException, IOException, InterruptedIOException
	  {
		 byte[] direccion = new byte[16];

		 if(offset < 0 || length < 0 || length > data.length - offset)
	      throw new IllegalArgumentException("Invalid offset or length.");

	    int result = 0;

	    if(getUseSelectTimeout() && !__rtimeout.isZero())
	      result =
	        __select(__socket, true, __rtimeout.seconds, __rtimeout.microseconds);

	    if(result == 0)
	      result =
	        (address == null ?
	         __recvfrom1(__socket, data, offset, length, __family) :
	         __recvfrom2(__socket, data, offset, length, __family, direccion));
	    if ((result == 0) && (address != null))
	    	address.fromByteArray(direccion);
	    if(result < 0) {
	      if(__isErrorEAGAIN())
	        __throwInterruptedIOException();
	      else
	        __throwIOException();
	    }

	    return result;
	  }

	  /** Same as {@code read(data, 0, length, null);} */
	  public int read(byte[] data, int offset, int length)
	    throws IllegalArgumentException, IOException, InterruptedIOException
	  {
	    return read(data, offset, length, (NetlinkAddress)null);
	  }

	  /** Same as {@code read(data, 0, data.length, address);} */
	  public int read(byte[] data, NetlinkAddress address)
	    throws IOException, InterruptedIOException
	  {
	    return read(data, 0, data.length, address);
	  }

	  /** Same as {@code read(address, data, 0, data.length, null);} */
	  public int read(byte[] data)
	    throws IOException, InterruptedIOException
	  {
	    return read(data, 0, data.length, (NetlinkAddress)null);
	  }

	  protected native static int __sendto(int socket, byte[] data, int offset,
	                                     int length, int family, byte[] address);

	  /**
	   * Writes packet data to the socket.  The data should not include
	   * the IP header.  IPv4 ({@link #PF_INET}) sockets may set the
	   * IP_HDRINCL option with {@link #setIPHeaderInclude}, in which case the
	   * packet data should include the IP header.
	   *
	   * @param address The destination to write to.
	   * @param data The buffer from which to copy the packet data.
	   * @param offset The offset into the buffer where the data starts.
	   * @param length The number of bytes to write.
	   * @exception IllegalArgumentException If the offset or lengths are invalid.
	   * @exception IOException If an I/O error occurs.
	   * @exception InterruptedIOException If the write operation times out.
	   * @return The number of bytes written.
	   */
	  public int write(NetlinkAddress address, byte[] data, int offset, int length)
	    throws IllegalArgumentException, IOException, InterruptedIOException 
	  {
	    if(offset < 0 || length < 0 || length > data.length - offset)
	      throw new IllegalArgumentException("Invalid offset or length.");

	    int result = 0;

	    if(getUseSelectTimeout() && !__stimeout.isZero())
	      result =
	        __select(__socket, false, __stimeout.seconds, __stimeout.microseconds);

	    if(result == 0)
	      result =
	        __sendto(__socket, data, offset, length, __family,
	                 address.toByteArray());

	    if(result < 0) {
	      if(__isErrorEAGAIN())
	        __throwInterruptedIOException();
	      else
	        __throwIOException();
	    }

	    return result;
	  }


	  /** Same as {@code write(address, data, 0, data.length);} */
	  public int write(NetlinkAddress address, byte[] data)
	    throws IOException, InterruptedIOException
	  {
	    return write(address, data, 0, data.length);
	  }
 
	  
	  
	  
	  protected native static int __getSizeofType(int tipo);

	  public enum TipoEstructura {
		  SOCKADDR_NL(1),
		  RTMSG(2),
		  RTATTR(3),
		  RTNETXTHOP(4),
		  IFINFOMSG(5),
		  PREFIXMSG(6);
		  
		  protected int value;
		  TipoEstructura(int value) { this.value = value; }
		  int getValue() { return this.value; }
	  }

	  /**
	   * Permite obtener el valor de compilaci�n de ciertos tipos netlink en el sistema actual
	   * 
	   * En las bibliotecas nativas, ciertas estructuras no tienen longitud fija, sino que dependen del tama�o de
	   * los diferentes tipos (short, int, ...) en el sistema actual. El protocolo Netlink, y la implementaci�n
	   * para Linux define macros que, internamente, utilizan el sizeof de estas estructuras para contener las 
	   * longitudes reales del protocolo en dicho sistema.
	   * 
	   * @param tipo  struct XXXX del sistema nativo
	   * @return Longitud en octetos de la estructura en el sistema nativo
	   */
	  public static int getSizeofType(TipoEstructura tipo) {
		  return __getSizeofType(tipo.getValue());
	  }


	  protected native static int __getAlignment();

	  /**
	   * Permite obtener el alineamiento de atributos en Netlink. Habitualmente es 4, pero existe una macro en el
	   * nativo que debe usarse pues puede ser diferente en otras arquitecturas. Esta funci�n permite obtener el 
	   * valor de esta macro en el sistema nativo.
	   * @return Valor (en octetos) de alineamiento de atributos en el sistema nativo.
	   */
	  public static int getAlineamiento() {
		  return __getAlignment();
	  }


	  protected native static long __getPid();

	  /**
	   * Permite obtener el identificador de proceso (PID) de la máquina JAVA actual 
	   * Necesario porque en Netlink se utiliza el PID en el protocolo y en JAVA no existe un 
	   * mecanismo que lo proporcione de forma garantizada (sí algún truco que a veces funciona)

	   * @return PID del proceso máquina virtual java actual
	   */
	  public static long getProcessId() {
		  return __getPid();
	  }

	  /* Conveniencia */
	  /* C�digo en MensajeNetlink, quiz� mejor en clase externa */
		/**
		 * Funci�n auxiliar para serializaci�n en littleEndian si fuera necesario
		 * @param i  Entero(4 octetos) a serializar (bigEndian = JAVA)
		 * @return Entero(4 octetos) resultado de invertir los octetos (littleEndian)
		 */
		protected static int reverseIfNeeded(int i) {
			return (isBigEndianNativeArchitecture ? i : Integer.reverseBytes(i)); 
		}

		/**
		 * Funci�n auxiliar para serializaci�n en littleEndian si fuera necesario
		 * @param s  Short(2 octetos) a serializar (bigEndian = JAVA)
		 * @return Short(2 octetos) resultado de invertir los octetos (littleEndian)
		 */
		protected static short reverseIfNeeded(short s) {
			return (isBigEndianNativeArchitecture ? s : Short.reverseBytes(s)); 
		}

		public class NetlinkAddress { //TODO: Derivar de SocketAddress
			public int pid;
			public int grupos;

			public byte[] toByteArray() {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);

				try {

					dos.writeShort(reverseIfNeeded((short)PF_INET)); //familia (realmente es AF_INET, pero coinciden)
					dos.writeShort(0); //relleno
					dos.writeInt(reverseIfNeeded(pid));
					dos.writeInt(reverseIfNeeded(grupos));

				} catch (IOException e) { // no es I/O (array)
				}
				return baos.toByteArray();
			}
			public void fromByteArray(byte[] direccion) {
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(direccion));

				try {
				int familia = reverseIfNeeded(dis.readShort()); 
				if (familia != PF_INET)
					throw new IllegalArgumentException("Decodificaci�n de direcci�n no contiene familia PF_INET");
				// relleno
				dis.readShort();
				pid = reverseIfNeeded(dis.readInt());
				grupos = reverseIfNeeded(dis.readInt());
				} catch (IOException e) {
					e.printStackTrace();
				}			}
		}
}

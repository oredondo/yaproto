/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Inet4Address {

	/**
	 * Convierte una dirección IP a int
	 */
	public static int toInt(InetAddress inetAddress) {
		/*
		// Obtiene la dirección en byte[]
		byte[] b = inetAddress.getAddress();
		int r = 0; // resultado

		for(int i = 0; i < 4; i++) 
			r += b[i] & ((0xF000) >>> ((3-i) * 8)); //orden de red: b[0] contiene MSB
		return r;*/
		
		int result = 0;
		for (byte b: inetAddress.getAddress())
		{
		    result = result << 8 | (b & 0xFF);
		}
		return result;
		
	}
	
	/**
	 * Genera una InetAddress dado un int
	 */
	public static InetAddress fromInt(int inetAddress) {
		byte[] b = new byte[4];
		
		for(int i = 0; i < 4; i++) 
			b[i] = (byte) (inetAddress >>> ((3-i) * 8) & (0xFF)); //orden de red: b[0] contiene MSB. Desplazamiento SIN signo
		try {
			return InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			// Imposible: el array de octetos tiene .length==4
			return null;
		}
	}

	/**
	 * Dados una IP cualquiera y su máscara de red(en notación CIDR: longitud como cantidad 
	 * de bit '1' consecutivos), obtiene un entero correspondiente a la dirección de la red
	 * Ej: 1.2.3.5/30 devuelve un entero correspondiente a 1.2.3.4  
	 */
	public static int getDireccionRed(InetAddress inetAddress, int longitudPrefijo) {
		int r = Inet4Address.toInt(inetAddress);

		return r & (0x8000 >> longitudPrefijo);  // aplica máscara (desplazamiento CON signo)		
	}

	/**
	 * Dados una IP cualquiera y su máscara de red(en notación CIDR: longitud como cantidad 
	 * de bit '1' consecutivos), obtiene una nueva dirección InetAddress correspondiente a la 
	 * dirección de la red
	 * Ej: 1.2.3.5/30 devuelve un InetAddress correspondiente a 1.2.3.4  
	 */
	public static InetAddress getPrefijo(InetAddress inetAddress, int longitudPrefijo) {
		int r = Inet4Address.getDireccionRed(inetAddress, longitudPrefijo);
		return Inet4Address.fromInt(r); 		
	}

}

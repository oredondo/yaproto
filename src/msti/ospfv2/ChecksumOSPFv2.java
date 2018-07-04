package msti.ospfv2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;


public class ChecksumOSPFv2 {

	/*
	public ChecksumOSPFv2(){
		
	}
	*/
	
	
	/**
	 * Calcula checksum de un mensaje OSPF, suponiendo el campo checksum a 0 y excluyendo campos de autenticacion
	 * @param data El mensaje serializado
	 * @return valor del checksum
	 */
	public static short calcularChecksumOSPF(byte[] data){
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(Arrays.copyOfRange(data, 0, 15));
			outputStream.write(Arrays.copyOfRange(data, 24, data.length));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte mensajeSinAuth[]= outputStream.toByteArray();
		return (short) checksum1(mensajeSinAuth);
		
	}
	
	/**
	 * Comprueba el checksum de un mensaje OSPF, poniendo campo checksum a 0 y excluyendo campos de autenticacion
	 * @param data El mensaje serializado
	 * @return true si el checksum es correcto, false si no lo es
	 */
	public static boolean verificarChecksumOSPF(byte[] data){
		
		//comprobar que el array tiene al menos 24 bytes (cabecera)
		if (data.length>=24){
			//guardar checksum del mensaje
			//short checksumMensajeShort = (short) ( ((data[12] & 0xFF)<<8) | (data[13] & 0xFF) );
			byte[] array = new byte[] {data[12], data[13]};
			ByteBuffer buffer = ByteBuffer.wrap(array);
			short checksumMensajeShort =buffer.getShort();
			
			//poner campos checksum a 0
			data[12]=0;
			data[13]=0;
			//calcular de nuevo checksum del mensaje y si cocincide devolver true
			if(checksumMensajeShort == calcularChecksumOSPF(data)){
				return true;
			}else{
				return false;
			}	
		}	
		return false;
	}
	
	/**
	 * Calcula checksum de un mensaje LSA, suponiendo el campo checksum a 0 y excluyendo el campo LSage
	 * @param data El mensaje serializado
	 * @return valor del checksum
	 */
	public static short calcularChecksumLSA(byte[] data){
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(Arrays.copyOfRange(data, 2, data.length));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte mensajeSinLSAge[]= outputStream.toByteArray();
		return fletcher16(mensajeSinLSAge);

	}
	
	/**
	 * Comprueba el checksum de un mensaje LSA, poniendo campo checksum a 0 y excluyendo el campo LSage
	 * @param data El mensaje serializado
	 * @return true si el checksum es correcto, false si no lo es
	 */
	public static boolean verificarChecksumLSA(byte[] data){
		
		//comprobar que el array tiene al menos 20 bytes (cabecera)
		if (data.length>=20){
			//guardar checksum del mensaje
			byte[] array = new byte[] {data[16], data[17]};
			ByteBuffer buffer = ByteBuffer.wrap(array);
			short checksumMensajeShort =buffer.getShort();
			
			//poner campos checksum a 0
			data[16]=0;
			data[17]=0;
			//calcular de nuevo checksum del mensaje y si cocincide devolver true
			if(checksumMensajeShort == calcularChecksumLSA(data)){
				return true;
			}else{
				return false;
			}	
		}	
		return false;
	}
	
	
	
	/**
	 * Calculates the Fletcher-16 checksum of a byte array
	 * @param data The byte array representation of the data
	 */
	public static short fletcher16(byte[] data){
		short sum1 = 0;
		short sum2 = 0;
		short modulus = 255;

		for (int i = 0; i < data.length; i++){
			sum1 = (short) ((sum1 + data[i]) % modulus);
			sum2 = (short) ((sum2 + sum1) % modulus);
		}
		return (short) ((sum2 << 8) | sum1);
	}

	/**
	 * Calculates the Fletcher-16 checksum of a byte array, using
	 * an optimized implementation of the Fletcher checksum algorithm
	 * @param data The byte array representation of the data
	 */
	public static short fletcher16_opt(byte[] data){
		int length = data.length;
		short sum1 = 0xff;
		short sum2 = 0xff;
		int i = 0;

		while (length > 0) {
			int tlen = (length > 20) ? 20 : length;
			length -= tlen;
			do {
				sum2 += sum1 += data[i];
				i++;
			} while ((--tlen) > 0);
			sum1 = (short) ((sum1 & 0xff) + (sum1 >> 8));
			sum2 = (short) ((sum2 & 0xff) + (sum2 >> 8));
		}
		/* Second reduction step to reduce sums to 8 bits */
		sum1 = (short) ((sum1 & 0xff) + (sum1 >> 8));
		sum2 = (short) ((sum2 & 0xff) + (sum2 >> 8));
		return (short) (sum2 << 8 | sum1);
	}
	
	
	
	public static long checksum1(byte[] buf) {
	    int length = buf.length;
	    int i = 0;

	    long sum = 0;
	    long data;

	    // Handle all pairs
	    while (length > 1) {
	      // Corrected to include @Andy's edits and various comments on Stack Overflow
	      data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
	      sum += data;
	      // 1's complement carry bit correction in 16-bits (detecting sign extension)
	      if ((sum & 0xFFFF0000) > 0) {
	        sum = sum & 0xFFFF;
	        sum += 1;
	      }

	      i += 2;
	      length -= 2;
	    }

	    // Handle remaining byte in odd length buffers
	    if (length > 0) {
	      // Corrected to include @Andy's edits and various comments on Stack Overflow
	      sum += (buf[i] << 8 & 0xFF00);
	      // 1's complement carry bit correction in 16-bits (detecting sign extension)
	      if ((sum & 0xFFFF0000) > 0) {
	        sum = sum & 0xFFFF;
	        sum += 1;
	      }
	    }

	    // Final 1's complement value correction to 16-bits
	    sum = ~sum;
	    sum = sum & 0xFFFF;
	    return sum;

	}
	
	public static long checksum2(byte[] buf, int length) {
	    int i = 0;
	    long sum = 0;
	    while (length > 0) {
	        sum += (buf[i++]&0xff) << 8;
	        if ((--length)==0) break;
	        sum += (buf[i++]&0xff);
	        --length;
	    }

	    return (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF;
	}
	
	
	public static int checksum3(byte[] bytes){ //crc16ccitt
		int crc = 0xFFFF;
		int polynomial = 0xFFFF;
		for (byte b : bytes) {
	            for (int i = 0; i < 8; i++) {
	                boolean bit = ((b   >> (7-i) & 1) == 1);
	                boolean c15 = ((crc >> 15    & 1) == 1);
	                crc <<= 1;
	                if (c15 ^ bit) crc ^= polynomial;
	            }
	    }
		crc &= 0xffff;
		return crc;
	}
	
	
	
	//Onosproject
	
	public static byte[] calculateOspfCheckSum(byte[] packet, int checksumPos1, int checksumPos2) {

        int hexasum = 0;
        for (int i = 0; i < packet.length; i = i + 2) {
            if (i != 12) {
                byte b1 = packet[i];
                String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
                b1 = packet[i + 1];
                String s2 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
                String hexa = s1 + s2;
                int num1 = Integer.parseInt(hexa, 2);
                hexasum = hexasum + num1;
                String convertTo16 = Integer.toHexString(hexasum);
                if (convertTo16.length() > 4) {
                    hexasum = convertToSixteenBits(convertTo16);
                }
            }
        }
        StringBuilder sb = new StringBuilder(Integer.toHexString(hexasum));
        if (sb.length() > 4) {
            sb = sb.reverse();
            StringBuilder s1 = new StringBuilder(sb.substring(0, 4));
            s1 = s1.reverse();
            StringBuilder s2 = new StringBuilder(sb.substring(4, sb.length()));
            s2 = s2.reverse();
            hexasum = Integer.parseInt(s1.toString(), 16) + Integer.parseInt(s2.toString(), 16);
        }
        int finalChecksum = calculateChecksum(hexasum);
        return convertToTwoBytes(finalChecksum);
    }
	
	public static int convertToSixteenBits(String strToConvert) {
        StringBuilder sb = new StringBuilder(strToConvert);
        sb = sb.reverse();
        StringBuilder s1 = new StringBuilder(sb.substring(0, 4));
        s1 = s1.reverse();
        StringBuilder s2 = new StringBuilder(sb.substring(4, sb.length()));
        s2 = s2.reverse();
        int num = Integer.parseInt(s1.toString(), 16) + Integer.parseInt(s2.toString(), 16);
        return num;
    }
	
	private static int calculateChecksum(int hexasum) {

        char[] tempZeros = {'0', '0', '0', '0'};
        StringBuffer hexaAsBinaryStr = new StringBuffer(Integer.toBinaryString(hexasum));
        int length = hexaAsBinaryStr.length();
        while (length > 16) {
            if (hexaAsBinaryStr.length() % 4 != 0) {
                int offset = hexaAsBinaryStr.length() % 4;
                hexaAsBinaryStr.insert(0, tempZeros, 0, 4 - offset);
            }
            StringBuffer hexaStr1 = new StringBuffer(hexaAsBinaryStr.reverse().substring(0, 16));
            String revHexaStr1 = hexaStr1.reverse().toString();
            StringBuffer hexaStr2 = new StringBuffer(hexaAsBinaryStr.reverse());
            StringBuffer hexaStr3 = new StringBuffer(hexaStr2.reverse().substring(16, hexaStr2.length()));
            String revHexaStr3 = hexaStr3.reverse().toString();
            int lastSixteenHexaBits = Integer.parseInt(revHexaStr1, 2);
            int remainingHexaBits = Integer.parseInt(revHexaStr3, 2);
            int totalCheckSum = lastSixteenHexaBits + remainingHexaBits;
            hexaAsBinaryStr = new StringBuffer(Integer.toBinaryString(totalCheckSum));
            length = hexaAsBinaryStr.length();
        }
        if (hexaAsBinaryStr.length() < 16) {
            int count = 16 - hexaAsBinaryStr.length();
            String s = hexaAsBinaryStr.toString();
            for (int i = 0; i < count; i++) {
                s = "0" + s;
            }

            hexaAsBinaryStr = new StringBuffer(s);

        }
        StringBuffer checksum = negate(hexaAsBinaryStr);
        return Integer.parseInt(checksum.toString(), 2);
    }
	
	private static StringBuffer negate(StringBuffer binaryString) {
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                binaryString.replace(i, i + 1, "0");
            } else {
                binaryString.replace(i, i + 1, "1");
            }
        }

        return binaryString;
    }
	
	public static byte[] convertToTwoBytes(int numberToConvert) {

        byte[] numInBytes = new byte[2];
        String s1 = Integer.toHexString(numberToConvert);
        if (s1.length() % 2 != 0) {
            s1 = "0" + s1;
        }
        byte[] hexas = DatatypeConverter.parseHexBinary(s1);
        if (hexas.length == 1) {
            numInBytes[0] = 0;
            numInBytes[1] = hexas[0];
        } else {
            numInBytes[0] = hexas[0];
            numInBytes[1] = hexas[1];
        }
        return numInBytes;
    }
	
	public static byte[] calculateLsaChecksum(byte[] lsaBytes, int lsaChecksumPos1, int lsaChecksumPos2) {

        byte[] tempLsaByte = Arrays.copyOf(lsaBytes, lsaBytes.length);

        int[] checksumOut = {0, 0};
        tempLsaByte[lsaChecksumPos1] = 0;
        tempLsaByte[lsaChecksumPos2] = 0;
        byte[] byteCheckSum = {0, 0};
        for (int i = 2; i < tempLsaByte.length; i++) {
            checksumOut[0] = checksumOut[0] + ((int) tempLsaByte[i] & 0xFF);
            checksumOut[1] = checksumOut[1] + checksumOut[0];
        }
        checksumOut[0] = checksumOut[0] % 255;
        checksumOut[1] = checksumOut[1] % 255;
        int byte1 = (int) ((tempLsaByte.length - lsaChecksumPos1 - 1) * checksumOut[0] - checksumOut[1]) % 255;
        if (byte1 <= 0) {
            byte1 += 255;
        }
        int byte2 = 510 - checksumOut[0] - byte1;
        if (byte2 > 255) {
            byte2 -= 255;
        }

        byteCheckSum[0] = (byte) byte1;
        byteCheckSum[1] = (byte) byte2;

        return byteCheckSum;
    }

	
	
	
	
	
	/*
	public static void main (String[] args){

		byte[] data=  new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x00,
			    0x00, 0x30, (byte)0x9d, (byte)0x00, (byte)0x00, (byte)0x9c, (byte)0x5d, 0x08, 0x00, 0x00, 0x08};
		
		
		System.out.println("data: " + data + "length: " + data.length);
		short checksum = fletcher16_opt(data);
		// Bitmask short to int
		System.out.println("fletcher16opt: "+checksum);
		System.out.println("fletcher16opt: "+Integer.toHexString(checksum & 0xffff));
		
		checksum = fletcher16(data);
		// Bitmask short to int
		System.out.println("fletcher16: "+checksum);
		System.out.println("fletcher16: "+Integer.toHexString(checksum & 0xffff));
		
		/*byte[] checksumOnosprojectOSPF;
		checksumOnosprojectOSPF = calculateOspfCheckSum(data,12,13);
		// Bitmask short to int
		System.out.println("checksumOnosprojectOspf: "+checksumOnosprojectOSPF[0]);
		System.out.println("checksumOnosprojectOspf: "+checksumOnosprojectOSPF[1]);
		
		short val=(short)( ((checksumOnosprojectOSPF[0] & 0xFF)<<8) | (checksumOnosprojectOSPF[1] & 0xFF) );;
		System.out.println("checksumOnosprojectOspf: "+val);
		System.out.println("checksumOnosprojectOspf: "+Integer.toHexString(val & 0xffff));
		
		
		byte[] checksumOnosprojectLSA;
		checksumOnosprojectLSA = calculateLsaChecksum(data,16,17);
		// Bitmask short to int
		System.out.println("checksumOnosprojectLSA: "+checksumOnosprojectLSA[0]);
		System.out.println("checksumOnosprojectLSA: "+checksumOnosprojectLSA[1]);
		
		short val2=(short)( ((checksumOnosprojectLSA[0] & 0xFF)<<8) | (checksumOnosprojectLSA[1] & 0xFF) );;
		System.out.println("checksumOnosprojectLSA: "+val2);
		System.out.println("checksumOnosprojectLSA: "+Integer.toHexString(val2 & 0xffff));
		*/
		
		
		
		/*
		
		
		long checksum2 = checksum1(data);
		// Bitmask short to int
		System.out.println("checksum1: "+checksum2);
		System.out.println("checksum1: "+Long.toHexString(checksum2 & 0xffff));
		
		checksum2 = checksum2(data, data.length);
		// Bitmask short to int
		System.out.println("checksum2: "+checksum2);
		System.out.println("checksum2: "+Long.toHexString(checksum2 & 0xffff));
		
		
		int checksumInt = checksum3(data);
		// Bitmask short to int
		System.out.println("checksum3: "+checksumInt);
		System.out.println("checksum3: "+Integer.toHexString(checksumInt & 0xffff));
		
	}
	*/
	
	 
	
}
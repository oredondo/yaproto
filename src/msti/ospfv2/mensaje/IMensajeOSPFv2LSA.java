
/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.Collections;
import java.util.Iterator;

import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS;

public interface IMensajeOSPFv2LSA {

	/*
	enum TipoLS {
		RouterLinks((byte)1), 
		NetworkLinks((byte)2),
		SumaryLinkIPNetwork((byte)3),
		SumaryLinkASBR((byte)4),
		ASExternalLink((byte)5);
		
		private byte codigo;
		private TipoLS(byte codigo) { this.codigo = codigo; }
		public byte getCodigo() { return codigo; }
		public void setCodigo(byte codigo) { this.codigo = codigo; }
		public Iterator<TipoLS> iterator() { return Collections.singleton(this).iterator(); }
		public static TipoLS getByValue(int value){
		    for(TipoLS en : values())
		        if( en.getCodigo() == value)
		            return en;
		    return null;
		}
	}
	
	public short getLSAge();
	public boolean hasLSAge();
	
	public byte getOptions();
	public boolean hasOptions();
	
	
	
	public int getLinkStateID();
	public boolean hasLinkStateID();
	
	public int getAdvertisingRouter();
	public boolean hasAdvertisingRouter();
	
	public int getLSSequenceNumber();
	public boolean hasLSSequenceNumber();
	
	public short getLSChecksum();
	public boolean hasLSChecksum();
	
	public short getLength();
	public boolean hasLength();*/
	
	public IMensajeOSPFv2LinkStateAdvertisementHeader getHeader();
	public boolean hasHeader();
	
	public TipoLS getLSType();
	public boolean hasLSType();
	
	public boolean getIsLSChecksumOK();
	
	/**
	 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
	 */
	public interface Build {

		public Build setHeader(IMensajeOSPFv2LinkStateAdvertisementHeader header);
		public Build setLSType(TipoLS lSType);
		
		/*public Build setLSType(TipoLS lSType);
		public Build setLSAge(short lSAge);
		public Build setOptions(byte options);*/
		
		/*public Build setLSAge(short lSAge);
		public Build setOptions(byte options);
		public Build setLSType(TipoLS lSType);
		public Build setLinkStateID(int linkStateID);
		public Build setAdvertisingRouter(int advertisingRouter);
		public Build setLSSequenceNumber(int lSSequenceNumber);
		public Build setLSChecksum(short lSChecksum);
		public Build setLength(short length);	*/
	}
}
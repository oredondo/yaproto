/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.util.Collections;
import java.util.Iterator;

public interface IMensajeOSPFv2 {

	
	enum Tipo {
		OSPFHello((byte)1), 
		OSPFDatabaseDescription((byte)2),
		OSPFLinkStateRequest((byte)3),
		OSPFLinkStateUpdate((byte)4),
		OSPFLinkStateAcknowledgment((byte)5);
		
		private byte codigo;
		private Tipo(byte codigo) { this.codigo = codigo; }
		public byte getCodigo() { return codigo; }
		public void setCodigo(byte codigo) { this.codigo = codigo; }
		public Iterator<Tipo> iterator() { return Collections.singleton(this).iterator(); }
		public static Tipo getByValue(int value){
		    for(Tipo en : values())
		        if( en.getCodigo() == value)
		            return en;
		    return null;
		}
	}
	
	public Tipo getTipo();
	public boolean hasTipo();
	
	public byte getVersion();
	public boolean hasVersion();
	
	public short getPacketLength();
	public boolean hasPacketLength();
	
	public int getRouterID();
	public boolean hasRouterID();
	
	public int getAreaID();
	public boolean hasAreaID();
	
	public short getChecksum();
	public boolean hasChecksum();
	
	public short getAutype();
	public boolean hasAutype();
	
	public long getAuthentication();
	public boolean hasAuthentication();
	
	public boolean getIsChecksumOK();
	
	/**
	 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
	 */
	public interface Build {

		public Build setTipo(Tipo tipo);	

		public Build setVersion(byte version);		
	}
}

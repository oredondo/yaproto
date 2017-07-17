/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.mensaje;

import java.util.Collections;
import java.util.Iterator;

public interface IMensajeRIP {

	// TODO: revisar si es posible sacar la implementación de aquí hacia el MensajeRIP, manteniendo el enumerado
	enum Tipo {
		RIPPeticion((byte)1), 
		RIPRespuesta((byte)2);
		
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
	
	/**
	 * Métodos de modificación de atributos. La clase IMensaje, una vez construida, es de sólo lectura
	 */
	public interface Build {

		public Build setTipo(Tipo tipo);	

		public Build setVersion(byte version);		
	}
}

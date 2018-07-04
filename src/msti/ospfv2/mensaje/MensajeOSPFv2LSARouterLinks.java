/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.ChecksumOSPFv2;


public class MensajeOSPFv2LSARouterLinks extends MensajeOSPFv2LSA implements IMensaje, IMensajeOSPFv2LSARouterLinks {
	/* si construyï¿½ y entregï¿½ el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	protected boolean hasVEB=false;
	protected boolean hasNLinks=false;
	protected boolean hasRouterLinks=false;
	
	protected byte veb;
	protected short nlinks;
	protected List<IMensajeOSPFv2LSARouterLinksLinks> routerLinks;
	
	protected MensajeOSPFv2LSARouterLinks() {
		super();
	}

	/* IMensaje (serializaciï¿½n, construcciï¿½n) */
	
	@Override
	public Builder newBuilder() {
		return Builder.crear();
	}

	public IMensajeBuilder toBuilder() {
		/* Crea un nuevo builder */
		Builder builder = newBuilder();
		/* Inicializa el mensaje en el nuevo builder con los campos modificados del mensaje actual */
		builder.mezclarDesde(this);
		return builder;
	}
	
	@Override
	public byte[] writeToByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(this.getLongitudSerializado());
		try {
			super.writeToOutputStream(baos);
			writeToOutputStream(baos);
		} 
		catch (IOException e) {
			// No I/O involucrada sobre byteArray
		}
		return baos.toByteArray();
	}
	
	/**
	 * Serializa este objeto sobre un outputStream proporcionado.
	 * 
	 * Este objeto es un miembro de una uniï¿½n (herencia de MensajeRIP, selector: tipo/version) implï¿½cita en la propia clase
	 * Por ello, siempre incluirï¿½ en la salida los campos selector de la uniï¿½n (tipo/version). 
	 * 
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		//escribe la cabecera
		super.writeToOutputStream(output);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		//escribe veb
		if (hasVEB)
			dos.writeByte(veb);
		//escribe 8 bits a 0
		dos.writeByte(0);
		//escribe nlinks
		if (hasNLinks)
			dos.writeShort(nlinks);
		//escribe routerLinks
		if (hasRouterLinks){
			for (IMensajeOSPFv2LSARouterLinksLinks link: routerLinks)
				((IMensaje) link).writeToOutputStream(dos);				
			
		}
		//colocar lo del baos detrás del mensajeSerializado
		ByteArrayOutputStream baosMensajeCompleto = new ByteArrayOutputStream( );
		baosMensajeCompleto.write(mensajeSerializadoLSA);
		baosMensajeCompleto.write(baos.toByteArray());
		
		mensajeSerializadoLSA= baosMensajeCompleto.toByteArray();
		
		//antes de calcular checksum, calcular y rellenar PacketLengh
		short packetLengthLSARouter = (short) getLongitudSerializado();
		mensajeSerializadoLSA[18] =  (byte)((packetLengthLSARouter >> 8) & 0xff);
		mensajeSerializadoLSA[19] = (byte)(packetLengthLSARouter & 0xff);
		//header.packetLength = packetLengthLSARouter;
		
		//calcular checksum
		//checksum está a 0, calcularlo, rellenarlo (en el array mensaje Serializado)
		short checksumLSARouter = (short) ChecksumOSPFv2.calcularChecksumLSA(mensajeSerializadoLSA);
		mensajeSerializadoLSA[16] =  (byte)((checksumLSARouter >> 8) & 0xff);
		mensajeSerializadoLSA[17] = (byte)(checksumLSARouter & 0xff);
		//header.lSChecksum=checksumLSARouter;
		
		output.write(mensajeSerializadoLSA);

	}

	@Override
	public int getLongitudSerializado() {
		//cabecera (super) + 4 octetos y X más por cada Link(depende del numero de TOS de cada uno)
		
		int longitud=super.getLongitudSerializado();
		longitud+=4;
		
		ListIterator<IMensajeOSPFv2LSARouterLinksLinks> litr=routerLinks.listIterator();
		while(litr.hasNext()){
	            longitud+=((IMensaje) litr.next()).getLongitudSerializado();
	    }	
		
		return longitud;
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2LSA.Builder implements IMensajeBuilder, IMensajeOSPFv2LSARouterLinks, IMensajeOSPFv2LSARouterLinks.Build {
		
		private Builder() {
			//super();
			mensaje = new MensajeOSPFv2LSARouterLinks();
		}

		protected MensajeOSPFv2LSARouterLinks getMensaje() {
			return (MensajeOSPFv2LSARouterLinks)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2LSARouterLinks build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o mï¿½s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2LSARouterLinks getDefaultInstanceforType() {
			MensajeOSPFv2LSARouterLinks mensaje = new MensajeOSPFv2LSARouterLinks();
			mensaje.estaConstruido = true;
			return mensaje;
		}
		
		/**
		 * Internamente es un wrapper a mezclarDesde InputStream
		 */
		@Override
		public Builder mezclarDesde(byte[] datos) {
			/* No lee los campos comunes, pues los lee la clase super() y ella invoca a este mezclarDesde */
			ByteArrayInputStream bis = new ByteArrayInputStream(datos);
			try { 
				mezclarDesde(bis);
			}
			catch (IOException e) {
				// No debe dar excepciï¿½n I/O sobre un array de octetos
			}			
			return this;
		}

		/**
		 * MezclaDesde los campos del elemento. 
		 * 
		 * Este elemento es un miembro de una uniï¿½n (herencia de MensajeRIP, selector: tipo/version) implï¿½cita en la clase
		 * Por ello, si se desea incluir en la mezcla los campos selector de la uniï¿½n (tipo/version) deberï¿½a usar el
		 * mï¿½todo mezclarDesde de la uniï¿½n (en la clase base). 
		 */
		@Override
		public Builder mezclarDesde(InputStream inputStream, byte[] mensajeSerializadoLSA) throws IOException {
						
			DataInputStream dis = new DataInputStream(inputStream);
			dis.read(mensajeSerializadoLSA, 20, inputStream.available()); 
			
			//Comprobar checksum del mensaje con la funcion verificar
			//si está bien, lees, sino checksumOK false
			if(ChecksumOSPFv2.verificarChecksumLSA(mensajeSerializadoLSA)){
				ByteArrayInputStream bais = new ByteArrayInputStream(mensajeSerializadoLSA);
				dis = new DataInputStream(bais);
				//cabecera
				byte[] cabecera = new byte[20];
				dis.read(cabecera, 0, 20);
				
				//veb
				this.setVEB(dis.readByte());
				//8 bits a 0
				dis.readByte();
				//nlinks
				short nLinks=dis.readShort();
				this.setNLinks(nLinks);
				//routerLinks
				for(short i=0;i<nLinks;i++){
					this.addRouterLink(MensajeOSPFv2LSARouterLinksLinks.Builder.crear()
							.mezclarDesde(dis)
							.build());
				}
				setIsLSChecksumOK(true);
				
			}else{
				setIsLSChecksumOK(false);
			}
					
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			if (mensaje instanceof IMensajeOSPFv2LSA)   
				super.mezclarDesde(mensajeOrigen);
			else if (mensaje instanceof IMensajeOSPFv2LSARouterLinks){
				IMensajeOSPFv2LSARouterLinks _mensajeOrigen = (IMensajeOSPFv2LSARouterLinks) mensajeOrigen;

				if (_mensajeOrigen.hasVEB())
					this.setVEB(_mensajeOrigen.getVEB());

				if (_mensajeOrigen.hasNLinks())
					this.setNLinks(_mensajeOrigen.getNLinks());

				if (_mensajeOrigen.hasRouterLinks())
					this.setRouterLinks(_mensajeOrigen.getRouterLinks());
				
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2LSARouterLinks::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LSARouterLinks");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasVEB) && (getMensaje().hasNLinks) && (getMensaje().hasRouterLinks)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public byte getVEB() {
			// TODO Auto-generated method stub
			return getMensaje().veb;
		}
		public short getNLinks() {
			// TODO Auto-generated method stub
			return getMensaje().nlinks;
		}
		public List<IMensajeOSPFv2LSARouterLinksLinks> getRouterLinks() {
			// TODO Auto-generated method stub
			return getMensaje().routerLinks;
		}

		/**
		 * No realiza copia de los objetos IMensajeRipRuta.
		 * Si el mensaje no tenï¿½a lista, establece como lista la indicada. Si tenï¿½a lista, aï¿½ade los elementos de 
		 * la lista indicada a la existente.
		 */
		@Override
		/*public Builder setRIPRutas(List<IMensajeRIPRuta> ripRutas) {
			if (getMensaje().ripRutas == null)
				getMensaje().ripRutas = ripRutas;
			else
				for (IMensajeRIPRuta mensajeRIPRuta: ripRutas)
					getMensaje().ripRutas.add(mensajeRIPRuta);
			getMensaje().hasRutas = true;
			return this;
		}*/
		
		public Builder setVEB(byte veb) {
			getMensaje().veb =veb;
			getMensaje().hasVEB = true;
			return this;
		}
		
		public Builder setNLinks(short nLinks) {
			getMensaje().nlinks =nLinks;
			getMensaje().hasNLinks = true;
			return this;
		}
		
		public Builder setRouterLinks(List<IMensajeOSPFv2LSARouterLinksLinks> routerLinks) {
			getMensaje().routerLinks =routerLinks;
			getMensaje().hasRouterLinks = true;
			return this;
		}

		
		public Builder removeRouterLinks() {
			getMensaje().routerLinks = null;
			getMensaje().hasRouterLinks = false;
			return this;
		}
		
		@Override
		public Builder addRouterLink(IMensajeOSPFv2LSARouterLinksLinks routerLink) {
			if (getMensaje().routerLinks == null)
				getMensaje().routerLinks = new ArrayList<IMensajeOSPFv2LSARouterLinksLinks>();
			getMensaje().routerLinks.add(routerLink);
			getMensaje().hasRouterLinks = true;
			return this;
		}

		/*@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}*/
		
		public boolean hasVEB() {
			return getMensaje().hasVEB;
		}
		public boolean hasNLinks() {
			return getMensaje().hasNLinks;
		}
		public boolean hasRouterLinks() {
			return getMensaje().hasRouterLinks;
		}
		
		//

		/*@Override
		public boolean esPeticionTablaCompleta() {
			return getMensaje().esPeticionTablaCompleta();
		}

		public Builder setPeticionTablaCompleta(boolean b) {
			getMensaje().peticionTablaCompleta = new Boolean(true);
			return this;
		}*/

	}

	/**
	 * TODO: De momento, hasta separar la interfaz IMensajeRIPRuta en dos IMensajeRIPRuta.Getters, 
	 * IMensajeRIPRuta.Setters (usando IMensajeRIPRuta.Builder como tipos pasados, 
	 * y devolviendo en todos los set el mismo objeto Builder para poder encadenar la 
	 * construcciï¿½n en una lï¿½nea de cï¿½digo.
	 * 
	 *  Por ello de momento se repite el cï¿½digo al estar el setter en ambas clases (mensaje y builder)
	 *  Repito el cï¿½digo porque el bueno debe estar en el Builder, pero para no repetir deberï¿½a invocar desde el 
	 *  Builder al set del Mensaje y poner el bueno en el getMensaje(). Asï¿½ en un futuro sï¿½lo hay que borrar el setter
	 *  del mensaje.
	 *
	 */
	
	public byte getVEB() {
		// TODO Auto-generated method stub
		return this.veb;
	}
	public short getNLinks() {
		// TODO Auto-generated method stub
		return this.nlinks;
	}
	public List<IMensajeOSPFv2LSARouterLinksLinks> getRouterLinks() {
		// TODO Auto-generated method stub
		return this.routerLinks;
	}
	
	public boolean hasVEB() {
		return this.hasVEB;
	}
	public boolean hasNLinks() {
		return this.hasNLinks;
	}
	public boolean hasRouterLinks() {
		return this.hasRouterLinks;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--> LSA Router Links: ");

		
		if(hasHeader)
			sb.append(header.toString());
		
		if (hasVEB)
			sb.append(",VEB " + Byte.toString(veb));
		if (hasNLinks)
			sb.append(",NLinks " + Short.toString(nlinks));
		
		if (hasRouterLinks){
			sb.append(",Links ");
			for (IMensajeOSPFv2LSARouterLinksLinks link: routerLinks)
				sb.append(link.toString() + " ");
		}
		return sb.toString();
	}

	/*@Override
	public boolean esPeticionTablaCompleta() {
		if (peticionTablaCompleta == null) {
			boolean esTablaCompleta = false;
			if (this.hasRutas && (this.getRIPRutas().size() == 1)) {
				IMensajeRIPRuta ruta = this.getRIPRutas().get(0);
				if (ruta.hasIdFamiliaDirecciones() && (ruta.getIdFamiliaDirecciones() == 0) && (ruta.getMetrica() >= 16))
					esTablaCompleta = true;
			}
			peticionTablaCompleta = new Boolean(esTablaCompleta);
		}
		return peticionTablaCompleta.booleanValue();
	}*/
}
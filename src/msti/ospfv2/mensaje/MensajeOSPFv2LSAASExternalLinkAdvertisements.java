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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.ChecksumOSPFv2;
import msti.ospfv2.mensaje.MensajeOSPFv2LSARouterLinksLinks.Builder;


public class MensajeOSPFv2LSAASExternalLinkAdvertisements extends MensajeOSPFv2LSA implements IMensaje, IMensajeOSPFv2LSAASExternalLinkAdvertisements {
	/* si construyï¿½ y entregï¿½ el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	protected boolean hasNetworkMask=false;
	protected boolean hasTOSFields=false;

	
	protected int networkMask;
	protected List<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields> tosFields;
	
	protected MensajeOSPFv2LSAASExternalLinkAdvertisements() {
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
		//escribe networkMask
		if (hasNetworkMask)
			dos.writeInt(networkMask);
	
		//escribe tosFields
		if (hasTOSFields){
			for (IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields tosField: tosFields)
				((IMensaje) tosField).writeToOutputStream(dos);
		}
		
		//colocar lo del baos detrás del mensajeSerializado
		ByteArrayOutputStream baosMensajeCompleto = new ByteArrayOutputStream( );
		baosMensajeCompleto.write(mensajeSerializadoLSA);
		baosMensajeCompleto.write(baos.toByteArray());
		
		mensajeSerializadoLSA= baosMensajeCompleto.toByteArray();
		
		//antes de calcular checksum, calcular y rellenar PacketLengh
		short packetLengthLSAAS = (short) getLongitudSerializado();
		mensajeSerializadoLSA[18] =  (byte)((packetLengthLSAAS >> 8) & 0xff);
		mensajeSerializadoLSA[19] = (byte)(packetLengthLSAAS & 0xff);
		//header.packetLength = packetLengthLSAAS;
		
		//calcular checksum
		//checksum está a 0, calcularlo, rellenarlo (en el array mensaje Serializado)
		short checksumLSAAS = (short) ChecksumOSPFv2.calcularChecksumLSA(mensajeSerializadoLSA);
		mensajeSerializadoLSA[16] =  (byte)((checksumLSAAS >> 8) & 0xff);
		mensajeSerializadoLSA[17] = (byte)(checksumLSAAS & 0xff);
		//header.lSChecksum=checksumLSAAS;
		
		output.write(mensajeSerializadoLSA);
		
	}

	@Override
	public int getLongitudSerializado() {
		//cabecera (super) + 4 octetos y 12 más por cada tosFields
		
		return super.getLongitudSerializado() + 4 + 12*tosFields.size();
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2LSA.Builder implements IMensajeBuilder, IMensajeOSPFv2LSAASExternalLinkAdvertisements, IMensajeOSPFv2LSAASExternalLinkAdvertisements.Build {
		
		private Builder() {
			//super();
			mensaje = new MensajeOSPFv2LSAASExternalLinkAdvertisements();
		}

		protected MensajeOSPFv2LSAASExternalLinkAdvertisements getMensaje() {
			return (MensajeOSPFv2LSAASExternalLinkAdvertisements)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2LSAASExternalLinkAdvertisements build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o mï¿½s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2LSAASExternalLinkAdvertisements getDefaultInstanceforType() {
			MensajeOSPFv2LSAASExternalLinkAdvertisements mensaje = new MensajeOSPFv2LSAASExternalLinkAdvertisements();
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
				
				//networkMask
				this.setNetworkMask(dis.readInt());				
				//tosFields			
				while (dis.available() > 0) { 
					this.addTOSField(MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields.Builder.crear()
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
			else if (mensaje instanceof IMensajeOSPFv2LSAASExternalLinkAdvertisements){
				IMensajeOSPFv2LSAASExternalLinkAdvertisements _mensajeOrigen = (IMensajeOSPFv2LSAASExternalLinkAdvertisements) mensajeOrigen;

				if (_mensajeOrigen.hasNetworkMask())
					this.setNetworkMask(_mensajeOrigen.getNetworkMask());

				if (_mensajeOrigen.hasTOSFields())
					this.setTOSFields(_mensajeOrigen.getTOSFields());
				
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2LSAASExternalLinkAdvertisements::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LSAASExternalLinkAdvertisements");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasNetworkMask) && (getMensaje().hasTOSFields)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public int getNetworkMask() {
			// TODO Auto-generated method stub
			return getMensaje().networkMask;
		}
		
		public List<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields> getTOSFields() {
			// TODO Auto-generated method stub
			return getMensaje().tosFields;
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
		
		public Builder setNetworkMask(int networkMask) {
			getMensaje().networkMask =networkMask;
			getMensaje().hasNetworkMask = true;
			return this;
		}
		
		
		public Builder setTOSFields(List<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields> tosFields) {
			getMensaje().tosFields =tosFields;
			getMensaje().hasTOSFields = true;
			return this;
		}
		
		public Builder addTOSField(IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields tosField) {
			if (getMensaje().tosFields == null)
				getMensaje().tosFields = new ArrayList<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields>();
			getMensaje().tosFields.add(tosField);
			getMensaje().hasTOSFields = true;
			return this;
		}
		
		
		public Builder removeTOSFields() {
			getMensaje().tosFields = null;
			getMensaje().hasTOSFields = false;
			return this;		
		}
		

		/*@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}*/
		
		public boolean hasNetworkMask() {
			return getMensaje().hasNetworkMask;
		}
		public boolean hasTOSFields() {
			return getMensaje().hasTOSFields;
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
	
	public int getNetworkMask() {
		// TODO Auto-generated method stub
		return this.networkMask;
	}
	public List<IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields> getTOSFields() {
		// TODO Auto-generated method stub
		return this.tosFields;
	}

	
	public boolean hasNetworkMask() {
		return this.hasNetworkMask;
	}
	public boolean hasTOSFields() {
		return this.hasTOSFields;
	}



	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--> LSA AS External Link Advertisements: ");

		if(hasHeader)
			sb.append(header.toString());
		
		if (hasNetworkMask)
			sb.append(",NetworkMask " + Integer.toString(networkMask));
		
		if (this.tosFields != null)
			sb.append(",TOS ");
			for (IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields tosField: tosFields)
				sb.append(tosField.toString() +" ");

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
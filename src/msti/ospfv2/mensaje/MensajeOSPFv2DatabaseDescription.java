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

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.ChecksumOSPFv2;


public class MensajeOSPFv2DatabaseDescription extends MensajeOSPFv2 implements IMensaje, IMensajeOSPFv2DatabaseDescription {
	/* si construyï¿½ y entregï¿½ el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	protected boolean hasOptions=false;
	protected boolean hasIMMS=false;
	protected boolean hasDDSequenceNumber=false;
	protected boolean hasLSAHeaders=false;
	
	protected byte options;
	protected byte imms;
	protected int dDSequenceNumber;
	protected List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders;
	
	protected MensajeOSPFv2DatabaseDescription() {
		super();
		/* Implï¿½cito en esta clase, estï¿½n estos dos campos de la cabecera. Los fuerza TODO: Quitar */
		tipo = Tipo.OSPFDatabaseDescription;
		hasTipo = true;
		version = 2;
		hasVersion = true;
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
		//16 bits a cero
		dos.writeShort(0);
		//escribe options
		if (hasOptions)
			dos.writeByte(options);
		//5 bits a cero + I + M + MS
		if (hasIMMS)
			dos.writeByte(imms);
		
		//escribe dDSequenceNumber
		if (hasDDSequenceNumber)
			dos.writeInt(dDSequenceNumber);

		//escribe lSAHeaders
		if (hasLSAHeaders){
			for (IMensajeOSPFv2LinkStateAdvertisementHeader lSAHeader: lSAHeaders)
			((IMensaje) lSAHeader).writeToOutputStream(dos);
		}
		
	
		//colocar lo del baos detrás del mensajeSerializado
		ByteArrayOutputStream baosMensajeCompleto = new ByteArrayOutputStream( );
		baosMensajeCompleto.write(mensajeSerializado);
		baosMensajeCompleto.write(baos.toByteArray());
		
		mensajeSerializado= baosMensajeCompleto.toByteArray();
		
		//antes de calcular checksum, calcular y rellenar PacketLengh
		short packetLengthDD = (short) getLongitudSerializado();
		mensajeSerializado[2] =  (byte)((packetLengthDD >> 8) & 0xff);
		mensajeSerializado[3] = (byte)(packetLengthDD & 0xff);
		packetLength = packetLengthDD;
		
		//calcular checksum
		//checksum está a 0, calcularlo, rellenarlo (en el array mensaje Serializado)
		short checksumDD = (short) ChecksumOSPFv2.calcularChecksumOSPF(mensajeSerializado);		
		mensajeSerializado[12] =  (byte)((checksumDD >> 8) & 0xff);
		mensajeSerializado[13] = (byte)(checksumDD & 0xff);
		checksum=checksumDD;
		
		output.write(mensajeSerializado); //longMensajeSerializado, longitudquehaescrito
	}

	@Override
	public int getLongitudSerializado() {
		//cabecera (super) + 8 octetos  y 20 más por cada hasLSAHeaders de la lista
		return super.getLongitudSerializado() + 8 + 20*lSAHeaders.size();
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2.Builder implements IMensajeBuilder, IMensajeOSPFv2DatabaseDescription, IMensajeOSPFv2DatabaseDescription.Build {
		
		private Builder() {
			// No invoca al super()
			mensaje = new MensajeOSPFv2DatabaseDescription();
		}

		protected MensajeOSPFv2DatabaseDescription getMensaje() {
			return (MensajeOSPFv2DatabaseDescription)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2DatabaseDescription build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o mï¿½s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2DatabaseDescription getDefaultInstanceforType() {
			MensajeOSPFv2DatabaseDescription mensaje = new MensajeOSPFv2DatabaseDescription();
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
		public Builder mezclarDesde(InputStream inputStream, byte[] mensajeSerializado) throws IOException {

			DataInputStream dis = new DataInputStream(inputStream);
			dis.read(mensajeSerializado, 24, inputStream.available()); 
						
			//Comprobar checksum del mensaje con la funcion verificar
			//si está bien, lees, sino checksumOK false
			if(ChecksumOSPFv2.verificarChecksumOSPF(mensajeSerializado)){
				ByteArrayInputStream bais = new ByteArrayInputStream(mensajeSerializado);
				dis = new DataInputStream(bais);
				//cabecera
				byte[] cabecera = new byte[24];
				dis.read(cabecera, 0, 24);
				
				//16 bits a 0
				dis.readShort();
				//Options
				this.setOptions(dis.readByte());
				//imms
				this.setIMMS(dis.readByte());
				//dDSequenceNumber
				this.setDDSequenceNumber(dis.readInt());
				//lSAHeaders
				while (dis.available() > 0) { 
					this.addLSAHeader(MensajeOSPFv2LinkStateAdvertisementHeader.Builder.crear()
											.mezclarDesde(dis)
											.build());
				}
				/*while (inputStream.available() > 0) { 
					this.addLSAHeader(MensajeOSPFv2LinkStateAdvertisementHeader.Builder.crear()
											.mezclarDesde(inputStream)
											.build());
				}*/
				setIsChecksumOK(true);
			
			}else{
				setIsChecksumOK(false);
			}
					
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			if (mensaje instanceof IMensajeOSPFv2)   
				super.mezclarDesde(mensajeOrigen);
			else if (mensaje instanceof IMensajeOSPFv2DatabaseDescription){
				IMensajeOSPFv2DatabaseDescription _mensajeOrigen = (IMensajeOSPFv2DatabaseDescription) mensajeOrigen;

				if (_mensajeOrigen.hasOptions())
					this.setOptions(_mensajeOrigen.getOptions());

				if (_mensajeOrigen.hasIMMS())
					this.setIMMS(_mensajeOrigen.getIMMS());

				if (_mensajeOrigen.hasDDSequenceNumber())
					this.setDDSequenceNumber(_mensajeOrigen.getDDSequenceNumber());
				
				if (_mensajeOrigen.hasLSAHeaders())
					this.setLSAHeaders(_mensajeOrigen.getLSAHeaders());				
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2DatabaseDescription::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2DatabaseDescription");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasOptions) && (getMensaje().hasIMMS) &&
					(getMensaje().hasDDSequenceNumber) && (getMensaje().hasLSAHeaders)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public byte getOptions() {
			// TODO Auto-generated method stub
			return getMensaje().options;
		}
		public byte getIMMS() {
			// TODO Auto-generated method stub
			return getMensaje().imms;
		}

		public int getDDSequenceNumber() {
			// TODO Auto-generated method stub
			return getMensaje().dDSequenceNumber;
		}
		public List<IMensajeOSPFv2LinkStateAdvertisementHeader> getLSAHeaders() {
			// TODO Auto-generated method stub
			return getMensaje().lSAHeaders;
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
		
		public Builder setOptions(byte options) {
			getMensaje().options =options;
			getMensaje().hasOptions = true;
			return this;
		}
		public Builder setIMMS(byte imms) {
			getMensaje().imms =imms;
			getMensaje().hasIMMS = true;
			return this;
		}
		
		public Builder setDDSequenceNumber(int dDSequenceNumber) {
			getMensaje().dDSequenceNumber =dDSequenceNumber;
			getMensaje().hasDDSequenceNumber = true;
			return this;
		}

		public Builder setLSAHeaders(List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders) {
			if (getMensaje().lSAHeaders == null){
				getMensaje().lSAHeaders =lSAHeaders;
			}
			else{
				for (IMensajeOSPFv2LinkStateAdvertisementHeader lSAHeader: lSAHeaders)
					getMensaje().lSAHeaders.add(lSAHeader);
			}
			getMensaje().hasLSAHeaders = true;		
			return this;
		}
		
		/*@Override
		public Builder removeRIPRutas() {
			getMensaje().ripRutas = null;
			getMensaje().hasRutas = false;
			return this;
		}*/
		
		public Builder removeLSAHeaders() {
			getMensaje().lSAHeaders = null;
			getMensaje().hasLSAHeaders = false;
			return this;
		}

		/*@Override
		public Builder addRIPRuta(IMensajeRIPRuta mensajeRIPRuta) {
			if (getMensaje().ripRutas == null)
				getMensaje().ripRutas = new ArrayList<IMensajeRIPRuta>(MAXRIPRUTAS);
			getMensaje().ripRutas.add(mensajeRIPRuta);
			getMensaje().hasRutas = true;
			return this;
		}*/
		
		@Override
		public Builder addLSAHeader(IMensajeOSPFv2LinkStateAdvertisementHeader lSAHeader) {
			if (getMensaje().lSAHeaders == null)
				getMensaje().lSAHeaders = new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
			getMensaje().lSAHeaders.add(lSAHeader);
			getMensaje().hasLSAHeaders = true;
			return this;
		}

		/*@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}*/
		
		public boolean hasOptions() {
			return getMensaje().hasOptions;
		}
		public boolean hasIMMS() {
			return getMensaje().hasIMMS;
		}
		public boolean hasDDSequenceNumber() {
			return getMensaje().hasDDSequenceNumber;
		}
		public boolean hasLSAHeaders() {
			return getMensaje().hasLSAHeaders;
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
	
	public byte getOptions() {
		// TODO Auto-generated method stub
		return this.options;
	}
	public byte getIMMS() {
		// TODO Auto-generated method stub
		return this.imms;
	}
	public int getDDSequenceNumber() {
		// TODO Auto-generated method stub
		return this.dDSequenceNumber;
	}
	public List<IMensajeOSPFv2LinkStateAdvertisementHeader> getLSAHeaders() {
		// TODO Auto-generated method stub
		return this.lSAHeaders;
	}
	

	public boolean hasOptions() {
		return this.hasOptions;
	}
	public boolean hasIMMS() {
		return this.hasIMMS;
	}
	public boolean hasDDSequenceNumber() {
		return this.hasDDSequenceNumber;
	}
	public boolean hasLSAHeaders() {
		return this.hasLSAHeaders;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----> Mensaje Database Descritpion OSPFv2: ");
		
		if(hasTipo)
			sb.append("Tipo " +Byte.toString(tipo.getCodigo()));
		if(hasVersion)
			sb.append(",Versión " + Byte.toString(version));
		if(hasPacketLength)
			sb.append(",PacketLength " + Short.toString(packetLength));
		if(hasRouterID)
			sb.append(",RouterID " + Integer.toString(routerID));
		if(hasAreaID)
			sb.append(",AreaID " + Integer.toString(areaID));
		if(hasChecksum)
			sb.append(",Checksum " + Short.toString(checksum));
		if(hasAutype)
			sb.append(",Autype " + Short.toString(autype));
		if(hasAuthentication)
			sb.append(",Authentication " + Long.toString(authentication));
		
		if (hasOptions)
			sb.append(",Options " + Byte.toString(options));
		
		if (hasIMMS)
			sb.append(",IMMS " + Byte.toString(imms));
		
		if (hasDDSequenceNumber)
			sb.append(",DDSequenceNumber " + Integer.toString(dDSequenceNumber));

		
		if (this.lSAHeaders != null){
			sb.append(",LSAHeaders ");
			for (IMensajeOSPFv2LinkStateAdvertisementHeader lSAHeader: lSAHeaders)
				sb.append(lSAHeader.toString() + " ");
		}
		return sb.toString();
	}


}
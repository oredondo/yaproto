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


import java.util.Calendar;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.ConfiguracionOSPFv2;

public class MensajeOSPFv2LinkStateAdvertisementHeader extends MensajeOSPFv2 implements IMensaje, IMensajeOSPFv2LinkStateAdvertisementHeader {
	/* si construyï¿½ y entregï¿½ el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	
	protected boolean hasLSAge=false;
	protected boolean hasOptions=false;
	protected boolean hasLSType=false;
	protected boolean hasLinkStateID=false;
	protected boolean hasAdvertisingRouter=false;
	protected boolean hasLSSequenceNumber=false;
	protected boolean hasLSChecksum=false;
	protected boolean hasLength=false;
	

	protected short lSAge;
	protected byte options;
	protected TipoLS lSType;
	protected int linkStateID;
	protected int advertisingRouter;
	protected int lSSequenceNumber;
	protected short lSChecksum;
	protected short length;
	
	//fecha de inserción, para calcular lSAge
	protected Calendar fechaInsercion = Calendar.getInstance();
	
	private MensajeOSPFv2LinkStateAdvertisementHeader() {
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
		DataOutputStream dos = new DataOutputStream(output);
		
		//calculo lSAge
		Calendar fechaActual = Calendar.getInstance();
		long milis1 = this.fechaInsercion.getTimeInMillis();
		long milis2 = fechaActual.getTimeInMillis();
		short lSAgeReal = (short) (this.lSAge + (milis2-milis1)/1000);
		if(lSAgeReal>ConfiguracionOSPFv2.getInstance().MAX_AGE){
			lSAgeReal = ConfiguracionOSPFv2.getInstance().MAX_AGE;
		}
		
		//escribe LSAge
		if (hasLSAge)
			dos.writeShort(lSAgeReal);
		//escribe options
		if (hasOptions)
			dos.writeByte(options);
		//escribe LSType
		if (hasLSType)
			dos.writeByte(lSType.getCodigo());
		//escribe linkStateID
		if (hasLinkStateID)
			dos.writeInt(linkStateID);
		//escribe advertisingRouter
		if (hasAdvertisingRouter)
			dos.writeInt(advertisingRouter);
		//escribe lSSequenceNumber
		if (hasLSSequenceNumber)
			dos.writeInt(lSSequenceNumber);
		
		//Chceksum y Lengh lo calcula la clase hija
		/*//escribe lSChecksum
		if (hasLSChecksum)
			dos.writeShort(lSChecksum);
		//escribe length
		if (hasLength)
			dos.writeShort(length);
		*/
		dos.writeShort(0);
		dos.writeShort(0);
	}

	@Override
	public int getLongitudSerializado() {
		//20 octetos
		return 20; 		 
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2.Builder implements IMensajeBuilder, IMensajeOSPFv2LinkStateAdvertisementHeader, IMensajeOSPFv2LinkStateAdvertisementHeader.Build {
		
		MensajeOSPFv2LinkStateAdvertisementHeader mensaje;
		
		private Builder() {
			// No invoca al super()
			mensaje = new MensajeOSPFv2LinkStateAdvertisementHeader();
		}

		protected MensajeOSPFv2LinkStateAdvertisementHeader getMensaje() {
			return (MensajeOSPFv2LinkStateAdvertisementHeader)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2LinkStateAdvertisementHeader build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o mï¿½s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2LinkStateAdvertisementHeader getDefaultInstanceforType() {
			MensajeOSPFv2LinkStateAdvertisementHeader mensaje = new MensajeOSPFv2LinkStateAdvertisementHeader();
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
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			
			DataInputStream dis = new DataInputStream(inputStream);
			//LSAge
			this.setLSAge(dis.readShort());
			//Options
			this.setOptions(dis.readByte());
			//LSType
			//Si el tipo no es 1..5, Checksum MAL (así no da error, nos daremos cuenta luego y desecharemos el paquete)
			byte tipo= dis.readByte();
			if(tipo==1 || tipo==2 || tipo==3 || tipo==4 || tipo==5){
				this.setLSType(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.getByValue(tipo));
			}
			//LinkStateID
			this.setLinkStateID(dis.readInt());
			//AdvertisingRouter
			this.setAdvertisingRouter(dis.readInt());
			//LSSequenceNumber
			this.setLSSequenceNumber(dis.readInt());
			//LSChecksum
			this.setLSChecksum(dis.readShort());
			//Length
			this.setLength(dis.readShort());
					
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {			
			if (mensajeOrigen instanceof IMensajeOSPFv2LinkStateAdvertisementHeader){
				IMensajeOSPFv2LinkStateAdvertisementHeader _mensajeOrigen = (IMensajeOSPFv2LinkStateAdvertisementHeader) mensajeOrigen;

				if (_mensajeOrigen.hasLSAge())
					this.setLSAge(_mensajeOrigen.getLSAge());

				if (_mensajeOrigen.hasOptions())
					this.setOptions(_mensajeOrigen.getOptions());

				if (_mensajeOrigen.hasLSType())
					this.setLSType(_mensajeOrigen.getLSType());

				if (_mensajeOrigen.hasLinkStateID())
					this.setLinkStateID(_mensajeOrigen.getLinkStateID());

				if (_mensajeOrigen.hasAdvertisingRouter())
					this.setAdvertisingRouter(_mensajeOrigen.getAdvertisingRouter());
				
				if (_mensajeOrigen.hasLSSequenceNumber())
					this.setLSSequenceNumber(_mensajeOrigen.getLSSequenceNumber());
				
				if (_mensajeOrigen.hasLSChecksum())
					this.setLSChecksum(_mensajeOrigen.getLSChecksum());
				
				if (_mensajeOrigen.hasLength())
					this.setLength(_mensajeOrigen.getLength());	
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2LinkStateAdvertisementHeader::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LinkStateAdvertisementHeader");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasLSAge) && (getMensaje().hasOptions) && (getMensaje().hasLSType) && (getMensaje().hasLinkStateID) &&
					(getMensaje().hasAdvertisingRouter) && (getMensaje().hasLSSequenceNumber) /*&& (getMensaje().hasLSChecksum) && (getMensaje().hasLength)*/); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public short getLSAge() {
			// TODO Auto-generated method stub
			return getMensaje().lSAge;
		}
		public byte getOptions() {
			// TODO Auto-generated method stub
			return getMensaje().options;
		}
		public TipoLS getLSType() {
			// TODO Auto-generated method stub
			return getMensaje().lSType;
		}
		public int getLinkStateID() {
			// TODO Auto-generated method stub
			return getMensaje().linkStateID;
		}
		public int getAdvertisingRouter() {
			// TODO Auto-generated method stub
			return getMensaje().advertisingRouter;
		}
		public int getLSSequenceNumber() {
			// TODO Auto-generated method stub
			return getMensaje().lSSequenceNumber;
		}
		public short getLSChecksum() {
			// TODO Auto-generated method stub
			return getMensaje().lSChecksum;
		}
		public short getLength() {
			// TODO Auto-generated method stub
			return getMensaje().length;
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
		

		
		public Builder setLSAge(short lSAge) {
			
			getMensaje().fechaInsercion = Calendar.getInstance();
			
			getMensaje().lSAge =lSAge;
			getMensaje().hasLSAge = true;
			return this;
		}
		
		public Builder setOptions(byte options) {
			getMensaje().options =options;
			getMensaje().hasOptions = true;
			return this;
		}
		public Builder setLSType(TipoLS lSType) {
			getMensaje().lSType =lSType;
			getMensaje().hasLSType = true;
			return this;
		}
		
		public Builder setLinkStateID(int linkStateID) {
			getMensaje().linkStateID =linkStateID;
			getMensaje().hasLinkStateID = true;
			return this;
		}
		
		public Builder setAdvertisingRouter(int advertisingRouter) {
			getMensaje().advertisingRouter =advertisingRouter;
			getMensaje().hasAdvertisingRouter = true;
			return this;
		}
		
		public Builder setLSSequenceNumber(int lSSequenceNumber) {
			getMensaje().lSSequenceNumber =lSSequenceNumber;
			getMensaje().hasLSSequenceNumber = true;
			return this;
		}

		public Builder setLSChecksum(short lSChecksum) {
			getMensaje().lSChecksum =lSChecksum;
			getMensaje().hasLSChecksum = true;
			return this;
		}
		
		public Builder setLength(short length) {
			getMensaje().length =length;
			getMensaje().hasLength = true;
			return this;
		}

		
		
		public boolean hasLSAge() {
			return getMensaje().hasLSAge;
		}
		public boolean hasOptions() {
			return getMensaje().hasOptions;
		}
		public boolean hasLSType() {
			return getMensaje().hasLSType;
		}
		public boolean hasLinkStateID() {
			return getMensaje().hasLinkStateID;
		}
		public boolean hasAdvertisingRouter() {
			return getMensaje().hasAdvertisingRouter;
		}
		public boolean hasLSSequenceNumber() {
			return getMensaje().hasLSSequenceNumber;
		}
		public boolean hasLSChecksum() {
			return getMensaje().hasLSChecksum;
		}
		public boolean hasLength() {
			return getMensaje().hasLength;
		}

		@Override
		public void incrementarLSAge(short lSAge) {
			
		}

		/*@Override
		public void setNewLSAge(short lSAge) {
			
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
	
	public short getLSAge() {
		Calendar fechaActual = Calendar.getInstance();
		
		long milis1 = this.fechaInsercion.getTimeInMillis();
		long milis2 = fechaActual.getTimeInMillis();
		short lSAgeReal = (short) (this.lSAge + (milis2-milis1)/1000);
		if(lSAgeReal>ConfiguracionOSPFv2.getInstance().MAX_AGE){
			return ConfiguracionOSPFv2.getInstance().MAX_AGE;
		}else{
			return lSAgeReal;
		}
	
	}
	
	/*public void setNewLSAge(short lSAgeNuevo){
		if(lSAgeNuevo>ConfiguracionOSPFv2.getInstance().MAX_AGE){
			this.lSAge =  ConfiguracionOSPFv2.getInstance().MAX_AGE;
		}else{
			this.lSAge=lSAgeNuevo;
		}
	}*/
	
	public void incrementarLSAge(short incremento){	
		if(this.lSAge+incremento > ConfiguracionOSPFv2.getInstance().MAX_AGE){
			this.lSAge =  ConfiguracionOSPFv2.getInstance().MAX_AGE;
		}else{
			this.lSAge+=incremento;
		}
	}

	public byte getOptions() {
		// TODO Auto-generated method stub
		return this.options;
	}
	public TipoLS getLSType() {
		// TODO Auto-generated method stub
		return this.lSType;
	}
	public int getLinkStateID() {
		// TODO Auto-generated method stub
		return this.linkStateID;
	}
	public int getAdvertisingRouter() {
		// TODO Auto-generated method stub
		return this.advertisingRouter;
	}
	public int getLSSequenceNumber() {
		// TODO Auto-generated method stub
		return this.lSSequenceNumber;
	}
	public short getLSChecksum() {
		// TODO Auto-generated method stub
		return this.lSChecksum;
	}
	public short getLength() {
		// TODO Auto-generated method stub
		return this.length;
	}
	
	public boolean hasLSAge() {
		return this.hasLSAge;
	}
	public boolean hasOptions() {
		return this.hasOptions;
	}
	public boolean hasLSType() {
		return this.hasLSType;
	}
	public boolean hasLinkStateID() {
		return this.hasLinkStateID;
	}
	public boolean hasAdvertisingRouter() {
		return this.hasAdvertisingRouter;
	}
	public boolean hasLSSequenceNumber() {
		return this.hasLSSequenceNumber;
	}
	public boolean hasLSChecksum() {
		return this.hasLSChecksum;
	}
	public boolean hasLength() {
		return this.hasLength;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//sb.append("-----> Mensaje Hello OSPFv2");

		if (hasLSAge)
			sb.append("LSAge " + Short.toString(lSAge));
		
		if (hasOptions)
			sb.append(",Options " + Byte.toString(options));
		
		if (hasLSType)
			sb.append(",LSType " + Byte.toString(lSType.getCodigo()));
		
		if (hasLinkStateID)
			sb.append(",LinkStateID " + Integer.toString(linkStateID));
		
		if (hasAdvertisingRouter)
			sb.append(",AdvertisingRouter " + Integer.toString(advertisingRouter));
		
		if (hasLSSequenceNumber)
			sb.append(",LSSequenceNumber " + Integer.toString(lSSequenceNumber));
		
		/*if (hasLSChecksum)
			sb.append(",LSChecksum " + Short.toString(lSChecksum));
		
		if (hasLength)
			sb.append(",length " + Short.toString(length));
		 */

		return sb.toString();
	}

}
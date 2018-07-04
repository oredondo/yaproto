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

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.mensaje.IMensajeOSPFv2.Tipo;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS;
import msti.ospfv2.mensaje.MensajeOSPFv2.Builder;

public class MensajeOSPFv2LSA implements IMensaje, IMensajeOSPFv2LSA {
	/* si construyó y entregó el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	/*protected boolean hasLSAge=false;
	protected boolean hasOptions=false;
	protected boolean hasLSType=false;
	protected boolean hasLinkStateID=false;
	protected boolean hasAdvertisingRouter=false;
	protected boolean hasLSSequenceNumber=false;
	protected boolean hasLSChecksum=false;
	protected boolean hasLength=false;*/
	protected boolean hasHeader=false;	
	protected boolean hasLSType=false;
	
	/*protected short lSAge;
	protected byte options;
	protected TipoLS lSType;
	protected int linkStateID;
	protected int advertisingRouter;
	protected int lSSequenceNumber;
	protected short lSChecksum;
	protected short length;*/
	protected IMensajeOSPFv2LinkStateAdvertisementHeader header;
	protected TipoLS lSType;
	
	protected boolean isLSChecksumOK;
	
	//array serializacion
	byte[] mensajeSerializadoLSA;
	
	protected MensajeOSPFv2LSA() {
	}

	/*
	 * Método factoría de builders según el tipo de mensaje OSPFv2
	 */
	public static IMensajeBuilder crearBuilder(TipoLS lSType) {
		switch (lSType) {
		case RouterLinks:
			return MensajeOSPFv2LSARouterLinks.Builder.crear().setLSType(TipoLS.RouterLinks);
		case NetworkLinks:
			return MensajeOSPFv2LSANetworkLinksAdvertisements.Builder.crear().setLSType(TipoLS.NetworkLinks);
		case SumaryLinkIPNetwork:
			return MensajeOSPFv2LSASumaryLinksAdvertisements.Builder.crear().setLSType(TipoLS.SumaryLinkIPNetwork);
		case SumaryLinkASBR:
			return MensajeOSPFv2LSASumaryLinksAdvertisements.Builder.crear().setLSType(TipoLS.SumaryLinkASBR);
		case ASExternalLink:
			return MensajeOSPFv2LSAASExternalLinkAdvertisements.Builder.crear().setLSType(TipoLS.ASExternalLink);
			
		default: // no posible: enumerado
			return null;
		}
	}

	/* IMensaje (serialización, construcción) */
	
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream(getLongitudSerializado());
		try {
			writeToOutputStream(baos);
		} 
		catch (IOException e) {
			// No I/O involucrada sobre byteArray
		}
		return baos.toByteArray();
	}

	/**
	 * 
	 * Nunca se invoca este método directamente pues no se construyen objetos MensajeOSPFv2 (unión). 
	 * Sólo sirve para herencia (las subclases sí invocan a la superclase
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();	
		DataOutputStream dos = new DataOutputStream(bos);
		
		if (hasHeader){
			((IMensaje) header).writeToOutputStream(dos);
		}
		
		mensajeSerializadoLSA = bos.toByteArray();

	}

	@Override
	public int getLongitudSerializado() {
		return 20;
	}
	
	/*
	 * Clase MensajeOSPFv2.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder implements IMensajeBuilder, IMensajeOSPFv2LSA, IMensajeOSPFv2LSA.Build {
		
		MensajeOSPFv2LSA mensaje;
		/* Builder para el mensaje. Inicialmente es this, pero así puede ocurrir que los analizadores mezclarDesde() 
		 * instancien una subclase diferente de mensaje y por tanto cambien el _builder a uno de la misma subclase
		 */
		Builder _builder;
		//array para serializacion
		byte[] mensajeSerializadoLSA;
		int lonMensajeSerializadoLSA;

		protected Builder() {
			mensaje = new MensajeOSPFv2LSA();
			_builder = this;
		}

		public static Builder crear() {
			return new Builder();
		}

		public static MensajeOSPFv2LSA getDefaultInstanceforType() {
			// MensajeRIP mensaje = new MensajeRIP();  // ya está inicializado completo
			throw new IllegalStateException("Solicitado getDefaultInstanceforType del selector de una unión. Sólo tienen sentido que se instancien las subclases");
		}
		
		@Override
		public MensajeOSPFv2LSA build() {
			if (_builder == this) {
				if (mensaje.estaConstruido)
					throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
				if (! estaCompleto())
					throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o más campos obligatorios sin rellenar)");

				// Marca para que falle siguiente build()
				mensaje.estaConstruido = true;

				return mensaje;
			}
			else  // Usa el nuevo _builder de la subclase
				return _builder.build();
		}

		/**
		 * Internamente es un wrapper a mezclarDesde InputStream
		 */
		@Override
		public Builder mezclarDesde(byte[] datos) {
			ByteArrayInputStream bis = new ByteArrayInputStream(datos);
			try { 
				mezclarDesde(bis);
			}
			catch (IOException e) {
				// No debe dar excepción I/O sobre un array de octetos
			}			
			return _builder;
		}

		//No se usa en OPSFv2
		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			return _builder;
			
		}
		
		
		public Builder mezclarDesde(InputStream inputStream, byte[] mensajeSerializadoLSA) throws IOException {
			DataInputStream dis = new DataInputStream(inputStream);

			if (_builder == this) {
				mensajeSerializadoLSA= new byte[dis.available()]; //solo la clase base hace el new
				dis.read(mensajeSerializadoLSA,0,20);
				lonMensajeSerializadoLSA = 20;
				ByteArrayInputStream bais = new ByteArrayInputStream(mensajeSerializadoLSA);
				dis = new DataInputStream(bais);
				
				//Leemos el contenido
				IMensajeOSPFv2LinkStateAdvertisementHeader header=MensajeOSPFv2LinkStateAdvertisementHeader.Builder.crear()
							.mezclarDesde(dis)
							.build();
				
				_builder.setHeader(header);
				_builder.setLSType(header.getLSType());

			}
			
			// Pide al nuevo builder (ya es de una subclase) que continúe el resto de la mezcla
			_builder.mezclarDesde(inputStream,mensajeSerializadoLSA);

			return _builder;
		}

		/** 
		 * En realidad, al ser una unión de tipos, este método nunca puede recibir un MensajeRIP, pues el builder
		 * de MensajeRIP sólo devuelve instancias de subclases (de esa unión)
	 	 * Este método sólo puede ser invocada desde una clase hija o desde su Builder
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			if (_builder == this) {
				if (mensajeOrigen instanceof IMensajeOSPFv2LSA) {
					IMensajeOSPFv2LSA _mensajeOrigen = (IMensajeOSPFv2LSA)mensajeOrigen;
					if (_mensajeOrigen.hasHeader())
						this.setHeader(_mensajeOrigen.getHeader());
						this.setLSType(_mensajeOrigen.getHeader().getLSType());	
					
					/*if (_mensajeOrigen.hasOptions())
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
						this.setLength(_mensajeOrigen.getLength());*/
				}
				else 
					throw new IllegalArgumentException("IMensajeOSPFv2LSA::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LSA");
			}
			else
				_builder.mezclarDesde(mensaje);

			return _builder;
		}

		@Override
		public boolean estaCompleto() {
			if (_builder == this) 
				return mensaje.hasHeader;
			else
				return _builder.estaCompleto();
		}

		/* IMensajeOSPFv2 */

		
		public Builder setLSType(TipoLS lSType) {
			if (_builder == this) {
				mensaje.lSType = lSType;
				mensaje.hasLSType = true;	

				/* Sólo si es la superclase MensajeOSPFv2 solicita a la factoría un builder (será 
				 * subclase de la clase Builder actual por ser una union) 
				 */
				if (this.getClass() == MensajeOSPFv2LSA.Builder.class) {
					_builder = (Builder)MensajeOSPFv2LSA.crearBuilder(mensaje.lSType);

					/* Pide al nuevo objeto en construcción que se inicialice con los valores comunes ya obtenidos */
					_builder.mezclarDesde(mensaje); 
				}
			}
			else
				_builder.setLSType(lSType);
			return _builder;
		}
		@Override
		public TipoLS getLSType() {
			if (_builder == this)
				return mensaje.getHeader().getLSType();
			else
				return _builder.getHeader().getLSType();
		}
		
		@Override
		public Builder setHeader(IMensajeOSPFv2LinkStateAdvertisementHeader header) {
			if (_builder == this) {
				mensaje.header = header;
				mensaje.hasHeader = true;		
			} 
			else
				_builder.setHeader(header);
			return _builder;
			
		}
		@Override
		public IMensajeOSPFv2LinkStateAdvertisementHeader getHeader() {
			if (_builder == this)
				return mensaje.getHeader();
			else
				return _builder.getHeader();
		}

		

		@Override
		public boolean hasHeader() {
			if (_builder == this)
				return mensaje.hasHeader();
			else 
				return _builder.hasHeader();
		}
		
		@Override
		public boolean hasLSType() {
			if (_builder == this)
				return mensaje.hasLSType();
			else 
				return _builder.hasLSType();
		}
		

		public Builder setIsLSChecksumOK(boolean isLSChecksumOK) {
			if (_builder == this) {
				mensaje.isLSChecksumOK = isLSChecksumOK;			
			}
			else
				_builder.setIsLSChecksumOK(isLSChecksumOK);
			return _builder;
		}
		@Override
		public boolean getIsLSChecksumOK() {
			if (_builder == this)
				return mensaje.getIsLSChecksumOK();
			else
				return _builder.getIsLSChecksumOK();
		}
		

	}
	/* IMensajeOSPFv2 */
	
	@Override
	public IMensajeOSPFv2LinkStateAdvertisementHeader getHeader() {
		return this.header;
	}
	
	@Override
	public boolean hasHeader() {
		return this.hasHeader;
	}

	public TipoLS getLSType() {
		return this.lSType;
	}
	
	@Override
	public boolean hasLSType() {
		return this.hasLSType;
	}
	
	@Override
	public boolean getIsLSChecksumOK() {
		return this.isLSChecksumOK;
	}
	

}

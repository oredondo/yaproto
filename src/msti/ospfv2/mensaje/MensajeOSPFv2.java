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

public class MensajeOSPFv2 implements IMensaje, IMensajeOSPFv2 {
	/* si construyó y entregó el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	protected Tipo tipo;
	protected boolean hasTipo = false;
	
	protected byte version;
	protected boolean hasVersion = false;
	
	protected short packetLength;
	protected boolean hasPacketLength = false;
	
	protected int routerID;
	protected boolean hasRouterID = false;
	
	protected int areaID;
	protected boolean hasAreaID = false;
	
	protected short checksum;
	protected boolean hasChecksum = false;
	
	protected short autype;
	protected boolean hasAutype = false;
	
	protected long authentication;
	protected boolean hasAuthentication = false;
	
	protected boolean isChecksumOK;
	
	//array serializacion
	byte[] mensajeSerializado;
	//int lonMensajeSerializado;
	
	protected MensajeOSPFv2() {
	}

	/*
	 * Método factoría de builders según el tipo de mensaje OSPFv2
	 */
	public static IMensajeBuilder crearBuilder(Tipo tipo) {
		switch (tipo) {
		case OSPFHello:
			return MensajeOSPFv2Hello.Builder.crear().setTipo(Tipo.OSPFHello);
		case OSPFDatabaseDescription:
			return MensajeOSPFv2DatabaseDescription.Builder.crear().setTipo(Tipo.OSPFDatabaseDescription);
		case OSPFLinkStateRequest:
			return MensajeOSPFv2LinkStateRequest.Builder.crear().setTipo(Tipo.OSPFLinkStateRequest);
		case OSPFLinkStateUpdate:
			return MensajeOSPFv2LinkStateUpdate.Builder.crear().setTipo(Tipo.OSPFLinkStateUpdate);
		case OSPFLinkStateAcknowledgment:
			return MensajeOSPFv2LinkStateAcknowledgment.Builder.crear().setTipo(Tipo.OSPFLinkStateAcknowledgment);
			
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
		dos.writeByte(this.version);
		dos.writeByte(this.tipo.getCodigo());
		//PacketLengh se rellena en la clase hija
		//dos.writeShort(this.packetLength);
		dos.writeShort(0);
		dos.writeInt(this.routerID);
		dos.writeInt(this.areaID);
		//Checksum a 0
		//dos.writeShort(this.checksum);
		dos.writeShort(0);
		
		dos.writeShort(this.autype);
		dos.writeLong(this.authentication);
		
		mensajeSerializado = bos.toByteArray();
		//lonMensajeSerializado = this.getLongitudSerializado();
	}

	@Override
	public int getLongitudSerializado() {
		return 24;
	}
	
	/*
	 * Clase MensajeOSPFv2.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder implements IMensajeBuilder, IMensajeOSPFv2, IMensajeOSPFv2.Build {
		
		MensajeOSPFv2 mensaje;
		/* Builder para el mensaje. Inicialmente es this, pero así puede ocurrir que los analizadores mezclarDesde() 
		 * instancien una subclase diferente de mensaje y por tanto cambien el _builder a uno de la misma subclase
		 */
		Builder _builder;
		//array para serializacion
		byte[] mensajeSerializado;
		int lonMensajeSerializado;

		protected Builder() {
			mensaje = new MensajeOSPFv2();
			_builder = this;
		}

		public static Builder crear() {
			return new Builder();
		}

		public static MensajeOSPFv2 getDefaultInstanceforType() {
			// MensajeRIP mensaje = new MensajeRIP();  // ya está inicializado completo
			throw new IllegalStateException("Solicitado getDefaultInstanceforType del selector de una unión. Sólo tienen sentido que se instancien las subclases");
		}
		
		@Override
		public MensajeOSPFv2 build() {
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
				mezclarDesde(bis,null);
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
		
		
		public Builder mezclarDesde(InputStream inputStream, byte[] mensajeSerializado) throws IOException {
			DataInputStream dis = new DataInputStream(inputStream);
			
			if (_builder == this) {
				mensajeSerializado= new byte[dis.available()]; //solo la clase base hace el new
				dis.read(mensajeSerializado,0,24);
				lonMensajeSerializado = 24;
				ByteArrayInputStream bais = new ByteArrayInputStream(mensajeSerializado);
				dis = new DataInputStream(bais);
				
				/* Obtiene los campos comunes de la unión */
				/* Versión */
				_builder.setVersion(dis.readByte()); 
				
				//Si el tipo no es 1..5, MAL
				byte tipo= dis.readByte();
				if(tipo==1 || tipo==2 || tipo==3 || tipo==4 || tipo==5){
					/* Tipo */
					// NOTA: Al cambiar el tipo, cambia el _builder a la subclase adecuada
					_builder.setTipo(IMensajeOSPFv2.Tipo.getByValue(tipo)); // Rellena el enumerado
					 
					/* Versión */
					_builder.setPacketLength(dis.readShort()); // _builder ya ha cambiado a uno de subclase 
					/* Versión */
					_builder.setRouterID(dis.readInt());  
					/* Versión */
					_builder.setAreaID(dis.readInt());  
					/* Versión */
					_builder.setChecksum(dis.readShort()); 
					/* Versión */
					_builder.setAutype(dis.readShort());  
					/* Versión */
					_builder.setAuthentication(dis.readLong());  
					setIsChecksumOK(true);
				}else{
					setIsChecksumOK(false);
					//si falla no tiene sentido seguir
					return _builder;
				}	
			}
			
			// Pide al nuevo builder (ya es de una subclase) que continúe el resto de la mezcla
			_builder.mezclarDesde(inputStream,mensajeSerializado);

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
				if (mensajeOrigen instanceof IMensajeOSPFv2) {
					IMensajeOSPFv2 _mensajeOrigen = (IMensajeOSPFv2)mensajeOrigen;
					if (_mensajeOrigen.hasTipo())
						this.setTipo(_mensajeOrigen.getTipo());						
					if (_mensajeOrigen.hasVersion())
						this.setVersion(_mensajeOrigen.getVersion());
					if (_mensajeOrigen.hasPacketLength())
						this.setPacketLength(_mensajeOrigen.getPacketLength());
					if (_mensajeOrigen.hasRouterID())
						this.setRouterID(_mensajeOrigen.getRouterID());
					if (_mensajeOrigen.hasAreaID())
						this.setAreaID(_mensajeOrigen.getAreaID());
					if (_mensajeOrigen.hasChecksum())
						this.setChecksum(_mensajeOrigen.getChecksum());
					if (_mensajeOrigen.hasAutype())
						this.setAutype(_mensajeOrigen.getAutype());
					if (_mensajeOrigen.hasAuthentication())
						this.setAuthentication(_mensajeOrigen.getAuthentication());
				}
				else 
					throw new IllegalArgumentException("IMensajeOSPFv2::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2");
			}
			else
				_builder.mezclarDesde(mensaje);

			return _builder;
		}

		@Override
		public boolean estaCompleto() {
			if (_builder == this) 
				return (mensaje.hasTipo && mensaje.hasVersion /*&& mensaje.hasPacketLength*/ && mensaje.hasRouterID && mensaje.hasAreaID &&
						/*mensaje.hasChecksum &&*/ mensaje.hasAutype && mensaje.hasAuthentication);
			else
				return _builder.estaCompleto();
		}

		/* IMensajeOSPFv2 */

		@Override
		public Builder setTipo(Tipo tipo) {
			if (_builder == this) {
				mensaje.tipo = tipo;
				mensaje.hasTipo = true;	

				/* Sólo si es la superclase MensajeOSPFv2 solicita a la factoría un builder (será 
				 * subclase de la clase Builder actual por ser una union) 
				 */
				if (this.getClass() == MensajeOSPFv2.Builder.class) {
					_builder = (Builder)MensajeOSPFv2.crearBuilder(mensaje.tipo);

					/* Pide al nuevo objeto en construcción que se inicialice con los valores comunes ya obtenidos */
					_builder.mezclarDesde(mensaje); 
				}
			}
			else
				_builder.setTipo(tipo);
			return _builder;
		}
		@Override
		public Tipo getTipo() {
			if (_builder == this)
				return mensaje.getTipo();
			else
				return _builder.getTipo();
		}

		@Override
		public Builder setVersion(byte version) {
			if (_builder == this) {
				mensaje.version = version;
				mensaje.hasVersion = true;
			} 
			else
				_builder.setVersion(version);
			return _builder;
		}
		@Override
		public byte getVersion() {
			if (_builder == this)
				return mensaje.getVersion();
			else 
				return _builder.getVersion();
		}
		
		public Builder setPacketLength(short packetLength) {
			if (_builder == this) {
				mensaje.packetLength = packetLength;
				mensaje.hasPacketLength = true;				
			}
			else
				_builder.setPacketLength(packetLength);
			return _builder;
		}
		@Override
		public short getPacketLength() {
			if (_builder == this)
				return mensaje.getPacketLength();
			else
				return _builder.getPacketLength();
		}
		
		public Builder setRouterID(int routerID) {
			if (_builder == this) {
				mensaje.routerID = routerID;
				mensaje.hasRouterID = true;				
			}
			else
				_builder.setRouterID(routerID);
			return _builder;
		}
		@Override
		public int getRouterID() {
			if (_builder == this)
				return mensaje.getRouterID();
			else
				return _builder.getRouterID();
		}
		
		public Builder setAreaID(int areaID) {
			if (_builder == this) {
				mensaje.areaID = areaID;
				mensaje.hasAreaID = true;				
			}
			else
				_builder.setAreaID(areaID);
			return _builder;
		}
		@Override
		public int getAreaID() {
			if (_builder == this)
				return mensaje.getAreaID();
			else
				return _builder.getAreaID();
		}
		
		
		public Builder setChecksum(short checksum) {
			if (_builder == this) {
				mensaje.checksum = checksum;
				mensaje.hasChecksum = true;				
			}
			else
				_builder.setChecksum(checksum);
			return _builder;
		}
		@Override
		public short getChecksum() {
			if (_builder == this)
				return mensaje.getChecksum();
			else
				return _builder.getChecksum();
		}
		
		public Builder setAutype(short autype) {
			if (_builder == this) {
				mensaje.autype = autype;
				mensaje.hasAutype = true;				
			}
			else
				_builder.setAutype(autype);
			return _builder;
		}
		@Override
		public short getAutype() {
			if (_builder == this)
				return mensaje.getAutype();
			else
				return _builder.getAutype();
		}
		
		public Builder setAuthentication(long authentication) {
			if (_builder == this) {
				mensaje.authentication = authentication;
				mensaje.hasAuthentication = true;				
			}
			else
				_builder.setAuthentication(authentication);
			return _builder;
		}
		@Override
		public long getAuthentication() {
			if (_builder == this)
				return mensaje.getAuthentication();
			else
				return _builder.getAuthentication();
		}
		
		
		@Override
		public boolean hasTipo() {
			if (_builder == this)
				return mensaje.hasTipo();
			else 
				return _builder.hasTipo();
		}

		@Override
		public boolean hasVersion() {
			if (_builder == this)
				return mensaje.hasVersion();
			else 
				return _builder.hasVersion();
		}
		
		@Override
		public boolean hasPacketLength() {
			if (_builder == this)
				return mensaje.hasPacketLength();
			else 
				return _builder.hasPacketLength();
		}
		
		@Override
		public boolean hasRouterID() {
			if (_builder == this)
				return mensaje.hasRouterID();
			else 
				return _builder.hasRouterID();
		}
		
		@Override
		public boolean hasAreaID() {
			if (_builder == this)
				return mensaje.hasAreaID();
			else 
				return _builder.hasAreaID();
		}
		
		@Override
		public boolean hasChecksum() {
			if (_builder == this)
				return mensaje.hasChecksum();
			else 
				return _builder.hasChecksum();
		}
		
		@Override
		public boolean hasAutype() {
			if (_builder == this)
				return mensaje.hasAutype();
			else 
				return _builder.hasAutype();
		}
		
		@Override
		public boolean hasAuthentication() {
			if (_builder == this)
				return mensaje.hasAuthentication();
			else 
				return _builder.hasAuthentication();
		}
		
		public Builder setIsChecksumOK(boolean isChecksumOK) {
			if (_builder == this) {
				mensaje.isChecksumOK = isChecksumOK;			
			}
			else
				_builder.setIsChecksumOK(isChecksumOK);
			return _builder;
		}
		@Override
		public boolean getIsChecksumOK() {
			if (_builder == this)
				return mensaje.getIsChecksumOK();
			else
				return _builder.getIsChecksumOK();
		}

	}
	/* IMensajeOSPFv2 */
	
	@Override
	public Tipo getTipo() {
		return this.tipo;
	}

	@Override
	public byte getVersion() {
		return this.version;
	}

	@Override
	public boolean hasTipo() {
		return this.hasTipo;
	}

	@Override
	public boolean hasVersion() {
		return this.hasVersion;
	}

	@Override
	public short getPacketLength() {
		return this.packetLength;
	}

	@Override
	public boolean hasPacketLength() {
		return this.hasPacketLength;
	}

	@Override
	public int getRouterID() {
		return this.routerID;
	}

	@Override
	public boolean hasRouterID() {
		return this.hasRouterID;
	}

	@Override
	public int getAreaID() {
		return this.areaID;
	}

	@Override
	public boolean hasAreaID() {
		return this.hasAreaID;
	}

	@Override
	public short getChecksum() {
		return this.checksum;
	}

	@Override
	public boolean hasChecksum() {
		return this.hasChecksum;
	}

	@Override
	public short getAutype() {
		return this.autype;
	}

	@Override
	public boolean hasAutype() {
		return this.hasAutype;
	}

	@Override
	public long getAuthentication() {
		return this.authentication;
	}

	@Override
	public boolean hasAuthentication() {
		return this.hasAuthentication;
	}
	
	@Override
	public boolean getIsChecksumOK() {
		return this.isChecksumOK;
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.util.RawSocketNetlink;

public class MensajeNetlink implements IMensaje, IMensajeNetlink {
	/* si construyó y entregó el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	protected int length;
	protected boolean hasLength;
	
	protected NetlinkMessageType messageType;
	protected boolean hasMessageType;
	
	protected short flags;
	protected boolean hasFlags;
	
	protected int sequenceNumber;
	protected boolean hasSequenceNumber;
	
	protected int processId;
	protected boolean hasProcessId;

	/* arquitectura del sistema nativo: el núcleo habla en nativo (bigEndian o littleEndian), java es bigEndian */
	protected static final boolean isBigEndianNativeArchitecture;
	protected static final int archAlignment;  /* definido en nativo para Netlink */
	
	/**
	 * Obtiene orden de octetos de la arquitectura
	 */
	static { 
		isBigEndianNativeArchitecture = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
		/* Obtiene alineamiento para Netlink */
		archAlignment = RawSocketNetlink.getAlineamiento();	 //TODO: Sacar esto de RawSocketNetlink para evitar la dependencia
	}

	protected MensajeNetlink() {
	}

	/*
	 * Método factoría de builders según el tipo de mensaje Netlink
	 */
	public static IMensajeBuilder crearBuilder(NetlinkMessageType messageType) {
		switch (messageType) {
		case RTM_NEWROUTE:
		case RTM_GETROUTE:
		case RTM_DELROUTE:
			return MensajeNetlinkRoute.Builder.crear().setMessageType(messageType);
		default: // tipos de mensaje no implementados
			throw new UnsupportedOperationException("MensajeNetlink::crearBuilder(): tipo de protocolo Netlink : " + messageType.toString() + " no implementado.");
		}
	}

	/**
	 * Función auxiliar para serialización en littleEndian si fuera necesario
	 * @param i  Entero(4 octetos) a serializar (bigEndian = JAVA)
	 * @return Entero(4 octetos) resultado de invertir los octetos (littleEndian)
	 */
	protected static int reverseIfNeeded(int i) {
		return (isBigEndianNativeArchitecture ? i : Integer.reverseBytes(i)); 
	}

	/**
	 * Función auxiliar para serialización en littleEndian si fuera necesario
	 * @param s  Short(2 octetos) a serializar (bigEndian = JAVA)
	 * @return Short(2 octetos) resultado de invertir los octetos (littleEndian)
	 */
	protected static short reverseIfNeeded(short s) {
		return (isBigEndianNativeArchitecture ? s : Short.reverseBytes(s)); 
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

	protected static int getRelleno(int actual) {
		// Nueva posicion = Avanza alineamiento-1 (y como puede pasarse, fuerza los últimos bit a 0: es decir, trunca a múltiplo del alineamiento)
		// Devuelve la diferencia entre la nueva posición y la actual (el incremento)
		return ((actual + (archAlignment - 1)) & ~(archAlignment -1)) - actual;
	}

	/**
	 * 
	 * 
	 *  0                   1                   2                   3
 	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 *  |                          Length                             |
 	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 *  |            Type              |           Flags              |
 	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 *  |                      Sequence Number                        |
 	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 *  |                      Process ID (PID)                       |
 	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		DataOutputStream dos = new DataOutputStream(output);

		// Longitud = cabecera + relleno + datos
		dos.writeInt(reverseIfNeeded(this.length));
		// Tipo
		dos.writeShort(reverseIfNeeded(this.messageType.getValue()));
		// Flags
		dos.writeShort(reverseIfNeeded(this.flags));
		// Número de secuencia
		dos.writeInt(reverseIfNeeded(this.sequenceNumber));
		// PID
		dos.writeInt(reverseIfNeeded(this.processId)); 
	}

	@Override
	public int getLongitudSerializado() {
		return 16;
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder implements IMensajeBuilder, IMensajeNetlink, IMensajeNetlink.Build {
		
		MensajeNetlink mensaje;
		/* Builder para el mensaje. Inicialmente es this, pero así puede ocurrir que los analizadores mezclarDesde() 
		 * instancien una subclase diferente de mensaje y por tanto cambien el _builder a uno de la misma subclase
		 */
		Builder _builder;

		protected Builder() {
			mensaje = new MensajeNetlink();
			_builder = this;
		}

		protected MensajeNetlink getMensaje() {
			return this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		public static MensajeNetlink getDefaultInstanceforType() {
			// MensajeRIP mensaje = new MensajeRIP();  // ya está inicializado completo
			throw new IllegalStateException("Solicitado getDefaultInstanceforType del selector de una unión. Sólo tienen sentido que se instancien las subclases");
		}

		@Override
		public MensajeNetlink build() {
			if (_builder == this) {
				if (getMensaje().estaConstruido)
					throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
				if (! estaCompleto())
					throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o más campos obligatorios sin rellenar)");

				// Marca para que falle siguiente build()
				getMensaje().estaConstruido = true;

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

		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			return this.mezclarDesde(inputStream, true); /* por defecto, encadena */
		}

		/*
		 * Permite elegir si encadenar a la subclase (por defecto, convertir el builder en el de la subclase)
		 */
		protected Builder mezclarDesde(InputStream inputStream, boolean encadenarSubclase) throws IOException {
			DataInputStream dis = new DataInputStream(inputStream);

			if (_builder == this) {
				/* Obtiene los campos comunes de la unión */

				/* Longitud */
				_builder.setLength(reverseIfNeeded(dis.readInt()));
				System.out.println("msghdr_len=" + _builder.getLength());
				/* Tipo */
				//TODO: verificar EN PDU viene tipo válido (enumerado devolverá null)
				// Si encadenarSubclase es true, cuando se hace el set, se genera un builder para la subclase y se sustituye _builder
				_builder.setMessageType(NetlinkMessageType.getByValue(reverseIfNeeded(dis.readShort())) , 
						encadenarSubclase);  // Cuando termina, el _builder contiene el builder de una subclase
				System.out.println("msghdr_type=" + _builder.getMessageType());
				/* Flags */
				_builder.setFlags(dis.readShort());
				System.out.println("msghdr_flags=" + _builder.getFlags());
				/* Secuencia */
				_builder.setSequenceNumber(reverseIfNeeded(dis.readInt()));
				System.out.println("msghdr_seq=" + _builder.getFlags());
				/* PID */
				_builder.setProcessId(reverseIfNeeded(dis.readInt()));
				System.out.println("msghdr_pid=" + _builder.getProcessId());
				if (encadenarSubclase)
					_builder.mezclarDesde(inputStream); // sólo encadena si se solicita
			}
			else
				_builder.mezclarDesde(inputStream);

			/* No construye .build() el objeto, pues puede seguir en construcción. Devuelve ya el nuevo _builder */
			return _builder;
		}

		@Override
		public Builder mezclarDesde(IMensaje mensajeOrigen) {
			if (_builder == this) {
				System.out.println("MensajeNetlink::mezclarDesde(Imensaje): _builder==this");
				if (mensajeOrigen instanceof MensajeNetlink) {
					System.out.println("MensajeNetlink::mezclarDesde(Imensaje): instanceof MensajeNetlink");
					// Sólo invocable desde subclases o desde _builder, pues esta clase no es instanciable para
					//el usuario
					MensajeNetlink _mensajeOrigen = (MensajeNetlink)mensajeOrigen;
					if (_mensajeOrigen.hasLength()) 
						this.setLength(_mensajeOrigen.getLength());
					if (_mensajeOrigen.hasMessageType()) 
						this.setMessageType(_mensajeOrigen.getMessageType());
					if (_mensajeOrigen.hasProcessId()) 
						this.setProcessId(_mensajeOrigen.getProcessId());
					if (_mensajeOrigen.hasSequenceNumber()) 
						this.setSequenceNumber(_mensajeOrigen.getSequenceNumber());
					if (_mensajeOrigen.hasFlags()) 
						this.setFlags(_mensajeOrigen.getFlags());	
					System.out.println("mensajeNetlink::mezclarDesde: copió:" + this.hasLength()+this.hasMessageType() +  this.hasProcessId() + this.hasSequenceNumber() + this.hasFlags());
				}
				else
					throw new IllegalArgumentException("MensajeNetlink::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeNetlink");
				/* no acción: imposible que sea invocada y reciba un MensajeNetlink pues estos nunca se construyen */
			}
			else
				_builder.mezclarDesde(mensaje);

			return _builder;
		}

		@Override
		public boolean estaCompleto() {
			if (_builder == this)
				return getMensaje().hasMessageType();
			else
				return _builder.estaCompleto();
		}


		/* Accesos no públicos para ciertos campos */
		
		protected Builder setLength(int length) {
			if (_builder == this) {
				getMensaje().length = length;
				getMensaje().hasLength = true;
			}
			else 
				return _builder.setLength(length);
			return this;
		}

		public int getLength() {
			if (_builder == this)
				return getMensaje().length;
			else 
				return _builder.getLength();
		}

		public boolean hasLength() {
			return getMensaje().hasLength;
		}

		/* IMensajeNetlink.Build */

		@Override
		public Builder setMessageType(NetlinkMessageType messageType) {
			return setMessageType(messageType, true); //por defecto, encadena a subclase (es una unión)
		}

		protected Builder setMessageType(NetlinkMessageType messageType, boolean encadenarSubclase) {
			if (_builder == this) {
				/* Rellena el tipo */
				System.out.println("MensajeNetlink:setMessageType()=" + messageType + "esSuperclase=" + (this.getClass() == MensajeNetlink.Builder.class));
				getMensaje().messageType = messageType;
				getMensaje().hasMessageType = true;

				if (encadenarSubclase) {/* Sólo la unión (pero no si la invoca una subclase con super()), al cambiar de tipo solicita a la factoría un builder (subclase de la clase Builder apropiado */
					if (this.getClass() == MensajeNetlink.Builder.class) {
						_builder = (Builder)MensajeNetlink.crearBuilder(getMensaje().messageType);

						/* Pide al nuevo objeto en construcción que se inicialice con los valores ya presentes en el objeto mensaje anterior */
						_builder.mezclarDesde(getMensaje()); 
					}
				}
			}
			else
				_builder.setMessageType(messageType);
			return _builder;
		}

		@Override
		public NetlinkMessageType getMessageType() {
			if (_builder == this)
				return getMensaje().messageType;
			else
				return _builder.getMessageType();
		}

		@Override
		public Builder setFlags(short flags) {
			if (_builder == this) {
				getMensaje().flags = flags;
				getMensaje().hasFlags = true;
			}
			else
				_builder.setFlags(flags);
			return _builder;
		}

		@Override
		public short getFlags() {
			if (_builder == this)
				return getMensaje().flags;
			else
				return _builder.getFlags();
		}

		@Override
		public Builder setFlag(NetlinkFlag flag) {
			if (_builder == this) {
				getMensaje().flags = (short)(getMensaje().flags | flag.getValue());
				getMensaje().hasFlags = true;
			}
			else
				_builder.setFlag(flag);
			return _builder;
		}

		@Override
		public Builder clearFlag(NetlinkFlag flag) {
			if (_builder == this) {
				getMensaje().flags = (short)(getMensaje().flags & ~(flag.getValue()));
				getMensaje().hasFlags = true;
			}
			else
				_builder.setFlag(flag);			
			return _builder;
		}

		@Override
		public Builder setSequenceNumber(int sequenceNumber) {
			if (_builder == this) {
				getMensaje().sequenceNumber = sequenceNumber;
				getMensaje().hasSequenceNumber = true;
			}
			else
				_builder.setSequenceNumber(sequenceNumber);
			return _builder;
		}

		@Override
		public int getSequenceNumber() {
			if (_builder == this)
				return getMensaje().sequenceNumber;
			else
				return _builder.getSequenceNumber();
		}

		@Override
		public Builder setProcessId(int processId) {
			if (_builder == this) {
				getMensaje().processId = processId;
				getMensaje().hasProcessId = true;
			}
			else
				_builder.setProcessId(processId);
			return _builder;
		}

		@Override
		public int getProcessId() {
			if (_builder == this)
				return getMensaje().processId;
			else
				return _builder.getProcessId();
		}

		@Override
		public boolean hasMessageType() {
			if (_builder == this)
				return getMensaje().hasMessageType();
			else
				return _builder.hasMessageType();
		}

		@Override
		public boolean hasFlags() {
			if (_builder == this)
				return getMensaje().hasFlags();
			else
				return _builder.hasFlags();
		}

		@Override
		public boolean hasSequenceNumber() {
			if (_builder == this)
				return getMensaje().hasSequenceNumber();
			else
				return _builder.hasSequenceNumber();
		}

		@Override
		public boolean hasProcessId() {
			if (_builder == this)
				return getMensaje().hasProcessId();
			else
				return _builder.hasProcessId();
		}

	}
	
	/* IMensajeNetlink */
	
	@Override
	public NetlinkMessageType getMessageType() {
		return this.messageType;
	}

	@Override
	public short getFlags() {
		return this.flags;
	}

	@Override
	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	@Override
	public int getProcessId() {
		return this.processId;
	}

	@Override
	public boolean hasMessageType() {
		return this.hasMessageType;
	}

	@Override
	public boolean hasFlags() {
		return this.hasFlags;
	}

	@Override
	public boolean hasSequenceNumber() {
		return this.hasSequenceNumber;
	}

	@Override
	public boolean hasProcessId() {
		return this.hasProcessId;
	}

	public int getLength() {
		return this.length;
	}

	public boolean hasLength() {
		return this.hasLength;
	}

}

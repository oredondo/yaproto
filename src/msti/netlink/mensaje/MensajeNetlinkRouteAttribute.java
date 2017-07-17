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

public class MensajeNetlinkRouteAttribute implements IMensaje, IMensajeNetlinkRouteAttribute {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	protected short length;
	protected boolean hasLength;
	
	protected NetlinkRouteAttributeType attributeType;
	protected boolean hasAttributeType;

	/* Si el tipo es anidado */
	protected boolean isNested = false;
	/* Si los datos van en orden de red */
	protected boolean isNetByteOrder = false;
	
	// TODO: Sacar arquitectura y detalles nativos (alineamiento, pid, sizeof,...) a clase static aparte
	/* arquitectura del sistema nativo: el n�cleo habla en nativo (bigEndian o littleEndian), java es bigEndian */
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

	protected MensajeNetlinkRouteAttribute() {
	}

	/*
	 * M�todo factor�a de builders seg�n el tipo de mensaje Netlink
	 */
	public static IMensajeBuilder crearBuilder(NetlinkRouteAttributeType attributeType) {
		switch (attributeType) {
		case RTA_UNSPEC:
			return MensajeNetlinkRouteAttributeUnspec.Builder.crear();
		case RTA_DST:
			return MensajeNetlinkRouteAttributeDst.Builder.crear();
		case RTA_SRC:
			return MensajeNetlinkRouteAttributePrefsrc.Builder.crear();
		case RTA_IIF:
			return MensajeNetlinkRouteAttributeIif.Builder.crear();
		case RTA_OIF:
			return MensajeNetlinkRouteAttributeOif.Builder.crear();
		case RTA_GATEWAY:
			return MensajeNetlinkRouteAttributeGateway.Builder.crear();
		case RTA_PRIORITY:
			return MensajeNetlinkRouteAttributePriority.Builder.crear();
		case RTA_PREFSRC:
			return MensajeNetlinkRouteAttributeSrc.Builder.crear();
//		case RTA_METRICS:
//			return MensajeNetlinkRouteAttributeMetrics.Builder.crear();
//		case RTA_MULTIPATH:
//			return MensajeNetlinkRouteAttributeMultipath.Builder.crear();
//		case RTA_FLOW:
//			return MensajeNetlinkRouteAttributeFlow.Builder.crear();
//		case RTA_CACHEINFO:
//			return MensajeNetlinkRouteAttributeCacheInfo.Builder.crear();
		case RTA_TABLE:
			return MensajeNetlinkRouteAttributeTable.Builder.crear();
		default: // tipos de mensaje no implementados
			throw new UnsupportedOperationException("MensajeNetlinkRouteAttribute::crearBuilder(): tipo: " + attributeType + " no implementado.");
		}
	}

	/**
	 * Funci�n auxiliar para serializaci�n en littleEndian si fuera necesario
	 * 
	 * 
	 * @param i  Entero(4 octetos) a serializar (bigEndian = JAVA)
	 * @return Entero(4 octetos) resultado de invertir los octetos (posible littleEndian)
	 */
	protected static int reverseIfNeeded(int i) {
		return (isBigEndianNativeArchitecture ? i : Integer.reverseBytes(i)); 
	}

	/**
	 * Funci�n auxiliar para serializaci�n en littleEndian si fuera necesario
	 * @param s  Short(2 octetos) a serializar (bigEndian = JAVA)
	 * @return Short(2 octetos) resultado de invertir los octetos (posible littleEndian)
	 */
	protected static short reverseIfNeeded(short s) {
		return (isBigEndianNativeArchitecture ? s : Short.reverseBytes(s)); 
	}


	/* IMensaje (serializaci�n, construcci�n) */
	
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
		// Nueva posicion = Avanza alineamiento-1 (y como puede pasarse, fuerza los �ltimos bit a 0: es decir, trunca a m�ltiplo del alineamiento)
		// Devuelve la diferencia entre la nueva posici�n y la actual (el incremento)
		return ((actual + (archAlignment - 1)) & ~(archAlignment -1)) - actual;
	}

	/**
	 * Serializa un atributo
	 * 
	 * nla_type (16 bits)
	 * +---+---+-------------------------------+
	 * | N | O | Attribute Type                |
	 * +---+---+-------------------------------+
	 * N := Carries nested attributes
	 * O := Payload stored in network byte order
	 *
	 * Note: The N and O flag are mutually exclusive.
	 */

	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		DataOutputStream dos = new DataOutputStream(output);

		// Longitud (=L+T+V)
		dos.writeShort(reverseIfNeeded(this.getLength()));
		// Tipo
		dos.writeShort(reverseIfNeeded(this.attributeType.getValue()));
	}

	@Override
	public int getLongitudSerializado() {
		return 4; 
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder implements IMensajeBuilder, IMensajeNetlinkRouteAttribute, IMensajeNetlinkRouteAttribute.Build {
		
		MensajeNetlinkRouteAttribute mensaje;
		/* Builder para el mensaje. Inicialmente es this, pero as� puede ocurrir que los analizadores mezclarDesde() 
		 * instancien una subclase diferente de mensaje y por tanto cambien el _builder a uno de la misma subclase
		 */
		Builder _builder;

		protected Builder() {
			mensaje = new MensajeNetlinkRouteAttribute();
			_builder = this;
		}

		protected MensajeNetlinkRouteAttribute getMensaje() {
			return this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		public static MensajeNetlinkRouteAttribute getDefaultInstanceforType() {
			throw new IllegalStateException("Solicitado getDefaultInstanceforType del selector de una uni�n. S�lo tienen sentido que se instancien las subclases");
		}

		@Override
		public MensajeNetlinkRouteAttribute build() {
			if (_builder == this) {
				if (getMensaje().estaConstruido)
					throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
				if (! estaCompleto())
					throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

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
				// No debe dar excepci�n I/O sobre un array de octetos
			}			
			return _builder;
		}

		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			DataInputStream dis = new DataInputStream(inputStream);
			boolean encadenarSubclase = true;

			if (_builder == this) {
				/* Obtiene los campos comunes de la uni�n LTV */

				/* Longitud (2 octetos, orden nativo) de atributo completo LTV --incluye la propia L-- */
				_builder.setLength(reverseIfNeeded(dis.readShort()));
				System.out.println("attr_len=" + _builder.getLength());

				/* Tipo */
				//TODO: verificar EN PDU viene un tipo v�lido (enumerado devolver� null)
				short campoTipo = reverseIfNeeded(dis.readShort());
				if ((campoTipo & NLA_F_NESTED) != 0)
					getMensaje().isNested = true;
				if ((campoTipo & NLA_F_NET_BYTEORDER) != 0)
					getMensaje().isNetByteOrder = true;
				campoTipo = (short)(campoTipo & NLA_TYPE_MASK);	// pone a 0 los dos bit de mayor peso y se queda con los 6 restantes

				if (campoTipo >= NetlinkRouteAttributeType.RTA_MAX.getValue()) {
					// Si el tipo es desconocido, asigna la marca dummy
					_builder.setAttributeType(NetlinkRouteAttributeType.RTA_MAX, false); /* no encadena */
					encadenarSubclase = false;
				}
				else 
					_builder.setAttributeType(NetlinkRouteAttributeType.getByValue(campoTipo));  // Cuando termina, el _builder contiene el builder de una subclase
				System.out.println("attr_type=" + _builder.getAttributeType());

				
				if (encadenarSubclase)
					_builder.mezclarDesde(inputStream);				
			}
			else
				_builder.mezclarDesde(inputStream);

			/* No construye .build() el objeto, pues puede seguir en construcci�n. Devuelve ya el nuevo _builder */
			return _builder;
		}

		@Override
		public Builder mezclarDesde(IMensaje mensajeOrigen) {
			if (_builder == this) {
				if (mensajeOrigen instanceof MensajeNetlinkRouteAttribute) {
					// S�lo invocable desde subclases o desde _builder, pues esta clase no es instanciable para
					//el usuario
					MensajeNetlinkRouteAttribute _mensajeOrigen = (MensajeNetlinkRouteAttribute)mensajeOrigen;
					if (_mensajeOrigen.hasLength()) 
						this.setLength(_mensajeOrigen.getLength());
					if (_mensajeOrigen.hasAttributeType()) 
						this.setAttributeType(_mensajeOrigen.getAttributeType());
				}
				else
					throw new IllegalArgumentException("MensajeNetlinkRouteAttribute::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeNetlink");
				/* no acci�n: imposible que sea invocada y reciba un MensajeNetlink pues estos nunca se construyen */
			}
			else
				_builder.mezclarDesde(mensaje);

			return _builder;
		}

		@Override
		public boolean estaCompleto() {
			if (_builder == this) {
				return this.hasAttributeType();
			}
			else
				return _builder.estaCompleto();
		}


		/* Accesos no p�blicos para ciertos campos */
		
		public Builder setLength(short length) {
			if (_builder == this) {
				getMensaje().length = length;
				getMensaje().hasLength = true;
			}
			else 
				return _builder.setLength(length);
			return this;
		}

		public short getLength() {
			if (_builder == this)
				return getMensaje().length;
			else 
				return _builder.getLength();
		}

		public boolean hasLength() {
			return getMensaje().hasLength;
		}

		/* IMensajeNetlinkRouteAttribute.Build */

		@Override
		public Builder setAttributeType(NetlinkRouteAttributeType attributeType) {
			return setAttributeType(attributeType, true);  //encadena a subclase
		}

		/**
		 * Cambio el atributo y, en base al tipo, encadena (o no) construyendo el builder de una subclase
		 * @param attributeType Tipo
		 * @param encadenarSubclase  true= encadena, false=no encadena
		 * @return este Builder
		 */
		protected Builder setAttributeType(NetlinkRouteAttributeType attributeType, boolean encadenarSubclase) {
			if (_builder == this) {
				/* Rellena el tipo */
				getMensaje().attributeType = attributeType;
				getMensaje().hasAttributeType = true;

				if (encadenarSubclase) {
					/* S�lo la uni�n (pero no si la invoca una subclase con super()), al cambiar de tipo solicita a la factor�a un builder (subclase de la clase Builder apropiado */
					if (this.getClass() == MensajeNetlinkRouteAttribute.Builder.class) {
						// Si es anidado crea el contenedor espec�fico con ese formato
						if (getMensaje().isNested)
							_builder = (Builder)MensajeNetlinkRouteNestedAttribute.Builder.crear();
						else 
							_builder = (Builder)MensajeNetlinkRouteAttribute.crearBuilder(getMensaje().attributeType);

						/* Pide al nuevo objeto en construcci�n que se inicialice con los valores ya presentes en el objeto mensaje anterior */
						_builder.mezclarDesde(getMensaje()); 
					}
				}
			}
			else
				_builder.setAttributeType(attributeType);
			return _builder;
		}

		@Override
		public NetlinkRouteAttributeType getAttributeType() {
			if (_builder == this)
				return getMensaje().attributeType;
			else
				return _builder.getAttributeType();
		}


		@Override
		public boolean hasAttributeType() {
			if (_builder == this)
				return getMensaje().hasAttributeType();
			else
				return _builder.hasAttributeType();
		}


	}
	
	/* IMensajeNetlink */
	
	@Override
	public NetlinkRouteAttributeType getAttributeType() {
		return this.attributeType;
	}


	@Override
	public boolean hasAttributeType() {
		return this.hasAttributeType;
	}


	public short getLength() {
		return this.length;
	}

	public boolean hasLength() {
		return this.hasLength;
	}

}

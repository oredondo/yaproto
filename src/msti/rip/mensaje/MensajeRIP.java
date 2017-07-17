/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;

public class MensajeRIP implements IMensaje, IMensajeRIP {
	/* si construyó y entregó el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	protected Tipo tipo;
	protected boolean hasTipo = false;
	
	protected byte version;
	protected boolean hasVersion = false;
	
	protected MensajeRIP() {
	}

	/*
	 * Método factoría de builders según el tipo de mensaje RIP
	 */
	public static IMensajeBuilder crearBuilder(Tipo tipo) {
		switch (tipo) {
		case RIPPeticion:
			return MensajeRIPPeticion.Builder.crear().setTipo(Tipo.RIPPeticion);
		case RIPRespuesta:
			return MensajeRIPRespuesta.Builder.crear().setTipo(Tipo.RIPRespuesta);
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
	 * Nunca se invoca este método directamente pues no se construyen objetos MensajeRIP (unión). 
	 * Sólo sirve para herencia (las subclases sí invocan a la superclase
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		DataOutputStream dos = new DataOutputStream(output);  // NOTA: estaCompleto... o no se hubiera construido
		dos.writeByte(this.tipo.getCodigo());
		dos.writeByte(this.version);
		dos.writeShort(0);  // 16 bit a cero
	}

	@Override
	public int getLongitudSerializado() {
		return 1;
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder implements IMensajeBuilder, IMensajeRIP, IMensajeRIP.Build {
		
		MensajeRIP mensaje;
		/* Builder para el mensaje. Inicialmente es this, pero así puede ocurrir que los analizadores mezclarDesde() 
		 * instancien una subclase diferente de mensaje y por tanto cambien el _builder a uno de la misma subclase
		 */
		Builder _builder;

		protected Builder() {
			mensaje = new MensajeRIP();
			_builder = this;
		}

		public static Builder crear() {
			return new Builder();
		}

		public static MensajeRIP getDefaultInstanceforType() {
			// MensajeRIP mensaje = new MensajeRIP();  // ya está inicializado completo
			throw new IllegalStateException("Solicitado getDefaultInstanceforType del selector de una unión. Sólo tienen sentido que se instancien las subclases");
		}
		
		@Override
		public MensajeRIP build() {
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

		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			DataInputStream dis = new DataInputStream(inputStream);

			if (_builder == this) {
				/* Obtiene los campos comunes de la unión */

				/* Tipo */
				// NOTA: Al cambiar el tipo, cambia el _builder a la subclase adecuada
				_builder.setTipo(IMensajeRIP.Tipo.getByValue(dis.readByte())); // Rellena el enumerado TODO: verificar llega un tipo válido
				/* Versión */
				_builder.setVersion(dis.readByte());  // _builder ya ha cambiado a uno de subclase
				/* Relleno */
				dis.readShort(); // lee 16 bit (a cero), y los descarta
			}
			// Pide al nuevo builder (ya es de una subclase) que continúe el resto de la mezcla
			_builder.mezclarDesde(inputStream);

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
				if (mensajeOrigen instanceof IMensajeRIP) {
					IMensajeRIP _mensajeOrigen = (IMensajeRIP)mensajeOrigen;
//					if (_mensajeOrigen.hasTipo())
//						this.setTipo(_mensajeOrigen.getTipo());						
					if (_mensajeOrigen.hasVersion())
						this.setVersion(_mensajeOrigen.getVersion());						
				}
				else 
					throw new IllegalArgumentException("MensajeRIP::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeRIP");
			}
			else
				_builder.mezclarDesde(mensaje);

			return _builder;
		}

		@Override
		public boolean estaCompleto() {
			if (_builder == this) 
				return (mensaje.hasTipo && mensaje.hasVersion);
			else
				return _builder.estaCompleto();
		}

		/* IMensajeRIP */

		@Override
		public Builder setTipo(Tipo tipo) {
			if (_builder == this) {
				mensaje.tipo = tipo;
				mensaje.hasTipo = true;	

				/* Sólo si es la superclase MensajeRIP solicita a la factoría un builder (será 
				 * subclase de la clase Builder actual por ser una union) 
				 */
				if (this.getClass() == MensajeRIP.Builder.class) {
					_builder = (Builder)MensajeRIP.crearBuilder(mensaje.tipo);

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

	}
	/* IMensajeRIP */
	
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

}

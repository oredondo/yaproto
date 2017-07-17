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
import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;

public class MensajeNetlinkControl extends MensajeNetlink implements IMensaje, IMensajeNetlinkControl {
	/* si construyó y entregó el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	protected NetlinkControlCode code;
	protected boolean hasCode = false;
	
	protected MensajeNetlink referencedNetlinkMessage;
	protected boolean hasReferencedNetlinkMessage = false;
	
	protected MensajeNetlinkControl() {
		super();		
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream(super.getLongitudSerializado() + getLongitudSerializado());
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
	 * Este objeto es un miembro de una unión (herencia de MensajeRIP, selector: tipo/version) implícita en la propia clase
	 * Por ello, siempre incluirá en la salida los campos selector de la unión (tipo/version). 
	 * 
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		DataOutputStream dos = new DataOutputStream(output);
		int escritos;
		
		// Selectores de la unión (cabecera Netlink)
		super.writeToOutputStream(output);
		escritos = super.getLongitudSerializado(); // se podría obtener tb del output

		/* Alineamiento tras cabecera */
		for (int i = getRelleno(escritos); i > 0; i--, escritos++) 
			dos.writeByte(0);

		// Mensaje control
				
		/* código de control */
		dos.writeInt(this.getCode().getValue());
		escritos += 4;

		/* Cabecera MensajeNetlink */
		this.referencedNetlinkMessage.writeToOutputStream(output);
		escritos += this.referencedNetlinkMessage.getLongitudSerializado();

		/* Alineamiento tras mensaje */
		for (int i = getRelleno(escritos); i > 0; i--, escritos++) 
			dos.writeByte(0);
	}

	/**
	 * Longitud del mensaje sin incluir el posible último relleno final
	 * @return
	 */
	protected int getLongitudMensaje() {
		int total;

		// Cabecera + relleno
		total = super.getLongitudSerializado();
		total += getRelleno(total);

		// Mensaje + relleno
		total += 4;
		total += this.referencedNetlinkMessage.getLongitudSerializado();  

		return total;
	}
	
	/**
	 * Longitud del mensaje que efectivamente se serializa (incluye el relleno final)
	 */
	@Override
	public int getLongitudSerializado() {
		int longitudMensaje = getLongitudMensaje();  // Mensaje sin relleno final
		
		longitudMensaje += getRelleno(longitudMensaje);  //Añade el último relleno tras el último atributo
		return longitudMensaje;
	}

	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeNetlink.Builder implements IMensajeBuilder, IMensajeNetlinkControl, IMensajeNetlinkControl.Build {
		
		private Builder() {
			_builder = this;
			mensaje = new MensajeNetlinkControl(); //inicializa el mensaje heredado con el tipo adecuado
		}

		/* Sobreescribe para cambiar el tipo devuelto y usarlo tipado donde se requiera */
		@Override
		protected MensajeNetlinkControl getMensaje() {
			return (MensajeNetlinkControl)this.mensaje;
		}

		
		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeNetlinkControl build() {
			if (mensaje.estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o más campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			mensaje.estaConstruido = true;

			return getMensaje();
		}

		public static MensajeNetlinkControl getDefaultInstanceforType() {
			// TODO: Petición de tabla de rutas puede ser el mensaje por defecto
			MensajeNetlinkControl mensaje = new MensajeNetlinkControl();
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
				// No debe dar excepción I/O sobre un array de octetos
			}			
			return this;
		}

		/**
		 * MezclaDesde los campos del elemento. 
		 * 
		 * Este elemento es un miembro de una unión (herencia de MensajeRIP, selector: tipo/version) implícita en la clase
		 * Por ello, si se desea incluir en la mezcla los campos selector de la unión (tipo/version) debería usar el
		 * método mezclarDesde de la unión (en la clase base). 
		 */
		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			DataInputStream dis = new DataInputStream(inputStream);
			// Antes se leyó la cabecera mensaje netlink
			int leidos = 16;

			/* Salta el relleno de alineamienti, si hubiera */
			for (int i = getRelleno(leidos); i > 0; i--, leidos++) 
				dis.readByte();

			/* Datos: mensaje control */

			/* familia de direcciones */
			this.setCode(NetlinkControlCode.getByValue(reverseIfNeeded(dis.readInt())));
			leidos += 4;
			/* Cabecera del mensaje referenciado */
			this.setReferencedNetlinkMessage(MensajeNetlink.Builder.crear()
					.mezclarDesde(inputStream, false).build()); /* con "false", no encadena a subclase, se queda con la cabecera */
			leidos += getMensaje().getReferencedNetlinkMessage().getLongitudSerializado();

			/* Alineamiento */
			for (int i = getRelleno(leidos); i > 0; i--, leidos++) 
				dis.readByte();

			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public Builder mezclarDesde(IMensaje mensajeOrigen) {
			// Copia desde la base los campos comunes
			if (mensaje instanceof IMensajeNetlink) {
				System.out.println("Mezclar desde base (es Imensajenetlink o subclase");
				super.mezclarDesde(mensajeOrigen);
			}
			else if (mensaje instanceof IMensajeNetlinkRoute) {
				// Campos comunes ya copiados en el instanceof anterior

				System.out.println("Mezclar resto (es Imensajenetlinkroute o subclase)");

				// Copia los campos específicos
				MensajeNetlinkControl _mensajeOrigen = (MensajeNetlinkControl)mensajeOrigen;
				if (_mensajeOrigen.hasCode())
					this.setCode(_mensajeOrigen.getCode());
				if (_mensajeOrigen.hasReferencedNetlinkMessage())
					this.setReferencedNetlinkMessage(_mensajeOrigen.getReferencedNetlinkMessage());
			}
			else 
				throw new IllegalArgumentException("MensajeNetlinkRoute::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeRIP");

			return this;
		}


		@Override
		public boolean estaCompleto() {
			return (super.estaCompleto() &&
					getMensaje().hasCode());
		}

		/* IMensajeNetlinkControl */


		@Override
		public Builder setCode(NetlinkControlCode code) {
			getMensaje().code = code;
			getMensaje().hasCode = true;			
			return this;
		}

		@Override
		public Builder setReferencedNetlinkMessage(MensajeNetlink referencedNetlinkMessage) {
			getMensaje().referencedNetlinkMessage = referencedNetlinkMessage;
			getMensaje().hasReferencedNetlinkMessage = true;			
			return this;
		}


		@Override
		public NetlinkControlCode getCode() {
			return getMensaje().code;
		}

		@Override
		public MensajeNetlink getReferencedNetlinkMessage() {
			return getMensaje().referencedNetlinkMessage;
		}

		@Override
		public boolean hasCode() {
			return getMensaje().hasCode;
		}

		@Override
		public boolean hasReferencedNetlinkMessage() {
			return getMensaje().hasReferencedNetlinkMessage;
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----> MensajeNetlinkControl");

		sb.append("\n Cabecera:");
		sb.append("  length="); 		sb.append(this.getLength());
		sb.append("; type="); 			sb.append(this.getMessageType());
		sb.append("; pid="); 			sb.append(this.getProcessId());
		sb.append("; seq="); 			sb.append(this.getSequenceNumber());
		sb.append("; flags=");			sb.append(this.getFlags());

		sb.append("\n Mensaje control:");
		sb.append("  code="); 			sb.append(this.getCode());

		sb.append("\n Cabecera mensaje referenciado: ----");
		this.getReferencedNetlinkMessage().toString();
		sb.append("\n Fin cabecera mensaje referenciado: ----");

		return sb.toString();
	}

	@Override
	public NetlinkControlCode getCode() {
		return this.code;
	}

	@Override
	public MensajeNetlink getReferencedNetlinkMessage() {
		return this.referencedNetlinkMessage;
	}

	@Override
	public boolean hasCode() {
		return this.hasCode;
	}

	@Override
	public boolean hasReferencedNetlinkMessage() {
		return this.hasReferencedNetlinkMessage;
	}

}

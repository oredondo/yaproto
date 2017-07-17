/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.netlink.mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;

public class MensajeNetlinkRouteAttributeUnspec extends MensajeNetlinkRouteAttribute implements IMensaje, IMensajeNetlinkRouteAttributeUnspec {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	protected MensajeNetlinkRouteAttributeUnspec() {
		super();		
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
	 * Este objeto es un miembro de una uni�n impl�cita en la propia clase
	 * Por ello, siempre incluir� en la salida los campos selector de la uni�n (tipo/longitud). 
	 * 
	 */
	/*
	 *  <------- NLA_HDRLEN ------> <-- NLA_ALIGN(payload)-->
	 * +---------------------+- - -+- - - - - - - - - -+- - -+
	 * |        Header       | Pad |     Payload       | Pad |
	 * |   (struct nlattr)   | ing |                   | ing |
	 * +---------------------+- - -+- - - - - - - - - -+- - -+
	 *  <-------------- nlattr->nla_len -------------->
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		// Selectores de la uni�n (cabecera en Attribute)
		super.writeToOutputStream(output);

		// No hay Datos		
	}

	protected int getLongitudMensaje() {
		// Cabecera (sin relleno posterior)
		return super.getLongitudSerializado();
	}
	@Override
	public int getLongitudSerializado() {
		return getLongitudMensaje(); // no a�ade relleno tras atributo, el que ponga varios juntos debe alinearlos
	}

	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeNetlinkRouteAttribute.Builder implements IMensajeBuilder, IMensajeNetlinkRouteAttributeUnspec, IMensajeNetlinkRouteAttributeUnspec.Build {
		
		private Builder() {
			_builder = this;
			mensaje = new MensajeNetlinkRouteAttributeUnspec(); //inicializa el mensaje heredado con el tipo adecuado
		}

		/* Sobreescribe para cambiar el tipo devuelto y usarlo tipado donde se requiera */
		@Override
		protected MensajeNetlinkRouteAttributeUnspec getMensaje() {
			return (MensajeNetlinkRouteAttributeUnspec)this.mensaje;
		}

		
		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeNetlinkRouteAttributeUnspec build() {
			if (mensaje.estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			mensaje.estaConstruido = true;

			return getMensaje();
		}

		public static MensajeNetlinkRouteAttributeUnspec getDefaultInstanceforType() {
			// TODO: Petici�n de tabla de rutas puede ser el mensaje por defecto
			MensajeNetlinkRouteAttributeUnspec mensaje = new MensajeNetlinkRouteAttributeUnspec();
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
				// No debe dar excepci�n I/O sobre un array de octetos
			}			
			return this;
		}

		/**
		 * MezclaDesde los campos del elemento. 
		 * 
		 * Este elemento es un miembro de una uni�n (herencia de MensajeRIP, selector: tipo/version) impl�cita en la clase
		 * Por ello, si se desea incluir en la mezcla los campos selector de la uni�n (tipo/version) deber�a usar el
		 * m�todo mezclarDesde de la uni�n (en la clase base). 
		 */
		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			// Nada que escribir
			
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public Builder mezclarDesde(IMensaje mensajeOrigen) {
			// Copia desde la base los campos comunes
			if (mensaje instanceof IMensajeNetlinkRouteAttribute) {
				super.mezclarDesde(mensajeOrigen);
			}
			else if (mensaje instanceof IMensajeNetlinkRouteAttributeUnspec) {
				// Campos comunes ya copiados en el instanceof anterior

				// nada que copiar
			}
			else 
				throw new IllegalArgumentException("mezclarDesde(IMensaje): objeto recibido no es de clase MensajeNetlinkRouteAttribute");

			return this;
		}


		@Override
		public boolean estaCompleto() {
			return (super.estaCompleto());			
		}

		/* IMensajeNetlinkRouteAttributeUnspec */

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" MensajeNetlinkRouteAttributeUnspec");

		sb.append("\n Cabecera:");
		sb.append("  length="); 		sb.append(this.getLength());
		sb.append("; type="); 			sb.append(this.getAttributeType());
		sb.append("\n Datos: -  (Unspec no transporta datos).");
		
		return sb.toString();
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;

public class MensajeRIPRespuesta extends MensajeRIP implements IMensaje, IMensajeRIPRespuesta {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	public static final int MAXRIPRUTAS = 25;
	protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	private MensajeRIPRespuesta() {
		super();
		/* Impl�cito en esta clase, est�n estos dos campos de la cabecera. Los fuerza */
		tipo = Tipo.RIPRespuesta;
		hasTipo = true;
		version = 2;
		hasVersion = true;
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
	 * Este objeto es un miembro de una uni�n (herencia de MensajeRIP, selector: tipo/version) impl�cita en la propia clase
	 * Por ello, siempre incluir� en la salida los campos selector de la uni�n (tipo/version). 
	 * 
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		// Selectores de la unión
		super.writeToOutputStream(output);
		// Rutas
		if (hasRutas && (! ripRutas.isEmpty()))
			for (IMensajeRIPRuta ripRuta: ripRutas)
				((IMensaje)ripRuta).writeToOutputStream(output);		
	}

	@Override
	public int getLongitudSerializado() {
		/* TODO: Si no supiesemos, que no debemos, que ripruta es fijo en RIPv2, deber�a ser un bucle por el arraylist
		 * consultando su getLongitudSerializado() de cada uno, para realizar la suma
		 */
		return super.getLongitudSerializado() + 20 * ripRutas.size(); // 20 octetos por ruta + campos de la uni�n		 
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeRIP.Builder implements IMensajeBuilder, IMensajeRIPRespuesta, IMensajeRIPRespuesta.Build {
		
		private Builder() {
			_builder = this;
			mensaje = new MensajeRIPRespuesta();
		}

		protected MensajeRIPRespuesta getMensaje() {
			return (MensajeRIPRespuesta)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeRIPRespuesta build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeRIPRespuesta getDefaultInstanceforType() {
			MensajeRIPRespuesta mensaje = new MensajeRIPRespuesta();
			mensaje.estaConstruido = true;
			return mensaje;
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
			return this;
		}

		@Override
		public Builder mezclarDesde(InputStream inputStream) throws IOException {
			/* Requiere de una implementaci�n (bytearrayinputstream o similar) que proporcione available(), 
			 * pues el protocolo no incluye previamente un campo n�meroEntradas y el tama�o es variable
			 */
			// RIP no tiene campo longitud. Termina cuando no hay m�s datos
			// No deber�an venir m�s de 25 rutas por mensaje
			while ((inputStream.available() > 0) && ((!getMensaje().hasRutas) || getMensaje().ripRutas.size() <= 25)) {  //TODO:constante
					this.addRIPRuta(MensajeRIPRuta.Builder.crear()
											.mezclarDesde(inputStream)
											.build());
			}
			if (getMensaje().hasRutas) 
				System.out.println("MensajeRIPRespusta::mezclarDesde: recogidas " + getMensaje().ripRutas.size() + "rutas.");
			else
				System.out.println("MensajeRIPRespusta::mezclarDesde: 0 rutas");
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			// Copia desde la base los campos comunes
			if (mensaje instanceof IMensajeRIP)    //mejor instanceof que reflexion IMensajeRIP.class.isInstance(mensajeOrigen); // esta o sus subclases
				super.mezclarDesde(mensajeOrigen);
			else if (mensaje instanceof IMensajeRIPRespuesta) {
				// Copia desde la base los campos comunes
				super.mezclarDesde(mensajeOrigen);

				MensajeRIPRespuesta mensajeRIPOrigen = (MensajeRIPRespuesta)mensajeOrigen;
				if (getMensaje().hasRutas)
					this.setRIPRutas(mensajeRIPOrigen.getRIPRutas());
			}
			else
				throw new IllegalArgumentException("MensajeRIPRespuesta::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeRIP");


			return this;
		}


		@Override
		public boolean estaCompleto() {
			return (getMensaje().hasTipo && getMensaje().hasVersion); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public List<IMensajeRIPRuta> getRIPRutas() {
			// TODO Auto-generated method stub
			return getMensaje().ripRutas;
		}

		/**
		 * No realiza copia de los objetos IMensajeRipRuta.
		 * Si el mensaje no ten�a lista, establece como lista la indicada. Si ten�a lista, a�ade los elementos de 
		 * la lista indicada a la existente.
		 */
		@Override
		public Builder setRIPRutas(List<IMensajeRIPRuta> ripRutas) {
			if (getMensaje().ripRutas == null)
				getMensaje().ripRutas = ripRutas;
			else
				for (IMensajeRIPRuta mensajeRIPRuta: ripRutas)
					getMensaje().ripRutas.add(mensajeRIPRuta);
			getMensaje().hasRutas = true;
			return this;
		}


		@Override
		public Builder removeRIPRutas() {
			getMensaje().ripRutas = null;
			getMensaje().hasRutas = false;
			return this;
		}

		@Override
		public Builder addRIPRuta(IMensajeRIPRuta mensajeRIPRuta) {
			if (getMensaje().ripRutas == null)
				getMensaje().ripRutas = new ArrayList<IMensajeRIPRuta>(MAXRIPRUTAS);
			getMensaje().ripRutas.add(mensajeRIPRuta);
			getMensaje().hasRutas = true;
			return this;
		}

		@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}

	}

	@Override
	public List<IMensajeRIPRuta> getRIPRutas() {
		return this.ripRutas;
	}

	@Override
	public boolean hasRIPRutas() {
		return this.hasRutas;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<----- MensajeRespuestaRIP");

		if (this.ripRutas != null)
			for (IMensajeRIPRuta mensajeRIPRuta: ripRutas)
				sb.append(mensajeRIPRuta.toString());

		return sb.toString();
	}

}

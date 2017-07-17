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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;

public class MensajeRIPPeticion extends MensajeRIP implements IMensaje, IMensajeRIPPeticion {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	public static final int MAXRIPRUTAS = 25;
	protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	
	protected MensajeRIPPeticion() {
		super();
		/* Impl�cito en esta clase, est�n estos dos campos de la cabecera. Los fuerza TODO: Quitar */
		tipo = Tipo.RIPPeticion;
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
	 * Este objeto es un miembro de una uni�n (herencia de MensajeRIP, selector: tipo/version) impl�cita en la propia clase
	 * Por ello, siempre incluir� en la salida los campos selector de la uni�n (tipo/version). 
	 * 
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		// Selectores de la uni�n
		super.writeToOutputStream(output);
		// Campos espec�ficos: rutas
		// Si es una petición de tabla completa, realmente envía una única pseudoruta para indicarlo.
		if ((peticionTablaCompleta != null) && peticionTablaCompleta.booleanValue()) {
			/* Tabla completa: una única ruta, con familia direcciones 0 y métrica infinito */
			MensajeRIPRuta.Builder pseudoruta = MensajeRIPRuta.Builder.crear();
			pseudoruta.setIdFamiliaDirecciones((byte) 0);
			InetAddress direccionCero = InetAddress.getByName("0.0.0.0");
			pseudoruta.setDireccionIP(direccionCero);
			pseudoruta.setDireccionProximoSalto(direccionCero);
			pseudoruta.setEtiquetaRuta((short) 0);
			pseudoruta.setLongitudPrefijoRed(0);
			pseudoruta.setMetrica(16); // TODO: constante RIP_INFINITO
			((IMensaje) pseudoruta.build()).writeToOutputStream(output);		
		}
		// Si no es pet. tabla completa, envía las rutas reales
		else {
			for (IMensajeRIPRuta ripRuta: ripRutas)
				((IMensaje) ripRuta).writeToOutputStream(output);		
		}
	}

	@Override
	public int getLongitudSerializado() {
		/* TODO: Si no supiesemos, que no deber�a, que ripruta es fijo en RIPv2, deber�a ser un bucle por el arraylist
		 * consultando su getLongitudSerializado() de cada uno, para realizar la suma
		 */
		return super.getLongitudSerializado() + 20 * ripRutas.size(); // 20 octetos por ruta		 
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeRIP.Builder implements IMensajeBuilder, IMensajeRIPPeticion, IMensajeRIPPeticion.Build {
		
		private Builder() {
			// No invoca al super()
			mensaje = new MensajeRIPPeticion();
		}

		protected MensajeRIPPeticion getMensaje() {
			return (MensajeRIPPeticion)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeRIPPeticion build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeRIPPeticion getDefaultInstanceforType() {
			MensajeRIPPeticion mensaje = new MensajeRIPPeticion();
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
			/* Requiere de una implementaci�n (bytearrayinputstream o similar) que proporcione available(), 
			 * pues el protocolo no incluye previamente un campo n�meroEntradas y el tama�o es variable
			 */
			// RIP no tiene campo longitud. Termina cuando no hay m�s datos
			// No deber�an venir m�s de 25 rutas por mensaje
			while ((inputStream.available() > 0) && (!getMensaje().hasRutas || (getMensaje().ripRutas.size() <= 25))) { 
					this.addRIPRuta(MensajeRIPRuta.Builder.crear()
											.mezclarDesde(inputStream)
											.build());
			}
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
			else if (mensaje instanceof IMensajeRIPPeticion) {
				// Copia desde la base los campos comunes
				super.mezclarDesde(mensajeOrigen);

				IMensajeRIPPeticion _mensajeOrigen = (IMensajeRIPPeticion)mensajeOrigen;
				if (_mensajeOrigen.hasRIPRutas())
					this.setRIPRutas(_mensajeOrigen.getRIPRutas());
			}
			else
				throw new IllegalArgumentException("MensajeRIPPeticion::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeRIP");


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

		@Override
		public boolean esPeticionTablaCompleta() {
			return getMensaje().esPeticionTablaCompleta();
		}

		public Builder setPeticionTablaCompleta(boolean b) {
			getMensaje().peticionTablaCompleta = new Boolean(true);
			return this;
		}

	}

	/**
	 * TODO: De momento, hasta separar la interfaz IMensajeRIPRuta en dos IMensajeRIPRuta.Getters, 
	 * IMensajeRIPRuta.Setters (usando IMensajeRIPRuta.Builder como tipos pasados, 
	 * y devolviendo en todos los set el mismo objeto Builder para poder encadenar la 
	 * construcci�n en una l�nea de c�digo.
	 * 
	 *  Por ello de momento se repite el c�digo al estar el setter en ambas clases (mensaje y builder)
	 *  Repito el c�digo porque el bueno debe estar en el Builder, pero para no repetir deber�a invocar desde el 
	 *  Builder al set del Mensaje y poner el bueno en el getMensaje(). As� en un futuro s�lo hay que borrar el setter
	 *  del mensaje.
	 *
	 */
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
		sb.append("-----> MensajePetici�nRIP");

		if (this.ripRutas != null)
			for (IMensajeRIPRuta mensajeRIPRuta: ripRutas)
				sb.append(mensajeRIPRuta.toString());

		return sb.toString();
	}

	@Override
	public boolean esPeticionTablaCompleta() {
		if (peticionTablaCompleta == null) {
			boolean esTablaCompleta = false;
			if (this.hasRutas && (this.getRIPRutas().size() == 1)) {
				IMensajeRIPRuta ruta = this.getRIPRutas().get(0);
				if (ruta.hasIdFamiliaDirecciones() && (ruta.getIdFamiliaDirecciones() == 0) && (ruta.getMetrica() >= 16))
					esTablaCompleta = true;
			}
			peticionTablaCompleta = new Boolean(esTablaCompleta);
		}
		return peticionTablaCompleta.booleanValue();
	}
}

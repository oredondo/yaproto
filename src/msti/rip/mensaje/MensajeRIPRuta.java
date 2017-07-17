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
import java.net.InetAddress;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;

public class MensajeRIPRuta  implements IMensaje, IMensajeRIPRuta {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	protected short idFamiliaDirecciones;
	protected boolean hasIdFamiliaDirecciones = false;

	protected short etiquetaRuta;
	protected boolean hasEtiquetaRuta = false;
	
	protected InetAddress direccionIP;
	protected boolean hasDireccionIP = false;

	protected int longitudPrefijoRed;
	protected boolean hasLongitudPrefijoRed = false;

	protected InetAddress direccionProximoSalto;
	protected boolean hasDireccionProximoSalto = false;

	protected int metrica;
	protected boolean hasMetrica = false;

	private MensajeRIPRuta() {
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

	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		DataOutputStream dos = new DataOutputStream(output);

		dos.writeShort(this.idFamiliaDirecciones);
		dos.writeShort(this.etiquetaRuta);
		dos.write(this.direccionIP.getAddress());
		dos.writeInt((1<<31) >> (this.longitudPrefijoRed - 1)); //Desplaza 100...0 (signo) y luego >> arrastra el signo generando 11110...0
		dos.write(this.direccionProximoSalto.getAddress());
		dos.writeInt(this.metrica);
	}

	@Override
	public int getLongitudSerializado() {
		return 20;
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	 public static class Builder implements IMensajeBuilder, IMensajeRIPRuta, IMensajeRIPRuta.Build {
		
		MensajeRIPRuta mensaje;

		private Builder() {
			mensaje = new MensajeRIPRuta();
		}
		
		public static Builder crear() {
			return new Builder();
		}

		
		@Override
		public MensajeRIPRuta build() {
			if (mensaje.estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			mensaje.estaConstruido = true;

			return mensaje;
		}

		
		public static MensajeRIPRespuesta getDefaultInstanceforType() {
			// TODO: �Dar la ruta por defecto en este caso ? :)
			throw new UnsupportedOperationException("MensajeRIPRuta::getDefaultInstanceforType(): no est� implementado mensaje de ruta relleno por defecto");
/*			MensajeRIPRuta mensaje = new MensajeRIPRuta();
			mensaje.estaConstruido = true;
			return mensaje;
*/		}
		
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
			byte[] bufer = new byte[4];
			DataInputStream dis = new DataInputStream(inputStream);
			/* Id familia direcciones */
			this.setIdFamiliaDirecciones(dis.readShort());
			/* Etiqueta ruta */
			this.setEtiquetaRuta(dis.readShort());
			/* Direcci�n IP */
			dis.read(bufer);
			this.setDireccionIP(InetAddress.getByAddress(bufer));
			/* M�scara de red */
			int longitudPrefijo = 0;
			for (int mascara = dis.readInt(); mascara != 0; mascara = mascara << 1)  //desplaza 1 a izqda, hasta valor 0
				longitudPrefijo++;
			this.setLongitudPrefijoRed(longitudPrefijo);
			/* Direcci�n pr�ximo salto */
			dis.read(bufer);
			this.setDireccionProximoSalto(InetAddress.getByAddress(bufer));
			/* M�trica*/
			this.setMetrica(dis.readInt());
			
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			if (mensajeOrigen instanceof IMensajeRIPRuta) {
			IMensajeRIPRuta _mensajeOrigen = (IMensajeRIPRuta)mensajeOrigen;

			if (_mensajeOrigen.hasIdFamiliaDirecciones())
				this.setIdFamiliaDirecciones(_mensajeOrigen.getIdFamiliaDirecciones());

			if (_mensajeOrigen.hasEtiquetaRuta())
				this.setEtiquetaRuta(_mensajeOrigen.getEtiquetaRuta());

			if (_mensajeOrigen.hasDireccionIP())
				this.setDireccionIP(_mensajeOrigen.getDireccionIP());

			if (_mensajeOrigen.hasLongitudPrefijoRed())
				this.setLongitudPrefijoRed(_mensajeOrigen.getLongitudPrefijoRed());

			if (_mensajeOrigen.hasDireccionProximoSalto())
				this.setDireccionProximoSalto(_mensajeOrigen.getDireccionProximoSalto());

			if (_mensajeOrigen.hasMetrica())
				this.setMetrica(_mensajeOrigen.getMetrica());
			}
			else
				throw new IllegalArgumentException("MensajeRIPRuta::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeRIPRespuesta");
			
			return this;
		}


		@Override
		public boolean estaCompleto() {
			return (mensaje.hasIdFamiliaDirecciones &&
					mensaje.hasEtiquetaRuta &&
					mensaje.hasDireccionIP &&
					mensaje.hasLongitudPrefijoRed &&
					mensaje.hasDireccionProximoSalto && 
					mensaje.hasMetrica);  
		}

		/* IMensajeRIPRuta */

		@Override
		public short getIdFamiliaDirecciones() {
			return mensaje.idFamiliaDirecciones;			
		}

		@Override
		public Builder setIdFamiliaDirecciones(short idFamiliaDirecciones) {
			mensaje.idFamiliaDirecciones = idFamiliaDirecciones;
			mensaje.hasIdFamiliaDirecciones = true;
			return this;
		}

		@Override
		public short getEtiquetaRuta() {
			return mensaje.etiquetaRuta;
		}

		@Override
		public Builder setEtiquetaRuta(short etiquetaRuta) {
			mensaje.etiquetaRuta = etiquetaRuta;
			mensaje.hasEtiquetaRuta = true;
			return this;
		}

		@Override
		public InetAddress getDireccionIP() {
			return mensaje.direccionIP;
		}

		@Override
		public Builder setDireccionIP(InetAddress direccionIP) {
			mensaje.direccionIP = direccionIP;
			mensaje.hasDireccionIP = (direccionIP != null); 
			return this;
		}

		@Override
		public int getLongitudPrefijoRed() {
			return mensaje.longitudPrefijoRed;
		}

		@Override
		public Builder setLongitudPrefijoRed(int longitudPrefijoRed) {
			mensaje.longitudPrefijoRed = longitudPrefijoRed;
			mensaje.hasLongitudPrefijoRed = true;
			return this;
		}

		@Override
		public InetAddress getDireccionProximoSalto() {
			return mensaje.direccionProximoSalto;
		}

		@Override
		public Builder setDireccionProximoSalto(InetAddress direccionProximoSalto) {
			mensaje.direccionProximoSalto = direccionProximoSalto;
			mensaje.hasDireccionProximoSalto = (direccionProximoSalto != null);
			return this;
		}

		@Override
		public int getMetrica() {
			return mensaje.metrica;
		}

		@Override
		public Builder setMetrica(int metrica) {
			mensaje.metrica = metrica;
			mensaje.hasMetrica = true;
			return this;
		}

		@Override
		public boolean hasIdFamiliaDirecciones() {
			return mensaje.hasIdFamiliaDirecciones;
		}

		@Override
		public boolean hasEtiquetaRuta() {
			return mensaje.hasEtiquetaRuta;
		}

		@Override
		public boolean hasDireccionIP() {
			return mensaje.hasDireccionIP;
		}

		@Override
		public boolean hasLongitudPrefijoRed() {
			return mensaje.hasLongitudPrefijoRed;
		}

		@Override
		public boolean hasDireccionProximoSalto() {
			return mensaje.hasDireccionProximoSalto;
		}

		@Override
		public boolean hasMetrica() {
			return mensaje.hasMetrica;
		}

	}

	/* IMensajeRIPRuta */

	@Override
	public short getIdFamiliaDirecciones() {
		return this.idFamiliaDirecciones;
	}

	@Override
	public short getEtiquetaRuta() {
		return this.etiquetaRuta;
	}

	@Override
	public InetAddress getDireccionIP() {
		return this.direccionIP;
	}

	@Override
	public int getLongitudPrefijoRed() {
		return this.longitudPrefijoRed;
	}

	@Override
	public InetAddress getDireccionProximoSalto() {
		return this.direccionProximoSalto;
	}

	@Override
	public int getMetrica() {
		// TODO Auto-generated method stub
		return this.metrica;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Ruta RIP ----------");

		sb.append("AFI="); 		sb.append(this.getIdFamiliaDirecciones());
		sb.append("Etiq=");		sb.append(this.getEtiquetaRuta());
		sb.append("IP=");		sb.append(this.getDireccionIP().toString());
		sb.append("Mask=\\");	sb.append(this.getLongitudPrefijoRed());
		sb.append("Next=");		sb.append(this.getDireccionProximoSalto().toString());
		sb.append("Metr=");		sb.append(this.getMetrica());
		return sb.toString();
	}

	@Override
	public boolean hasIdFamiliaDirecciones() {
		return this.hasIdFamiliaDirecciones;
	}

	@Override
	public boolean hasEtiquetaRuta() {
		return this.hasEtiquetaRuta;
	}

	@Override
	public boolean hasDireccionIP() {
		return this.hasDireccionIP;
	}

	@Override
	public boolean hasLongitudPrefijoRed() {
		return this.hasLongitudPrefijoRed;
	}

	@Override
	public boolean hasDireccionProximoSalto() {
		return this.hasDireccionProximoSalto;
	}

	@Override
	public boolean hasMetrica() {
		return this.hasMetrica;
	}
}

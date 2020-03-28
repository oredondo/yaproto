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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSARouterLinksLinks.Type;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateRequest.Builder;

public class MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields extends MensajeOSPFv2LSA implements IMensaje, IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	
	protected boolean hasETOS=false;
	protected boolean hasMetric=false;
	protected boolean hasForwardingAddress=false;
	protected boolean hasExternalRouteTag=false;

	protected byte etos;
	protected short metric;
	protected int forwardingAddress;
	protected int externalRouteTag;


	
	private MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields() {
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
		DataOutputStream dos = new DataOutputStream(output);
		//escribe etos
		if (hasETOS)
			dos.writeByte(etos);
		//byte a 0
		dos.writeByte(0);
		//escribe metric
		if (hasMetric)
			dos.writeShort(metric);
		//escribe forwardingAddress
		if (hasForwardingAddress)
			dos.writeInt(forwardingAddress);
		//escribe externalRouteTag
		if (hasExternalRouteTag)
			dos.writeInt(externalRouteTag);
		
	
		/*// Selectores de la uni�n
		super.writeToOutputStream(output);
		// Campos espec�ficos: rutas
		// Si es una petición de tabla completa, realmente envía una única pseudoruta para indicarlo.
		if ((peticionTablaCompleta != null) && peticionTablaCompleta.booleanValue()) {
			// Tabla completa: una única ruta, con familia direcciones 0 y métrica infinito 
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
		}*/
	}

	@Override
	public int getLongitudSerializado() {
		//12
		return 12; 		 
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2LSA.Builder implements IMensajeBuilder, IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields, IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields.Build {
		
		private Builder() {
			// No invoca al super()
			mensaje = new MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields();
		}

		protected MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields getMensaje() {
			return (MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields getDefaultInstanceforType() {
			MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields mensaje = new MensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields();
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
			/*// RIP no tiene campo longitud. Termina cuando no hay m�s datos
			// No deber�an venir m�s de 25 rutas por mensaje
			while ((inputStream.available() > 0) && (!getMensaje().hasRutas || (getMensaje().ripRutas.size() <= 25))) { 
					this.addRIPRuta(MensajeRIPRuta.Builder.crear()
											.mezclarDesde(inputStream)
											.build());
			}
			return this;*/
			
			
			DataInputStream dis = new DataInputStream(inputStream);
			//etos
			this.setETOS(dis.readByte());
			//byte a 0
			dis.readByte();
			//metric
			this.setMetric(dis.readShort());
			//forwardingAddress
			this.setForwardingAddress(dis.readInt());
			//externalRouteTag
			this.setExternalRouteTag(dis.readInt());
			

					
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {			
			if (mensajeOrigen instanceof IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields){
				IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields _mensajeOrigen = (IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields) mensajeOrigen;

				if (_mensajeOrigen.hasETOS())
					this.setETOS(_mensajeOrigen.getETOS());

				if (_mensajeOrigen.hasMetric())
					this.setMetric(_mensajeOrigen.getMetric());

				if (_mensajeOrigen.hasForwardingAddress())
					this.setForwardingAddress(_mensajeOrigen.getForwardingAddress());

				if (_mensajeOrigen.hasExternalRouteTag())
					this.setExternalRouteTag(_mensajeOrigen.getExternalRouteTag());

					
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LSAASExternalLinkAdvertisementsTOSFields");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasETOS) && (getMensaje().hasMetric) && (getMensaje().hasForwardingAddress)
					&& (getMensaje().hasExternalRouteTag)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public byte getETOS() {
			// TODO Auto-generated method stub
			return getMensaje().etos;
		}
		public short getMetric() {
			// TODO Auto-generated method stub
			return getMensaje().metric;
		}

		public int getForwardingAddress() {
			// TODO Auto-generated method stub
			return getMensaje().forwardingAddress;
		}
		public int getExternalRouteTag() {
			// TODO Auto-generated method stub
			return getMensaje().externalRouteTag;
		}


		/**
		 * No realiza copia de los objetos IMensajeRipRuta.
		 * Si el mensaje no ten�a lista, establece como lista la indicada. Si ten�a lista, a�ade los elementos de 
		 * la lista indicada a la existente.
		 */
		@Override
		/*public Builder setRIPRutas(List<IMensajeRIPRuta> ripRutas) {
			if (getMensaje().ripRutas == null)
				getMensaje().ripRutas = ripRutas;
			else
				for (IMensajeRIPRuta mensajeRIPRuta: ripRutas)
					getMensaje().ripRutas.add(mensajeRIPRuta);
			getMensaje().hasRutas = true;
			return this;
		}*/
		

		
		public Builder setETOS(byte etos) {
			getMensaje().etos =etos;
			getMensaje().hasETOS = true;
			return this;
		}
		
		public Builder setMetric(short metric) {
			getMensaje().metric =metric;
			getMensaje().hasMetric = true;
			return this;
		}
		public Builder setForwardingAddress(int forwardingAddress) {
			getMensaje().forwardingAddress =forwardingAddress;
			getMensaje().hasForwardingAddress = true;
			return this;
		}
		
		public Builder setExternalRouteTag(int externalRouteTag) {
			getMensaje().externalRouteTag =externalRouteTag;
			getMensaje().hasExternalRouteTag = true;
			return this;
		}
		

		/*@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}*/
		
		public boolean hasETOS() {
			return getMensaje().hasETOS;
		}
		public boolean hasMetric() {
			return getMensaje().hasMetric;
		}
		public boolean hasForwardingAddress() {
			return getMensaje().hasForwardingAddress;
		}
		public boolean hasExternalRouteTag() {
			return getMensaje().hasExternalRouteTag;
		}

		
		//

		/*@Override
		public boolean esPeticionTablaCompleta() {
			return getMensaje().esPeticionTablaCompleta();
		}

		public Builder setPeticionTablaCompleta(boolean b) {
			getMensaje().peticionTablaCompleta = new Boolean(true);
			return this;
		}*/

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
	
	public byte getETOS() {
		// TODO Auto-generated method stub
		return this.etos;
	}

	public short getMetric() {
		// TODO Auto-generated method stub
		return this.metric;
	}
	public int getForwardingAddress() {
		// TODO Auto-generated method stub
		return this.forwardingAddress;
	}
	public int getExternalRouteTag() {
		// TODO Auto-generated method stub
		return this.externalRouteTag;
	}
	
	
	
	public boolean hasETOS() {
		return this.hasETOS;
	}
	public boolean hasMetric() {
		return this.hasMetric;
	}
	public boolean hasForwardingAddress() {
		return this.hasForwardingAddress;
	}
	public boolean hasExternalRouteTag() {
		return this.hasExternalRouteTag;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//sb.append("-----> Mensaje Hello OSPFv2");

		if (hasETOS)
			sb.append(",E-TOS " + Byte.toString(etos));
		
		if (hasMetric)
			sb.append(",Metric " + Short.toString(metric));
		
		if (hasForwardingAddress)
			sb.append(",Forwarding Address " + Integer.toString(forwardingAddress));
		
		if (hasExternalRouteTag)
			sb.append(",External Route Tag " + Integer.toString(externalRouteTag));
		
			
		return sb.toString();
	}


}
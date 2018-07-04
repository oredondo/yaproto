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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.ChecksumOSPFv2;
import msti.ospfv2.mensaje.MensajeOSPFv2LSARouterLinksLinks.Builder;


public class MensajeOSPFv2LSASumaryLinksAdvertisements extends MensajeOSPFv2LSA implements IMensaje, IMensajeOSPFv2LSASumaryLinksAdvertisements {
	/* si construyï¿½ y entregï¿½ el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	protected boolean hasNetworkMask=false;
	protected boolean hasTOSs=false;
	protected boolean hasMetrics=false;
	
	protected int networkMask;
	protected List<Byte> toss;
	protected List<Short> metrics;
	
	protected MensajeOSPFv2LSASumaryLinksAdvertisements() {
		super();
	}

	/* IMensaje (serializaciï¿½n, construcciï¿½n) */
	
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
	 * Este objeto es un miembro de una uniï¿½n (herencia de MensajeRIP, selector: tipo/version) implï¿½cita en la propia clase
	 * Por ello, siempre incluirï¿½ en la salida los campos selector de la uniï¿½n (tipo/version). 
	 * 
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		//escribe la cabecera
		super.writeToOutputStream(output);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		//escribe networkMask
		if (hasNetworkMask)
			dos.writeInt(networkMask);
		//escribe routerLinks
		
		if(hasTOSs && hasMetrics && toss.size()==metrics.size()){
			Iterator<Byte> iterTOSs = toss.listIterator();
			Iterator<Short> iterMertrics = metrics.listIterator();	
		    while (iterTOSs.hasNext()) {
		    	dos.writeByte(iterTOSs.next());
		    	dos.writeByte(0);
		    	dos.writeShort(iterMertrics.next());
		    }
		}
		//colocar lo del baos detrás del mensajeSerializado
		ByteArrayOutputStream baosMensajeCompleto = new ByteArrayOutputStream( );
		baosMensajeCompleto.write(mensajeSerializadoLSA);
		baosMensajeCompleto.write(baos.toByteArray());
		
		mensajeSerializadoLSA= baosMensajeCompleto.toByteArray();
		
		//antes de calcular checksum, calcular y rellenar PacketLengh
		short packetLengthLSASummary = (short) getLongitudSerializado();
		mensajeSerializadoLSA[18] =  (byte)((packetLengthLSASummary >> 8) & 0xff);
		mensajeSerializadoLSA[19] = (byte)(packetLengthLSASummary & 0xff);
		//header.packetLength = packetLengthLSASummary;
		
		//calcular checksum
		//checksum está a 0, calcularlo, rellenarlo (en el array mensaje Serializado)
		short checksumLSASummary = (short) ChecksumOSPFv2.calcularChecksumLSA(mensajeSerializadoLSA);
		mensajeSerializadoLSA[16] =  (byte)((checksumLSASummary >> 8) & 0xff);
		mensajeSerializadoLSA[17] = (byte)(checksumLSASummary & 0xff);
		//header.lSChecksum=checksumLSASummary;
		
		output.write(mensajeSerializadoLSA);
	
	}

	@Override
	public int getLongitudSerializado() {
		//cabecera (super) + 4 octetos y 4 más por cada attachedRouters
		
		return super.getLongitudSerializado() + 4 + 4*toss.size();
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2LSA.Builder implements IMensajeBuilder, IMensajeOSPFv2LSASumaryLinksAdvertisements, IMensajeOSPFv2LSASumaryLinksAdvertisements.Build {
		
		private Builder() {
			//super();
			mensaje = new MensajeOSPFv2LSASumaryLinksAdvertisements();
		}

		protected MensajeOSPFv2LSASumaryLinksAdvertisements getMensaje() {
			return (MensajeOSPFv2LSASumaryLinksAdvertisements)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2LSASumaryLinksAdvertisements build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o mï¿½s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2LSASumaryLinksAdvertisements getDefaultInstanceforType() {
			MensajeOSPFv2LSASumaryLinksAdvertisements mensaje = new MensajeOSPFv2LSASumaryLinksAdvertisements();
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
				// No debe dar excepciï¿½n I/O sobre un array de octetos
			}			
			return this;
		}

		/**
		 * MezclaDesde los campos del elemento. 
		 * 
		 * Este elemento es un miembro de una uniï¿½n (herencia de MensajeRIP, selector: tipo/version) implï¿½cita en la clase
		 * Por ello, si se desea incluir en la mezcla los campos selector de la uniï¿½n (tipo/version) deberï¿½a usar el
		 * mï¿½todo mezclarDesde de la uniï¿½n (en la clase base). 
		 */
		@Override
		public Builder mezclarDesde(InputStream inputStream, byte[] mensajeSerializadoLSA) throws IOException {
		
			DataInputStream dis = new DataInputStream(inputStream);
			dis.read(mensajeSerializadoLSA, 20, inputStream.available()); 
			
			//Comprobar checksum del mensaje con la funcion verificar
			//si está bien, lees, sino checksumOK false
			if(ChecksumOSPFv2.verificarChecksumLSA(mensajeSerializadoLSA)){
				ByteArrayInputStream bais = new ByteArrayInputStream(mensajeSerializadoLSA);
				dis = new DataInputStream(bais);
				//cabecera
				byte[] cabecera = new byte[20];
				dis.read(cabecera, 0, 20);
				
				//networkMask
				this.setNetworkMask(dis.readInt());
				//toos+metrics
				byte[] bufer = new byte[4];
				while(dis.read(bufer,0,4) > 0){
					addTOS(bufer[0]);
					
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(bufer[2]);
					bb.put(bufer[3]);
					addMetric(bb.getShort(0));
				}
				setIsLSChecksumOK(true);
				
			}else{
				setIsLSChecksumOK(false);
			}
					
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			if (mensaje instanceof IMensajeOSPFv2LSA)   
				super.mezclarDesde(mensajeOrigen);
			else if (mensaje instanceof IMensajeOSPFv2LSASumaryLinksAdvertisements){
				IMensajeOSPFv2LSASumaryLinksAdvertisements _mensajeOrigen = (IMensajeOSPFv2LSASumaryLinksAdvertisements) mensajeOrigen;

				if (_mensajeOrigen.hasNetworkMask())
					this.setNetworkMask(_mensajeOrigen.getNetworkMask());

				if (_mensajeOrigen.hasTOSs())
					this.setTOSs(_mensajeOrigen.getTOSs());
				
				if (_mensajeOrigen.hasMetrics())
					this.setMetrics(_mensajeOrigen.getMetrics());
				
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2LSASumaryLinksAdvertisements::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LSASumaryLinksAdvertisements");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasNetworkMask) && (getMensaje().hasTOSs) && (getMensaje().hasMetrics)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public int getNetworkMask() {
			// TODO Auto-generated method stub
			return getMensaje().networkMask;
		}
		
		public List<Byte> getTOSs() {
			// TODO Auto-generated method stub
			return getMensaje().toss;
		}
		public List<Short> getMetrics() {
			// TODO Auto-generated method stub
			return getMensaje().metrics;
		}

		/**
		 * No realiza copia de los objetos IMensajeRipRuta.
		 * Si el mensaje no tenï¿½a lista, establece como lista la indicada. Si tenï¿½a lista, aï¿½ade los elementos de 
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
		
		public Builder setNetworkMask(int networkMask) {
			getMensaje().networkMask =networkMask;
			getMensaje().hasNetworkMask = true;
			return this;
		}
		
		
		public Builder setTOSs(List<Byte> toss) {
			getMensaje().toss =toss;
			getMensaje().hasTOSs = true;
			return this;
		}

		public Builder setMetrics(List<Short> metrics) {
			getMensaje().metrics =metrics;
			getMensaje().hasMetrics = true;
			return this;
		}
		
		public Builder addTOS(Byte tos) {
			if (getMensaje().toss == null)
				getMensaje().toss = new ArrayList<Byte>();
			getMensaje().toss.add(tos);
			getMensaje().hasTOSs = true;
			return this;
		}
		
		public Builder addMetric(Short metric) {
			if (getMensaje().metrics == null)
				getMensaje().metrics = new ArrayList<Short>();
			getMensaje().metrics.add(metric);
			getMensaje().hasMetrics = true;
			return this;
		}
		
		public Builder removeTOSs() {
			getMensaje().toss = null;
			getMensaje().hasTOSs = false;
			return this;		
		}
		
		public Builder removeMetrics() {
			getMensaje().metrics = null;
			getMensaje().hasMetrics = false;
			return this;		
		}

		/*@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}*/
		
		public boolean hasNetworkMask() {
			return getMensaje().hasNetworkMask;
		}
		public boolean hasTOSs() {
			return getMensaje().hasTOSs;
		}
		public boolean hasMetrics() {
			return getMensaje().hasMetrics;
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
	 * construcciï¿½n en una lï¿½nea de cï¿½digo.
	 * 
	 *  Por ello de momento se repite el cï¿½digo al estar el setter en ambas clases (mensaje y builder)
	 *  Repito el cï¿½digo porque el bueno debe estar en el Builder, pero para no repetir deberï¿½a invocar desde el 
	 *  Builder al set del Mensaje y poner el bueno en el getMensaje(). Asï¿½ en un futuro sï¿½lo hay que borrar el setter
	 *  del mensaje.
	 *
	 */
	
	public int getNetworkMask() {
		// TODO Auto-generated method stub
		return this.networkMask;
	}
	public List<Byte> getTOSs() {
		// TODO Auto-generated method stub
		return this.toss;
	}
	public List<Short> getMetrics() {
		// TODO Auto-generated method stub
		return this.metrics;
	}
	
	public boolean hasNetworkMask() {
		return this.hasNetworkMask;
	}
	public boolean hasTOSs() {
		return this.hasTOSs;
	}
	public boolean hasMetrics() {
		return this.hasMetrics;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--> LSA Sumary Links: ");

		if(hasHeader)
			sb.append(header.toString());
		
		if (hasNetworkMask)
			sb.append(",NetworkMask " + Integer.toString(networkMask));
		
		if (hasTOSs && hasMetrics){
			sb.append(",TOS-metrics ");
			Iterator<Byte> iterTOSs = toss.listIterator();
			Iterator<Short> iterMetrics = metrics.listIterator();		
		    while (iterTOSs.hasNext()) {
		    	sb.append(String.valueOf(iterTOSs.next()) +" ");
		    	sb.append(Byte.toString((byte) 0) + " ");
		    	sb.append(String.valueOf(iterMetrics.next()) + " ");
		    }
		}

		return sb.toString();
	}

	/*@Override
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
	}*/
}
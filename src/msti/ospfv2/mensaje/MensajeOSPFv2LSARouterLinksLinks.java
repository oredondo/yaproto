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

public class MensajeOSPFv2LSARouterLinksLinks extends MensajeOSPFv2LSA implements IMensaje, IMensajeOSPFv2LSARouterLinksLinks {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	
	protected boolean hasLinkID=false;
	protected boolean hasLinkData=false;
	protected boolean hasType=false;
	protected boolean hasNTOS=false;
	protected boolean hasTOS0Metric=false;
	protected boolean hasTOSs=false;
	protected boolean hasMetrics=false;

	protected int linkID;
	protected int linkData;
	protected Type type;
	protected byte ntos;
	protected short tos0Metric;
	protected List<Byte> toss;
	protected List<Short> metrics;

	
	private MensajeOSPFv2LSARouterLinksLinks() {
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
		//escribe linkID
		if (hasLinkID)
			dos.writeInt(linkID);
		//escribe linkData
		if (hasLinkData)
			dos.writeInt(linkData);
		//escribe Type
		if (hasType)
			dos.writeByte(type.getCodigo());
		//escribe ntos
		if (hasNTOS)
			dos.writeByte(ntos);
		//escribe tos0Metric
		if (hasTOS0Metric)
			dos.writeShort(tos0Metric);
		//escribe toss+0+metris
		
		if(hasTOSs && hasMetrics && ((byte)toss.size())==ntos && ((byte)metrics.size())==ntos){
			Iterator<Byte> iterTOSs = toss.listIterator();
			Iterator<Short> iterMertrics = metrics.listIterator();	
		    while (iterTOSs.hasNext()) {
		    	dos.writeByte(iterTOSs.next());
		    	dos.writeByte(0);
		    	dos.writeShort(iterMertrics.next());
		    }

		}else{
			throw new IllegalStateException("Mensaje Link mal construido.");
		}
		
	}

	@Override
	public int getLongitudSerializado() {
		//12+NTOS*4
		//return 12+4*Byte.toUnsignedInt(ntos);
		Byte ntosByte = new Byte(ntos);
		return 12+4*ntosByte.intValue();
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2LSA.Builder implements IMensajeBuilder, IMensajeOSPFv2LSARouterLinksLinks, IMensajeOSPFv2LSARouterLinksLinks.Build {
		
		private Builder() {
			// No invoca al super()
			mensaje = new MensajeOSPFv2LSARouterLinksLinks();
		}

		protected MensajeOSPFv2LSARouterLinksLinks getMensaje() {
			return (MensajeOSPFv2LSARouterLinksLinks)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2LSARouterLinksLinks build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2LSARouterLinksLinks getDefaultInstanceforType() {
			MensajeOSPFv2LSARouterLinksLinks mensaje = new MensajeOSPFv2LSARouterLinksLinks();
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
			
			DataInputStream dis = new DataInputStream(inputStream);
			
			//linkID
			this.setLinkID(dis.readInt());
			//linkData
			this.setLinkData(dis.readInt());
			//type
			this.setType(MensajeOSPFv2LSARouterLinksLinks.Type.getByValue(dis.readByte()));
			//ntos
			Byte ntos=dis.readByte();
			this.setNTOS(ntos);
			//tos0Metric
			this.setTOS0Metric(dis.readShort());
			
			int ntosInt = new Integer(ntos);
			if(ntosInt==0){
				List<Byte> toss = new ArrayList<Byte>();
				List<Short> metrics = new ArrayList<Short>();
				this.setTOSs(toss);
				this.setMetrics(metrics);
			}
			
			for(int i=0;i<ntosInt;i++){
				this.addTOS(dis.readByte());
				dis.readByte();
				this.addMetric(dis.readShort());
			}

					
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {			
			if (mensajeOrigen instanceof IMensajeOSPFv2LSARouterLinksLinks){
				IMensajeOSPFv2LSARouterLinksLinks _mensajeOrigen = (IMensajeOSPFv2LSARouterLinksLinks) mensajeOrigen;

				if (_mensajeOrigen.hasLinkID())
					this.setLinkID(_mensajeOrigen.getLinkID());

				if (_mensajeOrigen.hasLinkData())
					this.setLinkData(_mensajeOrigen.getLinkData());

				if (_mensajeOrigen.hasType())
					this.setType(_mensajeOrigen.getType());

				if (_mensajeOrigen.hasNTOS())
					this.setNTOS(_mensajeOrigen.getNTOS());

				if (_mensajeOrigen.hasTOS0Metric())
					this.setTOS0Metric(_mensajeOrigen.getTOS0Metric());
				
				if (_mensajeOrigen.hasTOSs())
					this.setTOSs(_mensajeOrigen.getTOSs());
				
				if (_mensajeOrigen.hasMetrics())
					this.setMetrics(_mensajeOrigen.getMetrics());
					
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2LSARouterLinksLinks::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2LSARouterLinksLinks");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasLinkID) && (getMensaje().hasLinkData) && (getMensaje().hasType) && (getMensaje().hasNTOS) &&
					(getMensaje().hasTOS0Metric) && (getMensaje().hasTOSs) && (getMensaje().hasMetrics)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public int getLinkID() {
			// TODO Auto-generated method stub
			return getMensaje().linkID;
		}
		public int getLinkData() {
			// TODO Auto-generated method stub
			return getMensaje().linkData;
		}
		public Type getType() {
			// TODO Auto-generated method stub
			return getMensaje().type;
		}
		public byte getNTOS() {
			// TODO Auto-generated method stub
			return getMensaje().ntos;
		}
		public short getTOS0Metric() {
			// TODO Auto-generated method stub
			return getMensaje().tos0Metric;
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
		

		
		public Builder setLinkID(int linkID) {
			getMensaje().linkID =linkID;
			getMensaje().hasLinkID = true;
			return this;
		}
		
		public Builder setLinkData(int linkData) {
			getMensaje().linkData =linkData;
			getMensaje().hasLinkData = true;
			return this;
		}
		public Builder setType(Type type) {
			getMensaje().type =type;
			getMensaje().hasType = true;
			return this;
		}
		
		public Builder setNTOS(byte ntos) {
			getMensaje().ntos =ntos;
			getMensaje().hasNTOS = true;
			return this;
		}
		
		public Builder setTOS0Metric(short tos0Metric) {
			getMensaje().tos0Metric =tos0Metric;
			getMensaje().hasTOS0Metric = true;
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
		
		public boolean hasLinkID() {
			return getMensaje().hasLinkID;
		}
		public boolean hasLinkData() {
			return getMensaje().hasLinkData;
		}
		public boolean hasType() {
			return getMensaje().hasType;
		}
		public boolean hasNTOS() {
			return getMensaje().hasNTOS;
		}
		public boolean hasTOS0Metric() {
			return getMensaje().hasTOS0Metric;
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
	 * construcci�n en una l�nea de c�digo.
	 * 
	 *  Por ello de momento se repite el c�digo al estar el setter en ambas clases (mensaje y builder)
	 *  Repito el c�digo porque el bueno debe estar en el Builder, pero para no repetir deber�a invocar desde el 
	 *  Builder al set del Mensaje y poner el bueno en el getMensaje(). As� en un futuro s�lo hay que borrar el setter
	 *  del mensaje.
	 *
	 */
	
	public int getLinkID() {
		// TODO Auto-generated method stub
		return this.linkID;
	}

	public int getLinkData() {
		// TODO Auto-generated method stub
		return this.linkData;
	}
	public Type getType() {
		// TODO Auto-generated method stub
		return this.type;
	}
	public byte getNTOS() {
		// TODO Auto-generated method stub
		return this.ntos;
	}
	public short getTOS0Metric() {
		// TODO Auto-generated method stub
		return this.tos0Metric;
	}
	public List<Byte> getTOSs() {
		// TODO Auto-generated method stub
		return this.toss;
	}
	public List<Short> getMetrics() {
		// TODO Auto-generated method stub
		return this.metrics;
	}
	
	
	public boolean hasLinkID() {
		return this.hasLinkID;
	}
	public boolean hasLinkData() {
		return this.hasLinkData;
	}
	public boolean hasType() {
		return this.hasType;
	}
	public boolean hasNTOS() {
		return this.hasNTOS;
	}
	public boolean hasTOS0Metric() {
		return this.hasTOS0Metric;
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
		//sb.append("-----> Mensaje Hello OSPFv2");

		if (hasLinkID)
			sb.append(",LinkID " + Integer.toString(linkID));
		
		if (hasLinkData)
			sb.append(",LinkData " + Integer.toString(linkData));
		
		if (hasType)
			sb.append(",Type " + Byte.toString(type.getCodigo()));
		
		if (hasNTOS)
			sb.append(",NTOS " + Byte.toString(ntos));
		
		if (hasTOS0Metric)
			sb.append(",TOS0Metric " + Short.toString(tos0Metric));
				
		if (hasTOSs && hasMetrics){
			sb.append(",TOS-metrics ");
			Iterator<Byte> iterTOSs = toss.listIterator();
			Iterator<Short> iterMetrics = metrics.listIterator();		
		    while (iterTOSs.hasNext()) {
		    	sb.append(String.valueOf(iterTOSs.next()) + " ");
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
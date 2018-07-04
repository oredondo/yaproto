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
import java.util.Arrays;
import java.util.List;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.ospfv2.ChecksumOSPFv2;
import msti.rip.mensaje.IMensajeRIP;


public class MensajeOSPFv2Hello extends MensajeOSPFv2 implements IMensaje, IMensajeOSPFv2Hello {
	/* si construyï¿½ y entregï¿½ el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;

	/*public static final int MAXRIPRUTAS = 25;
	//protected List<IMensajeRIPRuta> ripRutas;
	protected boolean hasRutas = false;

	protected Boolean peticionTablaCompleta = null; 
	*/
	protected boolean hasNetworkMask=false;
	protected boolean hasHelloInterval=false;
	protected boolean hasOptions=false;
	protected boolean hasRtrPri=false;
	protected boolean hasRouterDeadInterval=false;
	protected boolean hasDesignatedRouter=false;
	protected boolean hasBackupDesignatedRouter=false;
	protected boolean hasNeighbors=false;
	
	protected int networkMask;
	protected short helloInterval;
	protected byte options;
	protected byte rtrPri;
	protected int routerDeadInterval;
	protected int designatedRouter;
	protected int backupDesignatedRouter;
	protected List<Integer> neighbors;
	
	protected MensajeOSPFv2Hello() {
		super();
		/* Implï¿½cito en esta clase, estï¿½n estos dos campos de la cabecera. Los fuerza TODO: Quitar */
		tipo = Tipo.OSPFHello;
		hasTipo = true;
		version = 2;
		hasVersion = true;
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
		//escribe Network mask
		if (hasNetworkMask)
			dos.writeInt(networkMask);
		//escribe helloInterval
		if (hasHelloInterval)
			dos.writeShort(helloInterval);
		//escribe options
		if (hasOptions)
			dos.writeByte(options);
		//escribe RtrPri
		if (hasRtrPri)
			dos.writeByte(rtrPri);
		//escribe RouterDeadInterval
		if (hasRouterDeadInterval)
			dos.writeInt(routerDeadInterval);
		//escribe DesignatedRouter
		if (hasDesignatedRouter)
			dos.writeInt(designatedRouter);
		//escribe BackupDesignatedRouter
		if (hasBackupDesignatedRouter)
			dos.writeInt(backupDesignatedRouter);
		//escribe Neighbors
		if (hasNeighbors){
			for (Integer neighbor: neighbors)
				dos.writeInt(neighbor);				
		}
		//colocar lo del baos detrás del mensajeSerializado
		ByteArrayOutputStream baosMensajeCompleto = new ByteArrayOutputStream( );
		baosMensajeCompleto.write(mensajeSerializado);
		baosMensajeCompleto.write(baos.toByteArray());
		
		mensajeSerializado= baosMensajeCompleto.toByteArray();
		
		//antes de calcular checksum, calcular y rellenar PacketLengh
		short packetLengthHello = (short) getLongitudSerializado();
		mensajeSerializado[2] =  (byte)((packetLengthHello >> 8) & 0xff);
		mensajeSerializado[3] = (byte)(packetLengthHello & 0xff);
		packetLength = packetLengthHello;
		
		//calcular checksum
		//checksum está a 0, calcularlo, rellenarlo (en el array mensaje Serializado)
		short checksumHello = (short) ChecksumOSPFv2.calcularChecksumOSPF(mensajeSerializado);
		mensajeSerializado[12] =  (byte)((checksumHello >> 8) & 0xff);
		mensajeSerializado[13] = (byte)(checksumHello & 0xff);
		checksum=checksumHello;
		
		output.write(mensajeSerializado); //longMensajeSerializado, longitudquehaescrito
		
	}

	@Override
	public int getLongitudSerializado() {
		//cabecera (super) + 20 octetos y 4 más por cada neighbor de la lista
		return super.getLongitudSerializado() + 20 + 4*neighbors.size(); 		 
	}
	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeOSPFv2.Builder implements IMensajeBuilder, IMensajeOSPFv2Hello, IMensajeOSPFv2Hello.Build {
		
		private Builder() {
			// No invoca al super()
			mensaje = new MensajeOSPFv2Hello();
		}

		protected MensajeOSPFv2Hello getMensaje() {
			return (MensajeOSPFv2Hello)this.mensaje;
		}

		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeOSPFv2Hello build() {
			if (getMensaje().estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o mï¿½s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			getMensaje().estaConstruido = true;

			return getMensaje();
		}

		public static MensajeOSPFv2Hello getDefaultInstanceforType() {
			MensajeOSPFv2Hello mensaje = new MensajeOSPFv2Hello();
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
				mezclarDesde(bis,null);
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
		public Builder mezclarDesde(InputStream inputStream, byte[] mensajeSerializado) throws IOException {					

			DataInputStream dis = new DataInputStream(inputStream);
			dis.read(mensajeSerializado, 24, inputStream.available());
						
			//Comprobar checksum del mensaje con la funcion verificar
			//si está bien, lees, sino checksumOK false
			if(ChecksumOSPFv2.verificarChecksumOSPF(mensajeSerializado)){
				ByteArrayInputStream bais = new ByteArrayInputStream(mensajeSerializado);
				dis = new DataInputStream(bais);
				//cabecera
				byte[] cabecera = new byte[24];
				dis.read(cabecera, 0, 24);
				
				//NetworkMask
				this.setNetworkMask(dis.readInt());
				//HelloInterval
				this.setHelloInterval(dis.readShort());
				//Options
				this.setOptions(dis.readByte());
				//RtrPri
				this.setRtrPri(dis.readByte());
				//RouterDeadInterval
				this.setRouterDeadInterval(dis.readInt());
				//DesignatedRouter
				this.setDesignatedRouter(dis.readInt());
				//BackupDesignatedRouter
				this.setBackupDesignatedRouter(dis.readInt());
				//Neighbors
				byte[] bufer = new byte[4];
				while(dis.read(bufer,0,4) > 0){
					this.addNeighbor(java.nio.ByteBuffer.wrap(bufer).getInt());			
				}				
				setIsChecksumOK(true);
				
			}else{
				setIsChecksumOK(false);
			}
		
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public IMensajeBuilder mezclarDesde(IMensaje mensajeOrigen) {
			if (mensaje instanceof IMensajeOSPFv2)   
				super.mezclarDesde(mensajeOrigen);
			else if (mensaje instanceof IMensajeOSPFv2Hello){
				IMensajeOSPFv2Hello _mensajeOrigen = (IMensajeOSPFv2Hello) mensajeOrigen;

				if (_mensajeOrigen.hasNetworkMask())
					this.setNetworkMask(_mensajeOrigen.getNetworkMask());

				if (_mensajeOrigen.hasHelloInterval())
					this.setHelloInterval(_mensajeOrigen.getHelloInterval());

				if (_mensajeOrigen.hasOptions())
					this.setOptions(_mensajeOrigen.getOptions());

				if (_mensajeOrigen.hasRtrPri())
					this.setRtrPri(_mensajeOrigen.getRtrPri());

				if (_mensajeOrigen.hasRouterDeadInterval())
					this.setRouterDeadInterval(_mensajeOrigen.getRouterDeadInterval());

				if (_mensajeOrigen.hasDesignatedRouter())
					this.setDesignatedRouter(_mensajeOrigen.getDesignatedRouter());
				
				if (_mensajeOrigen.hasBackupDesignatedRouter())
					this.setBackupDesignatedRouter(_mensajeOrigen.getBackupDesignatedRouter());
				
				if (_mensajeOrigen.hasNeighbors())
					this.setNeighbors(_mensajeOrigen.getNeighbors());				
			}
			else
				throw new IllegalArgumentException("IMensajeOSPFv2Hello::mezclarDesde(IMensaje): objeto recibido no es de clase IMensajeOSPFv2Hello");
			
				return this;
		}


		@Override
		public boolean estaCompleto() {
			return ((getMensaje().hasNetworkMask) && (getMensaje().hasHelloInterval) && (getMensaje().hasOptions) && (getMensaje().hasRtrPri) &&
					(getMensaje().hasRouterDeadInterval) && (getMensaje().hasDesignatedRouter) && (getMensaje().hasBackupDesignatedRouter) && (getMensaje().hasNeighbors)); 
		}

		/* IMensajeRIPPeticion */

		@Override
		public int getNetworkMask() {
			// TODO Auto-generated method stub
			return getMensaje().networkMask;
		}
		public short getHelloInterval() {
			// TODO Auto-generated method stub
			return getMensaje().helloInterval;
		}
		public byte getOptions() {
			// TODO Auto-generated method stub
			return getMensaje().options;
		}
		public byte getRtrPri() {
			// TODO Auto-generated method stub
			return getMensaje().rtrPri;
		}
		public int getRouterDeadInterval() {
			// TODO Auto-generated method stub
			return getMensaje().routerDeadInterval;
		}
		public int getDesignatedRouter() {
			// TODO Auto-generated method stub
			return getMensaje().designatedRouter;
		}
		public int getBackupDesignatedRouter() {
			// TODO Auto-generated method stub
			return getMensaje().backupDesignatedRouter;
		}
		public List<Integer> getNeighbors() {
			// TODO Auto-generated method stub
			return getMensaje().neighbors;
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
		
		public Builder setHelloInterval(short helloInterval) {
			getMensaje().helloInterval =helloInterval;
			getMensaje().hasHelloInterval = true;
			return this;
		}
		
		public Builder setOptions(byte options) {
			getMensaje().options =options;
			getMensaje().hasOptions = true;
			return this;
		}
		public Builder setRtrPri(byte rtrPri) {
			getMensaje().rtrPri =rtrPri;
			getMensaje().hasRtrPri = true;
			return this;
		}
		
		public Builder setRouterDeadInterval(int routerDeadInterval) {
			getMensaje().routerDeadInterval =routerDeadInterval;
			getMensaje().hasRouterDeadInterval = true;
			return this;
		}
		
		public Builder setDesignatedRouter(int designatedRouter) {
			getMensaje().designatedRouter =designatedRouter;
			getMensaje().hasDesignatedRouter = true;
			return this;
		}
		
		public Builder setBackupDesignatedRouter(int backupDesignatedRouter) {
			getMensaje().backupDesignatedRouter =backupDesignatedRouter;
			getMensaje().hasBackupDesignatedRouter = true;
			return this;
		}

		public Builder setNeighbors(List<Integer> neighbors) {
			if (getMensaje().neighbors == null){
				getMensaje().neighbors =neighbors;
			}
			else{
				for (Integer neighbor: neighbors)
					getMensaje().neighbors.add(neighbor);
			}
			getMensaje().hasNeighbors = true;		
			return this;
		}
		
		/*@Override
		public Builder removeRIPRutas() {
			getMensaje().ripRutas = null;
			getMensaje().hasRutas = false;
			return this;
		}*/
		
		public Builder removeNeighbors() {
			getMensaje().neighbors = null;
			getMensaje().hasNeighbors = false;
			return this;
		}

		/*@Override
		public Builder addRIPRuta(IMensajeRIPRuta mensajeRIPRuta) {
			if (getMensaje().ripRutas == null)
				getMensaje().ripRutas = new ArrayList<IMensajeRIPRuta>(MAXRIPRUTAS);
			getMensaje().ripRutas.add(mensajeRIPRuta);
			getMensaje().hasRutas = true;
			return this;
		}*/
		
		@Override
		public Builder addNeighbor(int neighbor) {
			if (getMensaje().neighbors == null)
				getMensaje().neighbors = new ArrayList<Integer>();
			getMensaje().neighbors.add(neighbor);
			getMensaje().hasNeighbors = true;
			return this;
		}

		/*@Override
		public boolean hasRIPRutas() {
			return getMensaje().hasRutas;
		}*/
		
		public boolean hasNetworkMask() {
			return getMensaje().hasNetworkMask;
		}
		public boolean hasHelloInterval() {
			return getMensaje().hasHelloInterval;
		}
		public boolean hasOptions() {
			return getMensaje().hasOptions;
		}
		public boolean hasRtrPri() {
			return getMensaje().hasRtrPri;
		}
		public boolean hasRouterDeadInterval() {
			return getMensaje().hasRouterDeadInterval;
		}
		public boolean hasDesignatedRouter() {
			return getMensaje().hasDesignatedRouter;
		}
		public boolean hasBackupDesignatedRouter() {
			return getMensaje().hasBackupDesignatedRouter;
		}
		public boolean hasNeighbors() {
			return getMensaje().hasNeighbors;
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
	public short getHelloInterval() {
		// TODO Auto-generated method stub
		return this.helloInterval;
	}
	public byte getOptions() {
		// TODO Auto-generated method stub
		return this.options;
	}
	public byte getRtrPri() {
		// TODO Auto-generated method stub
		return this.rtrPri;
	}
	public int getRouterDeadInterval() {
		// TODO Auto-generated method stub
		return this.routerDeadInterval;
	}
	public int getDesignatedRouter() {
		// TODO Auto-generated method stub
		return this.designatedRouter;
	}
	public int getBackupDesignatedRouter() {
		// TODO Auto-generated method stub
		return this.backupDesignatedRouter;
	}
	public List<Integer> getNeighbors() {
		// TODO Auto-generated method stub
		return this.neighbors;
	}
	
	public boolean hasNetworkMask() {
		return this.hasNetworkMask;
	}
	public boolean hasHelloInterval() {
		return this.hasHelloInterval;
	}
	public boolean hasOptions() {
		return this.hasOptions;
	}
	public boolean hasRtrPri() {
		return this.hasRtrPri;
	}
	public boolean hasRouterDeadInterval() {
		return this.hasRouterDeadInterval;
	}
	public boolean hasDesignatedRouter() {
		return this.hasDesignatedRouter;
	}
	public boolean hasBackupDesignatedRouter() {
		return this.hasBackupDesignatedRouter;
	}
	public boolean hasNeighbors() {
		return this.hasNeighbors;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----> Mensaje Hello OSPFv2: ");
		
		if(hasTipo)
			sb.append("Tipo " +Byte.toString(tipo.getCodigo()));
		if(hasVersion)
			sb.append(",Versión " + Byte.toString(version));
		if(hasPacketLength)
			sb.append(",PacketLength " + Short.toString(packetLength));
		if(hasRouterID)
			sb.append(",RouterID " + Integer.toString(routerID));
		if(hasAreaID)
			sb.append(",AreaID " + Integer.toString(areaID));
		if(hasChecksum)
			sb.append(",Checksum " + Short.toString(checksum));
		if(hasAutype)
			sb.append(",Autype " + Short.toString(autype));
		if(hasAuthentication)
			sb.append(",Authentication " + Long.toString(authentication));
		
	
		
		if (hasNetworkMask)
			sb.append(",NetworkMask " + Integer.toString(networkMask));
		
		if (hasHelloInterval)
			sb.append(",HelloInterval " + Short.toString(helloInterval));
		
		if (hasOptions)
			sb.append(",Options " + Byte.toString(options));
		
		if (hasRtrPri)
			sb.append(",RtrPri " + Byte.toString(rtrPri));
		
		if (hasRouterDeadInterval)
			sb.append(",RouterDeadInterval " + Integer.toString(routerDeadInterval));
		
		if (hasDesignatedRouter)
			sb.append(",DesignatedRouter " + Integer.toString(designatedRouter));
		
		if (hasBackupDesignatedRouter)
			sb.append(",BackupDesignatedRouter " + Integer.toString(backupDesignatedRouter));
		
		
		if (this.neighbors != null)
			sb.append(",Neighbors ");
			for (Integer neighbor: neighbors)
				sb.append(neighbor.toString() +" ");

		return sb.toString();
	}

}

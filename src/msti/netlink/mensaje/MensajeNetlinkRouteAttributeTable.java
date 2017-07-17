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

public class MensajeNetlinkRouteAttributeTable extends MensajeNetlinkRouteAttribute implements IMensaje, IMensajeNetlinkRouteAttributeTable {
	/* si construyó y entregó el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	protected int table;
	protected boolean hasTable = false;
	
	protected MensajeNetlinkRouteAttributeTable() {
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
	 * Este objeto es un miembro de una unión implícita en la propia clase
	 * Por ello, siempre incluirá en la salida los campos selector de la unión (tipo/longitud). 
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
		DataOutputStream dos = new DataOutputStream(output);
		int escritos = 0;
		
		// Selectores de la unión (cabecera en Attribute)
		super.writeToOutputStream(output);
		escritos = super.getLongitudSerializado(); // se podría obtener también del output

		/* Alineamiento tras cabecera */
		for (int i = getRelleno(escritos); i > 0; i--, escritos++)
			dos.writeByte((byte)0);

		// Datos
		
		/* table */
		dos.writeInt(this.getTable());
		escritos += 4;
	}

	protected int getLongitudMensaje() {
		int total;

		// Cabecera + relleno
		total = super.getLongitudSerializado();
		total += getRelleno(total);
		// Datos
		total += 4;  
		return total;  //no relleno tras datos
	}
	@Override
	public int getLongitudSerializado() {
		return getLongitudMensaje(); // no añade relleno tras atributo, el que ponga varios juntos debe alinearlos
	}

	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeNetlinkRouteAttribute.Builder implements IMensajeBuilder, IMensajeNetlinkRouteAttributeTable, IMensajeNetlinkRouteAttributeTable.Build {
		
		private Builder() {
			_builder = this;
			mensaje = new MensajeNetlinkRouteAttributeTable(); //inicializa el mensaje heredado con el tipo adecuado
		}

		/* Sobreescribe para cambiar el tipo devuelto y usarlo tipado donde se requiera */
		@Override
		protected MensajeNetlinkRouteAttributeTable getMensaje() {
			return (MensajeNetlinkRouteAttributeTable)this.mensaje;
		}

		
		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeNetlinkRouteAttributeTable build() {
			if (mensaje.estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o más campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			mensaje.estaConstruido = true;

			return getMensaje();
		}

		public static MensajeNetlinkRouteAttributeTable getDefaultInstanceforType() {
			// TODO: Petición de tabla de rutas puede ser el mensaje por defecto
			MensajeNetlinkRouteAttributeTable mensaje = new MensajeNetlinkRouteAttributeTable();
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
			System.out.println("netlinkroute: mezclar desde is");
			DataInputStream dis = new DataInputStream(inputStream);

			// El anterior mensaje debe dejar alineado

			// Leída cabecera sin relleno
			int leidos = 4; //TODO: no anclar este valor

			/* Alineamiento tras cabecera */
			for (int i = getRelleno(leidos); i > 0; i--, leidos++) 
				dis.readByte();

			/* Datos */
			
			/* table */
			this.setTable((getMensaje().isNetByteOrder ? dis.readInt() : reverseIfNeeded(dis.readInt())));
			leidos += 4;
			
			return this;
		}

		/** 
		 * Copia los campos modificados en mensajeOrigen al mensaje actual
		 */
		@Override
		public Builder mezclarDesde(IMensaje mensajeOrigen) {
			// Copia desde la base los campos comunes
			if (mensaje instanceof IMensajeNetlinkRouteAttribute) {
				System.out.println("Mezclar desde base (es Imensajenetlink o subclase");
				super.mezclarDesde(mensajeOrigen);
			}
			else if (mensaje instanceof IMensajeNetlinkRouteAttributeTable) {
				// Campos comunes ya copiados en el instanceof anterior

				System.out.println("Mezclar resto (es Imensajenetlinkroute o subclase)");

				// Copia sólo los campos rellenos desde el objeto origen
				MensajeNetlinkRouteAttributeTable _mensajeOrigen = (MensajeNetlinkRouteAttributeTable)mensajeOrigen;
				if (_mensajeOrigen.hasTable())
					this.setTable(_mensajeOrigen.getTable());
			}
			else 
				throw new IllegalArgumentException("mezclarDesde(IMensaje): objeto recibido no es de clase MensajeNetlinkRouteAttribute");

			return this;
		}


		@Override
		public boolean estaCompleto() {
			return (super.estaCompleto() &&
					getMensaje().hasTable());			
		}

		/* IMensajeNetlinkRouteAttributeTable */

		@Override
		public Builder setTable(int table) {
			getMensaje().table = table;
			getMensaje().hasTable = true;			
			return this;
		}

		@Override
		public int getTable() {
			return getMensaje().table;
		}

		@Override
		public boolean hasTable() {
			return getMensaje().hasTable;
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" MensajeNetlinkRouteAttributeTable");

		sb.append("\n Cabecera:");
		sb.append("  length="); 		sb.append(this.getLength());
		sb.append("; type="); 			sb.append(this.getAttributeType());
		
		sb.append("\n Datos:");
		sb.append("  table="); 			sb.append(this.getTable());
		
		return sb.toString();
	}

	@Override
	public int getTable() {
		return this.table;
	}

	@Override
	public boolean hasTable() {
		return this.hasTable;
	}

}

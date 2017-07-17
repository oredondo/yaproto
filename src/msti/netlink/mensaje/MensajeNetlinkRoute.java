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
import java.util.ArrayList;
import java.util.List;

import msti.io.mensaje.IMensaje;
import msti.io.mensaje.IMensajeBuilder;
import msti.netlink.mensaje.IMensajeNetlinkRouteAttribute.NetlinkRouteAttributeType;
import msti.util.RawSocketNetlink;

public class MensajeNetlinkRoute extends MensajeNetlink implements IMensaje, IMensajeNetlinkRoute {
	/* si construy� y entreg� el mensaje, no permite nuevas construcciones */
	protected boolean estaConstruido = false;
	
	protected byte routeAddressFamily;
	protected boolean hasRouteAddressFamily = false;
	
	protected byte longitudOrigen;
	protected boolean hasLongitudOrigen = false;
	
	protected byte longitudDestino;
	protected boolean hasLongitudDestino = false;
	
	protected byte routeTypeOfService;
	protected boolean hasRouteTypeOfService = false;

	protected RoutingTableId routingTableId;
	protected boolean hasRoutingTableId = false;
	
	protected RoutingProtocol routingProtocol;
	protected boolean hasRoutingProtocol = false;
	
	protected RouteScope routeScope;
	protected boolean hasRouteScope = false;
	
	protected RouteType routeType;
	protected boolean hasRouteType = false;
	
	protected RouteFlag routeFlag;
	protected boolean hasRouteFlag = false;
	
	protected List<IMensajeNetlinkRouteAttribute> netlinkAttributes;
	protected boolean hasNetlinkAttributes = false;

	/* Serializaci�n/deserializaci�n */
	protected static final int sizeof_sockaddr_nl;  /*  definido en nativo para Netlink */
	protected static final int sizeof_rtmsg;  /*  definido en nativo para Netlink */
	protected static final int sizeof_rtattr;  /*  definido en nativo para Netlink */
	protected static final int sizeof_rtnexthop;  /*  definido en nativo para Netlink */
	protected static final int sizeof_ifinfomsg;  /*  definido en nativo para Netlink */
	protected static final int sizeof_prefixmsg;  /*  definido en nativo para Netlink */
	
	/**
	 * Obtiene valores del nativo (dado que Netlink depende del nativo, no tiene todas las longitudes fijas).
	 * Est� pensado para comunicaci�n local-local, pero aqu� es comunicaci�n local-java(bigendian, tipos de
	 * longitud fija, etc.)
	 */
	static {
		/* Obtiene tama�os de compilaci�n en el nativo de ciertas estructuras variables */
		sizeof_sockaddr_nl = RawSocketNetlink.getSizeofType(RawSocketNetlink.TipoEstructura.SOCKADDR_NL); /* Direccion netlink */
		sizeof_rtmsg = RawSocketNetlink.getSizeofType(RawSocketNetlink.TipoEstructura.RTMSG);		/* Data para mensajes GETROUTE,DELROUTE,NEWROUTE */
		sizeof_rtattr = RawSocketNetlink.getSizeofType(RawSocketNetlink.TipoEstructura.RTATTR);		/* Atributo para familia route */
		sizeof_rtnexthop = RawSocketNetlink.getSizeofType(RawSocketNetlink.TipoEstructura.RTNETXTHOP);	/* Datos como cadena: nhop+data+atrib,nhop+data+atrib */
		sizeof_ifinfomsg = RawSocketNetlink.getSizeofType(RawSocketNetlink.TipoEstructura.IFINFOMSG);	/* GETLINK,DELLINK,NEWLINK: TODO: llevar a MensajeNetlinkLink */
		sizeof_prefixmsg = RawSocketNetlink.getSizeofType(RawSocketNetlink.TipoEstructura.PREFIXMSG);	/* prefijo del mensaje, TODO: idem anterior */
	}
	
	protected MensajeNetlinkRoute() {
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
	 * Este objeto es un miembro de una uni�n (herencia de MensajeRIP, selector: tipo/version) impl�cita en la propia clase
	 * Por ello, siempre incluir� en la salida los campos selector de la uni�n (tipo/version). 
	 * 
	 */
	@Override
	public void writeToOutputStream(OutputStream output) throws IOException {
		DataOutputStream dos = new DataOutputStream(output);
		int escritos;
		
		// Selectores de la uni�n (cabecera Netlink)
		super.writeToOutputStream(output);
		escritos = super.getLongitudSerializado(); // se podr�a obtener tb del output

		/* Alineamiento tras cabecera */
		for (int i = getRelleno(escritos); i > 0; i--, escritos++) 
			dos.writeByte(0);

		// Mensaje netlinkRoute
				
		/* familia de direcciones */
		dos.writeByte(this.getRouteAddressFamily());
		/* longitud destino */
		dos.writeByte(this.getLongitudDestino());
		/* longitud origen */
		dos.writeByte(this.getLongitudOrigen());
		/* tos */
		dos.writeByte(this.getRouteTypeOfService());		
		/* table */
		dos.writeByte((byte) this.getRoutingTableId().getValue());
		/* protocol */
		dos.writeByte((byte) this.getRoutingProtocol().getValue());
		/* scope */
		dos.writeByte((byte) this.getRouteScope().getValue());
		/* type */
		dos.writeByte((byte) this.getRouteType().getValue());
		/* flags */
		dos.writeByte((byte) this.getRouteFlag().getValue());
		/* Escritos */
		escritos += 9;
		
		/* Alineamiento tras mensaje route */
		for (int i = getRelleno(escritos); i > 0; i--, escritos++) 
			dos.writeByte(0);

		// Atributos opcionales
		for (IMensajeNetlinkRouteAttribute netlinkAttribute: this.netlinkAttributes) {
			/* Escribe el atributo */
			((IMensaje)netlinkAttribute).writeToOutputStream(output);
			escritos += ((IMensaje)netlinkAttribute).getLongitudSerializado();	
			
			/* Alineamiento tras el atributo (incluido el �ltimo atributo) */
			for (int i = getRelleno(escritos); i > 0; i--, escritos++) 
				dos.writeByte(0);
		}
	}

	protected int getLongitudMensaje() {
		int total;
		int ultimoRelleno;

		// Cabecera + relleno
		total = super.getLongitudSerializado();
		total += getRelleno(total);

		// Mensaje + relleno
		total += 9;  // mensaje
		ultimoRelleno = getRelleno(total);
		total += ultimoRelleno;
		
		// Atributos con relleno entre ellos
		for (IMensajeNetlinkRouteAttribute netlinkAttribute: this.netlinkAttributes) {
			total += ((MensajeNetlinkRouteAttribute)netlinkAttribute).getLongitudSerializado();
			ultimoRelleno = getRelleno(total);
			total += ultimoRelleno;
		}
		// Quita el �ltimo relleno para conseguir la longitud del mensaje (campo len de la cabecera)
		total -= ultimoRelleno;

		return total;
	}
	@Override
	public int getLongitudSerializado() {
		int longitudMensaje = getLongitudMensaje();  // Mensaje sin relleno final
		
		longitudMensaje += getRelleno(longitudMensaje);  //A�ade el �ltimo relleno tras el �ltimo atributo
		return longitudMensaje;
	}

	
	/*
	 * Clase MensajeRIP.Builder
	 * Para que no sea externa y poder poner los constructores del mensaje como privados, p.ej.
	 */
	public static class Builder extends MensajeNetlink.Builder implements IMensajeBuilder, IMensajeNetlinkRoute, IMensajeNetlinkRoute.Build {
		
		private Builder() {
			_builder = this;
			mensaje = new MensajeNetlinkRoute(); //inicializa el mensaje heredado con el tipo adecuado
		}

		/* Sobreescribe para cambiar el tipo devuelto y usarlo tipado donde se requiera */
		@Override
		protected MensajeNetlinkRoute getMensaje() {
			return (MensajeNetlinkRoute)this.mensaje;
		}

		
		public static Builder crear() {
			return new Builder();
		}

		@Override
		public MensajeNetlinkRoute build() {
			if (mensaje.estaConstruido)
				throw new IllegalStateException("Solicitado build() por segunda o sucesivas veces de un objeto ya construido.");
			if (! estaCompleto())
				throw new IllegalStateException("Solicitado build() sobre objeto no completo (uno o m�s campos obligatorios sin rellenar)");

			// Marca para que falle siguiente build()
			mensaje.estaConstruido = true;

			return getMensaje();
		}

		public static MensajeNetlinkRoute getDefaultInstanceforType() {
			// TODO: Petici�n de tabla de rutas puede ser el mensaje por defecto
			MensajeNetlinkRoute mensaje = new MensajeNetlinkRoute();
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
			MensajeNetlinkRouteAttribute.Builder atributoBuilder;
			MensajeNetlinkRouteAttribute atributo;

			// El anterior debe dejar alineado
			// Mensaje netlinkRoute
			int leidos = 16; //cabecera
			
			/* Salta el relleno de alineamienti, si hubiera */
			for (int i = getRelleno(leidos); i > 0; i--, leidos++) 
				dis.readByte();

			/* Datos: mensaje de netlink_route rtmsg */

			/* familia de direcciones */
			this.setRouteAddressFamily(dis.readByte());
			System.out.println("rt_familia=" + getMensaje().getRouteAddressFamily());
			/* longitud destino */
			this.setLongitudDestino(dis.readByte());
			System.out.println("rt_dst_len=" + getMensaje().getLongitudDestino());
			/* longitud destino */
			this.setLongitudOrigen(dis.readByte());
			System.out.println("rt_src_len=" + getMensaje().getLongitudOrigen());
			/* tos */
			this.setRouteTypeOfService(dis.readByte());
			System.out.println("rt_tos=" + getMensaje().getRouteTypeOfService());
			/* table */
			this.setRoutingTableId(RoutingTableId.getByValue(dis.readByte()));
			System.out.println("rt_tableid=" + getMensaje().getRoutingTableId());
			/* protocol */
			this.setRoutingProtocol(RoutingProtocol.getByValue(dis.readByte()));
			System.out.println("rt_proto=" + getMensaje().getRoutingProtocol());
			/* scope */
			this.setRouteScope(RouteScope.getByValue(dis.readByte()));
			System.out.println("rt_scope=" + getMensaje().getRouteScope());
			/* type */
			this.setRouteType(RouteType.getByValue(dis.readByte()));
			System.out.println("rt_type=" + getMensaje().getRouteType());
			/* flags */
			this.setRouteFlag(RouteFlag.getByValue(dis.readByte()));
			System.out.println("rt_flags" + getMensaje().getRouteFlag());
			/* Leidos */
			leidos += 9;
			
			System.out.println("mezclar desde is: leidos=" + leidos + " relleno=" + getRelleno(leidos));
			/* Alineamiento */
			for (int i = getRelleno(leidos); i > 0; i--, leidos++) 
				dis.readByte();

			/* Lee atributos */
			while ((leidos < mensaje.length) && ((mensaje.length - leidos) > (getRelleno(leidos) + 4))) { // al menos una cabecera atributo completa detr�s

				atributoBuilder = MensajeNetlinkRouteAttribute.Builder.crear()
						.mezclarDesde(inputStream);
				// Si es un Src, le configura el prefijo, si está en el mensaje route  //TODO: pasar un parent???
				if ((atributoBuilder.getAttributeType() == NetlinkRouteAttributeType.RTA_PREFSRC) &&
						getMensaje().hasLongitudOrigen)
					((MensajeNetlinkRouteAttributeSrc.Builder)atributoBuilder).setPrefixLength(getMensaje().longitudOrigen);
				// Si es un Dst, le configura el prefijo, si está en el mensaje route  
				if ((atributoBuilder.getAttributeType() == NetlinkRouteAttributeType.RTA_DST) &&
						getMensaje().hasLongitudDestino)
					((MensajeNetlinkRouteAttributeDst.Builder)atributoBuilder).setPrefixLength(getMensaje().longitudDestino);

				// Inserta el atributo si tiene un tipo válido (debería siempre)
				atributo = atributoBuilder.build();
				if (atributoBuilder.getAttributeType() != NetlinkRouteAttributeType.RTA_MAX)
					this.addNetlinkAttribute(atributo);

				leidos += atributo.getLongitudSerializado(); // no incluye relleno
				// Alineamiento
				for (int i = getRelleno(leidos); i > 0; i--, leidos++) 
					dis.readByte();
				System.out.println("leidos_msg=" + leidos + " de " + mensaje.length);
			}

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

				// Copia los campos espec�ficos
				MensajeNetlinkRoute _mensajeOrigen = (MensajeNetlinkRoute)mensajeOrigen;
				if (_mensajeOrigen.hasRouteAddressFamily())
					this.setRouteAddressFamily(_mensajeOrigen.getRouteAddressFamily());
				if (_mensajeOrigen.hasRouteTypeOfService())
					this.setRouteTypeOfService(_mensajeOrigen.getRouteTypeOfService());
				if (_mensajeOrigen.hasRoutingTableId())
					this.setRoutingTableId(_mensajeOrigen.getRoutingTableId());
				if (_mensajeOrigen.hasRoutingProtocol())
					this.setRoutingProtocol(_mensajeOrigen.getRoutingProtocol());
				if (_mensajeOrigen.hasRouteScope())
					this.setRouteScope(_mensajeOrigen.getRouteScope());
				if (_mensajeOrigen.hasRouteType())
					this.setRouteType(_mensajeOrigen.getRouteType());
				if (_mensajeOrigen.hasRouteFlag())
					this.setRouteFlag(_mensajeOrigen.getRouteFlag());
				if (_mensajeOrigen.hasNetlinkAttributes())
					this.setNetlinkAttributes(_mensajeOrigen.getNetlinkAttributes());
			}
			else 
				throw new IllegalArgumentException("MensajeNetlinkRoute::mezclarDesde(IMensaje): objeto recibido no es de clase MensajeRIP");

			return this;
		}


		@Override
		public boolean estaCompleto() {
			boolean completo = false;

			if (mensaje.hasMessageType) {

				switch (mensaje.messageType) {
				case RTM_NEWROUTE:
				case RTM_DELROUTE:
					completo = (super.estaCompleto() &&
							getMensaje().hasRouteAddressFamily &&
							getMensaje().hasRouteTypeOfService &&
							getMensaje().hasRoutingTableId &&
							getMensaje().hasRoutingProtocol &&
							getMensaje().hasRouteScope &&
							getMensaje().hasRouteType &&
							getMensaje().hasRouteFlag);
					break;
				case RTM_GETROUTE:
					completo = (super.estaCompleto() && 
							getMensaje().hasRouteAddressFamily &&
							getMensaje().hasRouteTypeOfService &&
							getMensaje().hasRoutingTableId &&
							getMensaje().hasRoutingProtocol &&
							getMensaje().hasRouteScope &&
							getMensaje().hasRouteType &&
							getMensaje().hasRouteFlag);
					break;
				default:
					throw new UnsupportedOperationException ("Tipo de mensaje NETLINK_ROUTE a�n no implementado.");
				}
			}
			return completo;			
		}

		/* IMensajeRIPPeticion */

		@Override
		public List<IMensajeNetlinkRouteAttribute> getNetlinkAttributes() {
			return getMensaje().netlinkAttributes;
		}

		/**
		 * No realiza copia de los objetos IMensajeRipRuta.
		 * Si el mensaje no ten�a lista, establece como lista la indicada. Si ten�a lista, a�ade los elementos de 
		 * la lista indicada a la existente.
		 */
		@Override
		public Builder setNetlinkAttributes(List<IMensajeNetlinkRouteAttribute> netlinkAttributes) {
			if (getMensaje().netlinkAttributes == null)
				getMensaje().netlinkAttributes = netlinkAttributes;
			else
				for (IMensajeNetlinkRouteAttribute mensajeNetlinkAttribute: getMensaje().netlinkAttributes)
					getMensaje().netlinkAttributes.add(mensajeNetlinkAttribute);
			getMensaje().hasNetlinkAttributes = true;
			return this;
		}

		@Override
		public Builder removeNetlinkAttributes() {
			getMensaje().netlinkAttributes = null;
			getMensaje().hasNetlinkAttributes = false;
			return this;
		}

		@Override
		public Builder addNetlinkAttribute(IMensajeNetlinkRouteAttribute mensajeNetlinkAttribute) {
			if (getMensaje().netlinkAttributes == null)
				getMensaje().netlinkAttributes = new ArrayList<IMensajeNetlinkRouteAttribute>();
			getMensaje().netlinkAttributes.add(mensajeNetlinkAttribute);
			getMensaje().hasNetlinkAttributes = true;
			return this;
		}

		@Override
		public Builder setRouteAddressFamily(byte routeAddressFamily) {
			getMensaje().routeAddressFamily = routeAddressFamily;
			getMensaje().hasRouteAddressFamily = true;			
			return this;
		}

		@Override
		public Builder setRouteTypeOfService(byte routeTypeOfService) {
			getMensaje().routeTypeOfService = routeTypeOfService;
			getMensaje().hasRouteTypeOfService = true;			
			return this;
		}

		@Override
		public Builder setRoutingTableId(RoutingTableId routingTableId) {
			getMensaje().routingTableId = routingTableId;
			getMensaje().hasRoutingTableId = true;						
			return this;
		}

		@Override
		public Builder setRoutingProtocol(RoutingProtocol routingProtocol) {
			getMensaje().routingProtocol = routingProtocol;
			getMensaje().hasRoutingProtocol = true;						
			return this;
		}

		@Override
		public Builder setRouteScope(RouteScope routeScope) {
			getMensaje().routeScope = routeScope;
			getMensaje().hasRouteScope = true;			
			return this;
		}

		@Override
		public Builder setRouteType(RouteType routeType) {
			getMensaje().routeType = routeType;
			getMensaje().hasRouteType = true;			
			return this;
		}

		@Override
		public Builder setRouteFlag(RouteFlag routeFlag) {
			getMensaje().routeFlag = routeFlag;
			getMensaje().hasRouteFlag = true;			
			return this;
		}

		public Builder setLongitudOrigen(byte longitudOrigen) {
			getMensaje().longitudOrigen = longitudOrigen;
			getMensaje().hasLongitudOrigen = true;			
			return this;
		}

		public Builder setLongitudDestino(byte longitudDestino) {
			getMensaje().longitudDestino = longitudDestino;
			getMensaje().hasLongitudDestino = true;			
			return this;
		}

		@Override
		public byte getRouteAddressFamily() {
			return getMensaje().routeAddressFamily;
		}

		@Override
		public byte getRouteTypeOfService() {
			return getMensaje().routeTypeOfService;
		}

		@Override
		public RoutingTableId getRoutingTableId() {
			return getMensaje().routingTableId;
		}

		@Override
		public RoutingProtocol getRoutingProtocol() {
			return getMensaje().routingProtocol;
		}

		@Override
		public RouteScope getRouteScope() {
			return getMensaje().routeScope;
		}

		@Override
		public RouteType getRouteType() {
			return getMensaje().routeType;
		}

		@Override
		public RouteFlag getRouteFlag() {
			return getMensaje().routeFlag;
		}


		@Override
		public boolean hasRouteAddressFamily() {
			return getMensaje().hasRouteAddressFamily;
		}

		@Override
		public boolean hasRouteTypeOfService() {
			return getMensaje().hasRouteTypeOfService;
		}

		@Override
		public boolean hasRoutingTableId() {
			return getMensaje().hasRoutingTableId;
		}

		@Override
		public boolean hasRoutingProtocol() {
			return getMensaje().hasRoutingProtocol;
		}

		@Override
		public boolean hasRouteScope() {
			return getMensaje().hasRouteScope;
		}

		@Override
		public boolean hasRouteType() {
			return getMensaje().hasRouteType;
		}

		@Override
		public boolean hasRouteFlag() {
			return getMensaje().hasRouteFlag;
		}

		@Override
		public boolean hasNetlinkAttributes() {
			return getMensaje().hasNetlinkAttributes;
		}

	}

	@Override
	public List<IMensajeNetlinkRouteAttribute> getNetlinkAttributes() {
		return this.netlinkAttributes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----> MensajeNetlinkRoute");

		sb.append("\n Cabecera:");
		sb.append("  length="); 		sb.append(this.getLength());
		sb.append("; type="); 		sb.append(this.getMessageType());
		sb.append("; pid="); 			sb.append(this.getProcessId());
		sb.append("; seq="); 			sb.append(this.getSequenceNumber());
		sb.append("; flags=");		sb.append(this.getFlags());
		
		sb.append("\n Mensaje route:");
		sb.append("  family="); 		sb.append(this.getRouteAddressFamily());
		sb.append("; tos="); 			sb.append(this.getRouteTypeOfService());
		sb.append("; src_len="); 		sb.append(this.getLongitudOrigen());
		sb.append("; dst_len="); 		sb.append(this.getLongitudDestino());
		sb.append("; tableid="); 		sb.append(this.getRoutingTableId());
		sb.append("; protocol="); 		sb.append(this.getRoutingProtocol());
		sb.append("; scope="); 			sb.append(this.getRouteScope());
		sb.append("; type="); 			sb.append(this.getRouteType());
		sb.append("; flags="); 			sb.append(this.getRouteFlag());
		
		if (this.hasNetlinkAttributes)
			for (IMensajeNetlinkRouteAttribute mensajeNetlinkAttribute: this.netlinkAttributes)
				sb.append(mensajeNetlinkAttribute.toString());

		return sb.toString();
	}

	@Override
	public byte getRouteAddressFamily() {
		return this.routeAddressFamily;
	}

	@Override
	public byte getRouteTypeOfService() {
		return this.routeTypeOfService;
	}

	@Override
	public RoutingTableId getRoutingTableId() {
		return this.routingTableId;
	}

	@Override
	public RoutingProtocol getRoutingProtocol() {
		return this.routingProtocol;
	}

	@Override
	public RouteScope getRouteScope() {
		return this.routeScope;
	}

	@Override
	public RouteType getRouteType() {
		return this.routeType;
	}

	@Override
	public RouteFlag getRouteFlag() {
		return this.routeFlag;
	}

	public byte getLongitudOrigen() {
		return this.longitudOrigen;
	}

	public byte getLongitudDestino() {
		return this.longitudDestino;
	}

	@Override
	public boolean hasRouteAddressFamily() {
		return this.hasRouteAddressFamily;
	}

	@Override
	public boolean hasRouteTypeOfService() {
		return this.hasRouteTypeOfService;
	}

	@Override
	public boolean hasRoutingTableId() {
		return this.hasRoutingTableId;
	}

	@Override
	public boolean hasRoutingProtocol() {
		return this.hasRoutingProtocol;
	}

	@Override
	public boolean hasRouteScope() {
		return this.hasRouteScope;
	}

	@Override
	public boolean hasRouteType() {
		return this.hasRouteType;
	}

	@Override
	public boolean hasRouteFlag() {
		return this.hasRouteFlag;
	}

	@Override
	public boolean hasNetlinkAttributes() {
		return this.hasNetlinkAttributes;
	}
}

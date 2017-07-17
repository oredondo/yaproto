/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.net.DatagramSocket;
import java.net.SocketAddress;

public class SesionDatagrama extends Sesion {

	/**
	 * Constructor para SesionDatagrama (sesión en modo no flujo)
	 * 
	 * @param aceptador  Aceptador que generó la sesión
	 * @param sesionConfiguracion  Configuración asociada a la sesión
	 */
	public SesionDatagrama(Aceptador aceptador, SesionConfiguracion sesionConfiguracion) {
		super(aceptador, sesionConfiguracion, false); // esmodoFlujo == false
		// TODO Auto-generated constructor stub
	}

	/** Dirección remota de envío de los próximos mensajes: cuando es flujo no se utiliza */
	private SocketAddress direccionRemota;
	
	/** Dirección local */
	private SocketAddress direccionLocal;
	
	/** Socket */
	private DatagramSocket socket;


	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setDireccionRemota(SocketAddress direccionRemota) {
		this.direccionRemota = direccionRemota;
	}

	public SocketAddress getDireccionRemota() {
		return direccionRemota;
	}

	public void setDireccionLocal(SocketAddress direccionLocal) {
		this.direccionLocal = direccionLocal;
	}

	public SocketAddress getDireccionLocal() {
		return direccionLocal;
	}

	/**
	 * Escribe un mensaje saliente (de forma asíncrona). Es decir, la función sólo encol el mensaje para su procesado y envío posterior
	 * @param mensaje
	 * @param direccionRemota. Si no se proporciona (null), se usa la preasignada.
	 */
	public void escribir(Escritura escritura) {
		
        if ( this.esModoFlujo && (direccionRemota != null)) {
            throw new IllegalArgumentException("No compatible especificar una dirección remota con transporte modo flujo");
        }

        // Encola la solicitud en el escritor (sin espera a que llegue al canal)
        this.getEscritor().escribirAsincrono(this, escritura);        
	}


}

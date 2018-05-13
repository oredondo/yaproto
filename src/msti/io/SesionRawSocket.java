/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.net.InetAddress;

import com.savarese.rocksaw.net.RawSocket;

import msti.util.RawSocketNetlink;
import msti.util.RawSocketNetlink.NetlinkAddress;

public class SesionRawSocket extends Sesion {

	/**
	 * Constructor para SesionDatagrama (sesi�n en modo no flujo)
	 * 
	 * @param aceptador  Aceptador que gener� la sesi�n
	 * @param sesionConfiguracion  Configuraci�n asociada a la sesi�n
	 */
	public SesionRawSocket(Aceptador aceptador, SesionConfiguracion sesionConfiguracion) {
		super(aceptador, sesionConfiguracion, false); // esmodoFlujo == false
		// TODO Auto-generated constructor stub
	}

	/** Direcci�n remota de env�o de los pr�ximos mensajes: cuando es flujo no se utiliza */
	private InetAddress direccionRemota;
	
	/** Direcci�n local */
	private InetAddress direccionLocal;
	
	/** Socket */
	private RawSocket socket;


	public void setSocket(RawSocket socket) {
		this.socket = socket;
	}

	public RawSocket getSocket() {
		return socket;
	}

	public void setDireccionRemota(InetAddress direccionRemota) {
		this.direccionRemota = direccionRemota;
	}

	public InetAddress getDireccionRemota() {
		return direccionRemota;
	}

	public void setDireccionLocal(InetAddress direccionLocal) {
		this.direccionLocal = direccionLocal;
	}

	public InetAddress getDireccionLocal() {
		return direccionLocal;
	}

	/**
	 * Escribe un mensaje saliente (de forma as�ncrona). Es decir, la funci�n s�lo encol el mensaje para su procesado y env�o posterior
	 * @param mensaje
	 * @param direccionRemota. Si no se proporciona (null), se usa la preasignada.
	 */
	public void escribir(Escritura escritura) {
		
        if ( this.esModoFlujo && (direccionRemota != null)) {
            throw new IllegalArgumentException("No compatible especificar una direcci�n remota con transporte modo flujo");
        }

        // Encola la solicitud en el escritor (sin espera a que llegue al canal)
        this.getEscritor().escribirAsincrono(this, escritura);        
	}


}

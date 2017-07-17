/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class AceptadorDatagramaMulticast extends AceptadorDatagrama {

	/** 
	 * No se usa un CopyOnWriteArrayList porque esta configuración se realiza antes de poner en marcha el aceptador
	 * sin concurrencia.
	 */
	protected final ArrayList<InetAddress> grupos;
	
	public AceptadorDatagramaMulticast() {
		super();
		grupos = new ArrayList<InetAddress>();
		isMulticast = true; //marca como AceptadorDatagrama de tipo multicast
	}

	/**
	 * Bind 
	 */
	@Override
	public void bind(SocketAddress address) throws SocketException {
		try {
			this.socket = new MulticastSocket(address);
		} catch (IOException e) {
			// Convierte la excepción para compatibilidad con el método del cual deriva
			throw new SocketException(e.getMessage());
		}		
	}

	public void unirGrupo(InetAddress grupo) throws IOException {
		((MulticastSocket)this.socket).joinGroup(grupo);
		grupos.add(grupo);
	}

	public void abandonarGrupo(InetAddress grupo) throws IOException {
		((MulticastSocket)this.socket).leaveGroup(grupo);
		
		Iterator<InetAddress> iterador = grupos.iterator();
		while (iterador.hasNext()) {
		    if (Arrays.equals(((InetAddress)iterador.next()).getAddress(), grupo.getAddress()))
		    		iterador.remove();  // única forma segura de borrar mientras se itera :)
		}				
	}

	public ArrayList<InetAddress> getGrupos() {
		return grupos;
	}
}

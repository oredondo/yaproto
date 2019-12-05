/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2;

import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.FiltroNotificador;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.netlink.mensaje.MensajeNetlink;

public class TestNetlink implements ISesionCreadaListener,ILecturaListener {
	
	public TestNetlink() {
		System.out.println("Test instanciado");
	}

	@Override
	public void sesionCreada(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("Sesión creada: id=" + sesion.getId());
		((FiltroNotificador)sesion.getAceptador().getCadenaFiltros().getLast()).addLecturaListener(this, sesion.getId());
		System.out.println("   añadido ILecturaListener");		
	}

	@Override
	public void sesionInactiva(Sesion sesion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		System.out.println("Mensaje netlink recibido!");
		System.out.println("  sesion: id=" + sesion.getId());
		System.out.println("  lectura: id=" + lectura.getId() + "; mensaje=" + lectura.toString());
		
		if (lectura.getMensaje() instanceof MensajeNetlink) {
			/* */
		}
		
//		Escritura escritura = new Escritura(lectura.getMensaje());
//		escritura.setDireccionDestino(lectura.getDireccionOrigen());
//		sesion.escribir(escritura);

		return true;
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		System.out.println("Excepción capturada");
		System.out.println("   lectura: id=" + lectura.getId());
		e.printStackTrace();		
	}

	
}
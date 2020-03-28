/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2;

import msti.io.Escritura;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.FiltroNotificador;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.rip.mensaje.IMensajeRIPRuta;
import msti.rip.mensaje.MensajeRIPPeticion;

public class Test implements ISesionCreadaListener,ILecturaListener {


	public Test() {
		System.out.println("Test: Test(): Test instanciado");
	}

	@Override
	public void sesionCreada(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("Test: sesionCreada(): id=" + sesion.getId());
		((FiltroNotificador)sesion.getAceptador().getCadenaFiltros().getLast()).addLecturaListener(this, sesion.getId());
	}

	@Override
	public void sesionInactiva(Sesion sesion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		// TODO Auto-generated method stub
		
	}

	private void printRuta(IMensajeRIPRuta r) {
		System.out.print("   afi=" + r.getIdFamiliaDirecciones());
		System.out.print("   etiq=" + r.getEtiquetaRuta());
		System.out.print("   ip=" + r.getDireccionIP().toString());
		System.out.print("   mask=\\" + r.getLongitudPrefijoRed());
		System.out.print("   next=" + r.getDireccionProximoSalto().toString());
		System.out.println("   metrica=" + r.getMetrica());
		
	}
	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		System.out.print("Test: mensaje recibido()");
		System.out.print("  sesion: id=" + sesion.getId());
		System.out.print("  lectura: id=" + lectura.getId() );
		
		if (lectura.getMensaje() instanceof MensajeRIPPeticion) {
			MensajeRIPPeticion m = (MensajeRIPPeticion)lectura.getMensaje();
			System.out.print("  tipo: peticion" );
			System.out.print("  numRutas=" + m.getRIPRutas().size());
			for (IMensajeRIPRuta r: m.getRIPRutas()) {
				printRuta(r);
			}
		}
		System.out.println("");
		
		Escritura escritura = new Escritura(lectura.getMensaje());
		escritura.setDireccionDestino(lectura.getDireccionOrigen());
		sesion.escribir(escritura);

		return true;
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		System.out.println("Excepci�n capturada");
		System.out.println("   lectura: id=" + lectura.getId());
		e.printStackTrace();		
	}

}

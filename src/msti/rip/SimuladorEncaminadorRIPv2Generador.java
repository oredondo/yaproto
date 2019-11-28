/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.savarese.rocksaw.net.RawSocket;

import msti.io.Escritura;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.FiltroNotificador;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.io.SesionDatagrama;
import msti.rip.mensaje.IMensajeRIP.Tipo;
import msti.rip.mensaje.MensajeRIPPeticion;
import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.rip.mensaje.MensajeRIPRuta;

public class SimuladorEncaminadorRIPv2Generador implements Runnable, ISesionCreadaListener,ILecturaListener {
	
	private SesionDatagrama sesion;
	private Object semaforoSesion = new Object();
	
	public SimuladorEncaminadorRIPv2Generador() {
		System.out.println("Generador RIP instanciado");
	}

	@Override
	public synchronized void sesionCreada(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("Sesión creada: id=" + sesion.getId());
		((FiltroNotificador)sesion.getAceptador().getCadenaFiltros().getLast()).addLecturaListener(this, sesion.getId());
		System.out.println("   anadido ILecturaListener");

		synchronized (semaforoSesion) {
			System.out.println("En monitor sesionCreada: notificando...");
			this.sesion = (SesionDatagrama) sesion;
			semaforoSesion.notifyAll();
		}
	}

	@Override
	public void sesionInactiva(Sesion sesion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		// TODO Auto-generated method stub
		
	}

/*	private void printRuta(IMensajeRIPRuta r) {
		System.out.println(" --- Ruta ----");
		System.out.println("   afi=" + r.getIdFamiliaDirecciones());
		System.out.println("   etiq=" + r.getEtiquetaRuta());
		System.out.println("   ip=" + r.getDireccionIP().toString());
		System.out.println("   mask=\\" + r.getLongitudPrefijoRed());
		System.out.println("   next=" + r.getDireccionProximoSalto().toString());
		System.out.println("   metrica=" + r.getMetrica());
		
	}
*/
@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		System.out.println("Mensaje recibido");
		System.out.println("  sesion: id=" + sesion.getId());
		System.out.println("  lectura: id=" + lectura.getId());
		
		if (lectura.getMensaje() instanceof MensajeRIPPeticion) {
			MensajeRIPPeticion m = (MensajeRIPPeticion)lectura.getMensaje();
			System.out.print("  mensaje: peticion " );
			System.out.println(" numRutas=" + m.getRIPRutas().size());
/*			int contador=1;
			for (IMensajeRIPRuta r: m.getRIPRutas()) {
				System.out.println("  Ruta " + contador);
				printRuta(r);
				contador++;
			}
*/
			// Devuelve la petición como respuesta
			MensajeRIPRespuesta.Builder r = MensajeRIPRespuesta.Builder.crear();
			r.setTipo(Tipo.RIPRespuesta);
			r.setVersion((byte) 2);
			r.setRIPRutas(m.getRIPRutas());

			Escritura escritura = new Escritura(r.build());
			escritura.setDireccionDestino(lectura.getDireccionOrigen());
			sesion.escribir(escritura);
		}
		else if (lectura.getMensaje() instanceof MensajeRIPRespuesta) {
			MensajeRIPRespuesta m = (MensajeRIPRespuesta)lectura.getMensaje();
			System.out.print("  mensaje: respuesta " );
			System.out.println(" numRutas=" + m.getRIPRutas().size());
		}
				
		return true;
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		System.out.println("Excepci�n capturada");
		System.out.println("   lectura: id=" + lectura.getId());
		e.printStackTrace();		
	}

	@Override
	public void run() {
		MensajeRIPRuta.Builder r;
		MensajeRIPRespuesta.Builder m;
		
		// Espera a tener sesión
		synchronized (semaforoSesion) {
			while (sesion == null)
				try {
					System.out.println("En monitor run: esperando...");
					semaforoSesion.wait();
				} catch (InterruptedException e) {
					System.out.println("En monitor run: interrumpido.");
				}
		}
		
		// Envía mensajes de prueba
		m = MensajeRIPRespuesta.Builder.crear();
		m.setTipo(Tipo.RIPRespuesta);
		m.setVersion((byte) 2);
		// Ruta 1
		try {
			r = MensajeRIPRuta.Builder.crear();
			r.setIdFamiliaDirecciones((short) RawSocket.PF_INET);
			r.setEtiquetaRuta((short) 0);
			r.setDireccionIP(InetAddress.getByName("138.100.49.0"));
			r.setLongitudPrefijoRed(24);
			r.setDireccionProximoSalto(InetAddress.getByName("192.168.1.149"));
			r.setMetrica(4);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("error al generarl inetaddress");
		}

		m.addRIPRuta(r.build());
		// Ruta 2
		try {
			r = MensajeRIPRuta.Builder.crear();
			r.setIdFamiliaDirecciones((short) RawSocket.PF_INET);
			r.setEtiquetaRuta((short) 0);
			r.setDireccionIP(InetAddress.getByName("138.100.50.0"));
			r.setLongitudPrefijoRed(24);
			r.setDireccionProximoSalto(InetAddress.getByName("192.168.1.150"));
			r.setMetrica(5);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("error al generarl inetaddress");
		}

		m.addRIPRuta(r.build());
		// Ruta 3
		try {
			r = MensajeRIPRuta.Builder.crear();
			r.setIdFamiliaDirecciones((short) RawSocket.PF_INET);
			r.setEtiquetaRuta((short) 0);
			r.setDireccionIP(InetAddress.getByName("138.100.51.128"));
			r.setLongitudPrefijoRed(25);
			r.setDireccionProximoSalto(InetAddress.getByName("192.168.1.151"));
			r.setMetrica(6);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("error al generarl inetaddress");
		}

		m.addRIPRuta(r.build());

		try {
			Escritura escritura = new Escritura(m.build());
			escritura.setDireccionDestino(new InetSocketAddress(InetAddress.getByName("224.0.0.9"),520));
			sesion.escribir(escritura);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("error al generarl inetaddress");
		}
	}

	/* Principal */
/*	public static void main(String[] args) {
		SimuladorEncaminadorRIPv2Generador generador = new SimuladorEncaminadorRIPv2Generador();
		Thread hilo = new Thread(generador);
		hilo.start();
	}
*/}

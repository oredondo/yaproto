/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.savarese.rocksaw.net.RawSocket;

import msti.io.Escritura;
import msti.io.ILecturaListener;
import msti.io.ISesionCreadaListener;
import msti.io.FiltroNotificador;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.io.SesionDatagrama;
import msti.io.SesionRawSocket;
import msti.ospfv2.mensaje.*;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS;
import msti.util.Inet4Address;

public class SimuladorEncaminadorOSPFv2Generador implements Runnable, ISesionCreadaListener,ILecturaListener {
	
	private SesionRawSocket sesion;
	private Object semaforoSesion = new Object();
	
	public SimuladorEncaminadorOSPFv2Generador() {
		System.out.println("Generador OSPFv2 instanciado");
	}

	@Override
	public synchronized void sesionCreada(Sesion sesion) {
		// TODO Auto-generated method stub
		System.out.println("Sesión creada: id=" + sesion.getId());
		((FiltroNotificador)sesion.getAceptador().getCadenaFiltros().getLast()).addLecturaListener(this, sesion.getId());
		System.out.println("   añadido ILecturaListener");

		synchronized (semaforoSesion) {
			System.out.println("En monitor sesionCreada: notificando...");
			this.sesion = (SesionRawSocket) sesion;
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
		
		if (lectura.getMensaje() instanceof MensajeOSPFv2Hello) {
			MensajeOSPFv2Hello m = (MensajeOSPFv2Hello)lectura.getMensaje();
			System.out.print("Tipo " +m.getTipo());
			System.out.print("Version: " +m.getVersion());
			System.out.print("PacketLength: " + m.getPacketLength());
			System.out.print("RouterID: " +m.getRouterID());
			System.out.print("AreaID: " +m.getAreaID());
			System.out.print("Checksum: " +m.getChecksum());
			System.out.print("Autype: " +m.getAutype());
			System.out.print("Autehntication: " +m.getAuthentication());
			
			
			System.out.println(" Networkmask: " + m.getNetworkMask());
			System.out.println(" HelloInterval: " + m.getHelloInterval());
			System.out.println(" Options: " + m.getOptions());
			System.out.println(" RtrPri: " + m.getRtrPri());
			System.out.println(" RouterDeadInterval: " + m.getRouterDeadInterval());
			System.out.println(" DesignatedRouter: " + m.getDesignatedRouter());
			System.out.println(" BackupDesignatedRouter: " + m.getBackupDesignatedRouter());
			System.out.println(" Neighbors size= " + m.getNeighbors().size());
			
			int contador=1;
			for (Integer neighbor: m.getNeighbors()) {
				System.out.println("  Neighbor " + contador + ": " + neighbor);
				contador++;
			}

		}
		else if (lectura.getMensaje() instanceof MensajeOSPFv2DatabaseDescription) {
			MensajeOSPFv2DatabaseDescription m = (MensajeOSPFv2DatabaseDescription)lectura.getMensaje();
			System.out.print("Tipo " +m.getTipo());
			System.out.print("Version: " +m.getVersion());
			System.out.print("PacketLength: " + m.getPacketLength());
			System.out.print("RouterID: " +m.getRouterID());
			System.out.print("AreaID: " +m.getAreaID());
			System.out.print("Checksum: " +m.getChecksum());
			System.out.print("Autype: " +m.getAutype());
			System.out.print("Autehntication: " +m.getAuthentication());
			
			System.out.println(" Options:" + m.getOptions());
			System.out.println("IMMS: " +m.getIMMS());
			System.out.println("DDSequenceNumber: " +m.getDDSequenceNumber());
			System.out.println("LSAHeaders size= " + m.getLSAHeaders().size());
			
			int contador=1;
			for (IMensajeOSPFv2LinkStateAdvertisementHeader lsaH: m.getLSAHeaders()) {
				System.out.println("  LSAHeader  " + contador + ": " + lsaH.toString());
				contador++;
			}
		}
			
		else if (lectura.getMensaje() instanceof MensajeOSPFv2LinkStateRequest) {
			MensajeOSPFv2LinkStateRequest m = (MensajeOSPFv2LinkStateRequest)lectura.getMensaje();
			System.out.print("Tipo " +m.getTipo());
			System.out.print("Version: " +m.getVersion());
			System.out.print("PacketLength: " + m.getPacketLength());
			System.out.print("RouterID: " +m.getRouterID());
			System.out.print("AreaID: " +m.getAreaID());
			System.out.print("Checksum: " +m.getChecksum());
			System.out.print("Autype: " +m.getAutype());
			System.out.print("Autehntication: " +m.getAuthentication());

			System.out.println("LSAReferences size= " + m.getLSTypes().size());
			
			int contador=1;
			Iterator<Integer> iterLSTypes =  m.getLSTypes().listIterator();
			Iterator<Integer> iterLSIDss = m.getLSIDs().listIterator();
			Iterator<Integer> iterAdvertisingRouters = m.getAdvertisingRouters().listIterator();		
		    while (iterLSTypes.hasNext()) {
		    	System.out.println("  LSAReference  " + contador + ": ");
		    	System.out.println("  LSType " + contador + ": " + iterLSTypes.next());
		    	System.out.println("  LinkStateID " + contador + ": " + iterLSIDss.next());
		    	System.out.println("  AdvertisingRouter " + contador + ": " + iterAdvertisingRouters.next());
				contador++;

		    }

		}
		
		else if (lectura.getMensaje() instanceof MensajeOSPFv2LinkStateUpdate) {
			MensajeOSPFv2LinkStateUpdate m = (MensajeOSPFv2LinkStateUpdate)lectura.getMensaje();
			System.out.print("Tipo " +m.getTipo());
			System.out.print("Version: " +m.getVersion());
			System.out.print("PacketLength: " + m.getPacketLength());
			System.out.print("RouterID: " +m.getRouterID());
			System.out.print("AreaID: " +m.getAreaID());
			System.out.print("Checksum: " +m.getChecksum());
			System.out.print("Autype: " +m.getAutype());
			System.out.print("Autehntication: " +m.getAuthentication());

			System.out.print("Advertisements: " +m.getAdvertisements());
			
			int contador=1;
			for (IMensajeOSPFv2LSA lsa: m.getLSAs()) {
				System.out.println("  LSA  " + contador + ": " + lsa.toString());
				contador++;
			}
			
		}
		
		else if (lectura.getMensaje() instanceof MensajeOSPFv2LinkStateAcknowledgment) {
			MensajeOSPFv2LinkStateAcknowledgment m = (MensajeOSPFv2LinkStateAcknowledgment)lectura.getMensaje();
			System.out.print("Tipo " +m.getTipo());
			System.out.print("Version: " +m.getVersion());
			System.out.print("PacketLength: " + m.getPacketLength());
			System.out.print("RouterID: " +m.getRouterID());
			System.out.print("AreaID: " +m.getAreaID());
			System.out.print("Checksum: " +m.getChecksum());
			System.out.print("Autype: " +m.getAutype());
			System.out.print("Autehntication: " +m.getAuthentication());
			
			System.out.println("LSAHeaders size= " + m.getLSAHeaders().size());
			
			int contador=1;
			for (IMensajeOSPFv2LinkStateAdvertisementHeader lsaH: m.getLSAHeaders()) {
				System.out.println("  LSAHeader  " + contador + ": " + lsaH.toString());
				contador++;
			}
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
		Escritura escritura;
		
		
		// Envía mensajes de prueba
		//MensajeHello
		MensajeOSPFv2Hello.Builder h;
		h = MensajeOSPFv2Hello.Builder.crear();
		
		try {
			h.setRouterID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		h.setAreaID(1);
		h.setAutype((short) 0);
		h.setAuthentication(1234);
		
		h.setNetworkMask((int) 2147483520);
		h.setHelloInterval((short) 5);
		h.setOptions((byte) 0);
		h.setRtrPri((byte) 1);
		h.setRouterDeadInterval((int) 80);
		h.setDesignatedRouter((int) 0);
		h.setBackupDesignatedRouter((int) 0);
		List<Integer> neighbors=new ArrayList<Integer>();
		neighbors.add(12345678);
		neighbors.add(00000001);
		neighbors.add(1991);
		h.setNeighbors(neighbors);
		

		escritura = new Escritura(h.build());
		escritura.setDireccionDestino(new InetSocketAddress("192.168.1.2", 0)); //direccion AllSPFRouters
		sesion.escribir(escritura);
		
		
		
		
		//Mensaje DatabaseDescription
		MensajeOSPFv2DatabaseDescription.Builder d;
		d = MensajeOSPFv2DatabaseDescription.Builder.crear();
		
		try {
			d.setRouterID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		d.setAreaID(1);
		d.setAutype((short) 0);
		d.setAuthentication(1234);
		

		d.setOptions((byte) 0);
		d.setIMMS((byte) 7);
		d.setDDSequenceNumber(123);
		List<IMensajeOSPFv2LinkStateAdvertisementHeader> lSAHeaders=new ArrayList<IMensajeOSPFv2LinkStateAdvertisementHeader>();
		MensajeOSPFv2LinkStateAdvertisementHeader.Builder mensajeLSAHeader = MensajeOSPFv2LinkStateAdvertisementHeader.Builder.crear();		
		mensajeLSAHeader.setLSAge((short)0);
		mensajeLSAHeader.setOptions((byte) 0);
		mensajeLSAHeader.setLSType(TipoLS.RouterLinks);
		try {
			mensajeLSAHeader.setLinkStateID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
			mensajeLSAHeader.setAdvertisingRouter(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
		} catch (UnknownHostException e) {
		}
		mensajeLSAHeader.setLSSequenceNumber(123);
		MensajeOSPFv2LinkStateAdvertisementHeader lsaHeader = mensajeLSAHeader.build();
		lSAHeaders.add(lsaHeader);
		d.setLSAHeaders(lSAHeaders);
		

		escritura = new Escritura(d.build());
		escritura.setDireccionDestino(new InetSocketAddress("192.168.1.2", 0)); //direccion AllSPFRouters
		sesion.escribir(escritura);
		
		
		
		
		//Mensaje LinkStateRequest
		MensajeOSPFv2LinkStateRequest.Builder r;
		r = MensajeOSPFv2LinkStateRequest.Builder.crear();
		
		try {
			r.setRouterID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		r.setAreaID(1);
		r.setAutype((short) 0);
		r.setAuthentication(1234);
		
		List<Integer> lSTypes = new ArrayList<Integer>();
		List<Integer> lSIDs = new ArrayList<Integer>();
		List<Integer> advertisingRouters = new ArrayList<Integer>();
		lSTypes.add((int) TipoLS.RouterLinks.getCodigo());
		lSTypes.add((int) TipoLS.RouterLinks.getCodigo());	
		lSIDs.add(11);
		lSIDs.add(12);
		advertisingRouters.add(13);
		advertisingRouters.add(14);
				
		r.setLSTypes(lSTypes);
		r.setLSIDs(lSIDs);
		r.setAdvertisingRouters(advertisingRouters);
		
		escritura = new Escritura(r.build());
		escritura.setDireccionDestino(new InetSocketAddress("192.168.1.2", 0)); //direccion AllSPFRouters
		sesion.escribir(escritura);
		
		
		
		
		//Mensaje LinkStateUpdate
		MensajeOSPFv2LinkStateUpdate.Builder u;
		u = MensajeOSPFv2LinkStateUpdate.Builder.crear();
		
		try {
			u.setRouterID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
		} catch (UnknownHostException e) {
		}
		u.setAreaID(1);
		u.setAutype((short) 0);
		u.setAuthentication(1234);
		

		u.setAdvertisements(1);

		List<IMensajeOSPFv2LSA> lSAs=new ArrayList<IMensajeOSPFv2LSA>();
		MensajeOSPFv2LSARouterLinks.Builder mesajeLSA = MensajeOSPFv2LSARouterLinks.Builder.crear();
		mesajeLSA.setHeader(lsaHeader);
		mesajeLSA.setVEB((byte) 0);
		mesajeLSA.setNLinks((short) 1);
		List<IMensajeOSPFv2LSARouterLinksLinks> links = new ArrayList<IMensajeOSPFv2LSARouterLinksLinks>();
		MensajeOSPFv2LSARouterLinksLinks.Builder routerLink = MensajeOSPFv2LSARouterLinksLinks.Builder.crear();
		try {
			routerLink.setLinkID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
			routerLink.setLinkData(Inet4Address.toInt(InetAddress.getByName("255.255.255.0")));
		} catch (UnknownHostException e1) {
		}	
		routerLink.setType(IMensajeOSPFv2LSARouterLinksLinks.Type.ConnectionToAStubNetwork);
		routerLink.setNTOS((byte) 0);
		routerLink.setTOS0Metric((short) 1);
		List<Byte> toss = new ArrayList<Byte>();
		List<Short> metrics = new ArrayList<Short>();
		routerLink.setTOSs(toss);
		routerLink.setMetrics(metrics);
		
		links.add(routerLink.build());
		mesajeLSA.setRouterLinks(links);
		
		lSAs.add(mesajeLSA.build());
		u.setLSAs(lSAs);
		

		escritura = new Escritura(u.build());
		escritura.setDireccionDestino(new InetSocketAddress("192.168.1.2", 0)); //direccion AllSPFRouters
		sesion.escribir(escritura);
		
		
		
		
		
		//Mensaje LinkStateAcknowledgment
		MensajeOSPFv2LinkStateAcknowledgment.Builder a;
		a = MensajeOSPFv2LinkStateAcknowledgment.Builder.crear();
		
		try {
			a.setRouterID(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		a.setAreaID(1);
		a.setAutype((short) 0);
		a.setAuthentication(1234);
		a.setLSAHeaders(lSAHeaders);	

		escritura = new Escritura(a.build());
		escritura.setDireccionDestino(new InetSocketAddress("192.168.1.2", 0)); //direccion AllSPFRouters
		sesion.escribir(escritura);
		
		/*
		 
		 */
	}
	

}

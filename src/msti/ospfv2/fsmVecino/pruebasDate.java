package msti.ospfv2.fsmVecino;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import msti.fsm.FSMEstado;
import msti.fsm.FSMEstado.FSMIdEstado;
import msti.netlink.fsm.client.INetlinkOrden;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2InterfazLoopback;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FactoriaFSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2Hello;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateUpdate;

public class pruebasDate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*
		Calendar calendario = Calendar.getInstance();
		calendario = new GregorianCalendar();
		int nuevoDDSequenceNumber;
		nuevoDDSequenceNumber= calendario.get(Calendar.MILLISECOND) + 1000*calendario.get(Calendar.SECOND)
				+ 100000*calendario.get(Calendar.MINUTE) + 10000000*calendario.get(Calendar.HOUR_OF_DAY);
		
		System.out.println(calendario.get(Calendar.HOUR_OF_DAY) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND)+ ":" + calendario.get(Calendar.MILLISECOND));
		System.out.println("int: " + nuevoDDSequenceNumber);
		*/

		
		
		
		
		
		/*
		//Comprobacion de cómo saber en que estado estamos (Calculo DR y BDR)
		
		
		TablaRutas tablaRutas = null;
		INetlinkOrden tablaForwarding = null;
		
		FactoriaFSMMaquinaEstadosOSPFv2Interfaz factoria = new FactoriaFSMMaquinaEstadosOSPFv2Interfaz(tablaRutas,tablaForwarding);
		FSMMaquinaEstadosOSPFv2Interfaz maquinaEstados = factoria.getInstance();
		
		FSMEstado estadoActual = (FSMEstado) maquinaEstados.getEstadoActivo();
		
		System.out.println("estado actual ID: " + estadoActual.getId());
		
		FSMIdEstado idEstado =FSMIdEstadoOSPFv2Interfaz.LOOPBACK;
		FSMEstado estadoLoopback=  new FSMEstadoOSPFv2InterfazLoopback(idEstado);
		maquinaEstados.setEstado(estadoLoopback);
		
		estadoActual = (FSMEstado) maquinaEstados.getEstadoActivo();
		System.out.println("estado actual ID: " + estadoActual.getId());
		
		if(estadoActual.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.LOOPBACK)){
			System.out.println("Correcto");
		}
		

		*/
		
		
		
		/*
		
		//comprobacion de Inet4Address
		InetAddress dir = null;
		try {
			dir = InetAddress.getByName("224.0.0.6");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("error");
			
		}
		System.out.println("inetAddress: "+dir.getHostAddress());
		
		int direccionRed = msti.util.Inet4Address.toInt(dir);
		System.out.println("direccionRed: "+direccionRed);
		
		dir=msti.util.Inet4Address.fromInt(direccionRed);
		System.out.println("inetAddress: "+dir.getHostAddress());
		
		*/
				
		/*
		//comprobacion de InetAddress
		
		InetAddress dir = null;
		try {
			dir = InetAddress.getByName("224.0.0.8");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("error");
			
		}
		System.out.println("inetAddress: "+dir.getHostAddress());
		int result = 0;
		for (byte b: dir.getAddress())
		{
		    result = result << 8 | (b & 0xFF);
		}		
		
		int direccionRed = result;
		System.out.println("direccionRed: "+direccionRed);
		
		dir=msti.util.Inet4Address.fromInt(direccionRed);
		System.out.println("inetAddress: "+dir.getHostAddress());
		
		*/
		
		/*
		//pruebas bit a bit	
		byte opciones=5;
		
		if((opciones & 2)==2){
			System.out.println("bit E: si");
		}else{
			System.out.println("bit E: no");
		}
		
		if((opciones & 1)==1){
			System.out.println("bit T: si");
		}else{
			
		}
		*/
		
		
		
		
		
		/*
		//prueba lista, orden
		List<Integer> lSTypes = new ArrayList<Integer>();		
		List<Integer> lSIDs;	
		List<Integer> advertisingRouters;	
		 
		lSTypes.add(1);
		lSTypes.add(2);
		lSTypes.add(3);
		lSTypes.add(4);
		lSTypes.add(5);
		 
		System.out.println("Primer barrido");
		Iterator<Integer> iterLSTypes = lSTypes.listIterator();	
		while (iterLSTypes.hasNext()) {
			System.out.println(iterLSTypes.next());
	
		}
		
		lSTypes.remove(0);
		lSTypes.remove(2);
		lSTypes.add(101);
		lSTypes.add(102);
		lSTypes.add(103);
		
		System.out.println("Primer barrido");
		iterLSTypes = lSTypes.listIterator();	
		while (iterLSTypes.hasNext()) {
			System.out.println(iterLSTypes.next());
	
		}
		*/
		
		/*
		//int to byte
		int i=7;
		byte b= (byte)i;
		b = (byte) (b & 7);
		System.out.println(b);
		
		i=(int) b;
		System.out.println(i);
		*/
		
		
		/*
		//TipoLS to byte
		
		byte b=2;
		IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS t = IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks;
		if(b==t.getCodigo()){
			System.out.println("si");
		}
		*/
		
		
		
		
		/*
		//Bytes y shorts
		byte[] array = new byte[] { (byte)0x80, (byte)0xff};
		ByteBuffer buffer = ByteBuffer.wrap(array);
		short s =buffer.getShort();
		System.out.println("s:" + Integer.toBinaryString(0xFFFF & s));
		
		byte b1 =  (byte)((s >> 8) & 0xff);
		byte b2 = (byte)(s & 0xff);
		
		String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
		System.out.println(s1);
		
		String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
		System.out.println(s2);
		*/
		
		/*
		//terminar bucle
		
		List<String> lista = new ArrayList<String>();
		lista.add("a");
		lista.add("b");
		lista.add("c");
		lista.add("d");
		lista.add("e");
		lista.add("f");

		boolean error = false;
		int caso=1;
		switch(caso) {
			case 1:
				for(String s: lista){
					System.out.println(s);
					if(s.equals("d"))
						break;
					System.out.println("fin" + s);
				}
			System.out.println("fin case1");
			break;
		}
		*/
		
		/*
		//unsigned int
		int i = 0x80000000;
		System.out.println(i);
		i = 0x80000001;
		System.out.println(i);
		i = 0x7fffffff;
		System.out.println(i);
		i = 0x8fffffff;
		System.out.println(i);
		i = 0xffffffff;
		System.out.println(i);

		*/
		
		/*
		//Restar fechas
		Calendar cinicio = Calendar.getInstance();
		Date dinicio = cinicio.getTime();
		
		System.out.println(dinicio.toString());
		
		
		try {
			Thread.sleep(5001);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Calendar cfinal = Calendar.getInstance();
		Date dfinal = cfinal.getTime();
		System.out.println(dfinal.toString());
		
		
		long milis1 = cinicio.getTimeInMillis();
		long milis2 = cfinal.getTimeInMillis();
		
		System.out.println("la diferencia son: " + (short) (3+(milis2-milis1)/1000));
		
		*/
		
		
		
		/*
		//mensajes iguales
		MensajeOSPFv2Hello.Builder mensajeHello = MensajeOSPFv2Hello.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeHello.setPacketLength(); 					//lo introduce el Builder
		mensajeHello.setRouterID(123);
		mensajeHello.setAreaID(1234);
		//mensajeHello.setChecksum(); 						//lo introduce el Builder
		mensajeHello.setAutype((short) 0);
		mensajeHello.setAuthentication(0);

		//Campos Hello
		mensajeHello.setNetworkMask(0);
		mensajeHello.setHelloInterval((short) 0);
		mensajeHello.setOptions((byte) 0);
		mensajeHello.setRtrPri((byte) 5);
		mensajeHello.setRouterDeadInterval(40);
		mensajeHello.setDesignatedRouter(0);
		mensajeHello.setBackupDesignatedRouter(0);
		
		List<Integer> neighbors= new ArrayList<Integer>();
		neighbors.add(1);
		neighbors.add(2);
		neighbors.add(3);
		mensajeHello.setNeighbors(neighbors);
		
		MensajeOSPFv2Hello mensaje1= mensajeHello.build();
		
		
		
		MensajeOSPFv2Hello.Builder mensajeHello2 = MensajeOSPFv2Hello.Builder.crear();

		//header
		//tipo y version en el constructor
		
		//mensajeHello.setPacketLength(); 					//lo introduce el Builder
		mensajeHello2.setRouterID(123);
		mensajeHello2.setAreaID(1234);
		//mensajeHello.setChecksum(); 						//lo introduce el Builder
		mensajeHello2.setAutype((short) 0);
		mensajeHello2.setAuthentication(0);

		//Campos Hello
		mensajeHello2.setNetworkMask(0);
		mensajeHello2.setHelloInterval((short) 0);
		mensajeHello2.setOptions((byte) 0);
		mensajeHello2.setRtrPri((byte) 5);
		mensajeHello2.setRouterDeadInterval(40);
		mensajeHello2.setDesignatedRouter(0);
		mensajeHello2.setBackupDesignatedRouter(0);
		
		List<Integer> neighbors2= new ArrayList<Integer>();
		neighbors2.add(1);
		neighbors2.add(2);
		neighbors2.add(3);
		mensajeHello2.setNeighbors(neighbors2);
		
		MensajeOSPFv2Hello mensaje2= mensajeHello2.build();
		
		System.out.println(mensaje1.toString());
		System.out.println(mensaje2.toString());
		
		
		if(mensaje2.toString().equals(mensaje1.toString())){
			System.out.println("si");
		}else{
			System.out.println("no");
		}
		
		System.out.println("checksum " +mensaje1.getChecksum());
		
		*/
		
		
		
		//byte to long
		
		long l = (long) 0;
		byte b = 2;
		int i = 1234;
		System.out.println("l= "+ l);
		System.out.println("b= "+ b);
		System.out.println("i= "+ i);
		System.out.println("");
		
		l=b;
		System.out.println("l= "+ l);
		l <<=32;
		System.out.println("l= "+ l);
		l+=i;
		System.out.println("l= "+ l);
		
		
		Long l2=l;
		System.out.println("l2= "+ l2);
		l2 >>=32;
		System.out.println("l2= "+ l2);
		l2 = l2 & 0xff;
		System.out.println("l2= "+ l2);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
}

package msti.ospfv2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.savarese.rocksaw.net.RawSocket;

import msti.fsm.FSMContexto;
import msti.io.AceptadorRawSocket;
import msti.io.AceptadorRawSocketNetlink;
import msti.io.Filtro;
import msti.io.FiltroCodec;
import msti.io.FiltroLog;
import msti.io.FiltroLog.NivelLog;
import msti.io.mensaje.IMensajeCodecFactoria;
import msti.io.mensaje.IMensajeCodificador;
import msti.io.mensaje.IMensajeDecodificador;
import msti.io.FiltroNotificador;
import msti.netlink.fsm.client.FSMMaquinaEstadosNetlinkCliente;
import msti.netlink.mensaje.IMensajeNetlink;
import msti.netlink.mensaje.MensajeNetlinkCodec;
import msti.ospfv2.fsmInterfaz.FactoriaFSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.mensaje.MensajeOSPFv2Codec;
import msti.util.Inet4Address;
import msti.util.RawSocketNetlink;
import msti.util.RawSocketNetlink.NetlinkAddress;
import static msti.util.RawSocketNetlink.PF_NETLINK;

public class ProtocoloOSPFv2 implements Runnable {
	String[] localIps;
	AceptadorRawSocketNetlink aceptadorNetlinkCliente;
	AceptadorRawSocketNetlink aceptadorNetlinkObservador;
	TablaRutas tablaRutas;
	AceptadorRawSocket[] aceptadoresOSPFV2;
	ConfiguracionOSPFv2[] configuracionesOSPFv2;
	FactoriaFSMMaquinaEstadosOSPFv2Interfaz[] factoriaFSMMaquinaEstadosOSPFv2Interfazes;
	/**
	 * @param args
	 * @throws IOException
	 */
	public ProtocoloOSPFv2(String[] localIps1) throws IOException {
		localIps = localIps1;
		AceptadorRawSocket aceptadorOSPFv2;
		FiltroCodec filtroCodec;
		FiltroLog filtroLog;
		FiltroNotificador filtroNotificador;
		aceptadoresOSPFV2 = new AceptadorRawSocket[localIps.length];
		// Instancia una tabla de rutas
		TablaRutas tablaRutas = new TablaRutas();


		/**
		 * Canal comandos
		 */
		aceptadorNetlinkCliente = new AceptadorRawSocketNetlink();

		aceptadorNetlinkCliente.crear(PF_NETLINK, IMensajeNetlink.NetlinkProtocol.NETLINK_ROUTE.getValue());


		// Codec de Netlink
		filtroCodec = new FiltroCodec("Codec Netlink",
									new IMensajeCodecFactoria() {
										MensajeNetlinkCodec netlinkCodec = null;

										@Override
										public IMensajeDecodificador getDecodificador() {
											if (netlinkCodec == null)
												netlinkCodec = new MensajeNetlinkCodec();
											return netlinkCodec;
										}

										@Override
										public IMensajeCodificador getCodificador() {
											if (netlinkCodec == null)
												netlinkCodec = new MensajeNetlinkCodec();
											return netlinkCodec;
										}
		});
		aceptadorNetlinkCliente.getCadenaFiltros().addFirst(filtroCodec.getNombre(), filtroCodec);
		// Log de mensajes recibidos
		filtroLog = new FiltroLog("Log");
		filtroLog.setNivelLogMinimo(NivelLog.TRACE);
		aceptadorNetlinkCliente.getCadenaFiltros().addLast(filtroLog.getNombre(), filtroLog);
		// Patrón observable para el canal
		filtroNotificador = new FiltroNotificador("Notificador");
		aceptadorNetlinkCliente.getCadenaFiltros().addLast(filtroNotificador.getNombre(), filtroNotificador);

		// Instancia una máq. estados dedicada para enviar mandatos a la tabla forwarding
		FSMContexto contexto = new FSMContexto();
		FSMMaquinaEstadosNetlinkCliente meNetlinkCliente = new FSMMaquinaEstadosNetlinkCliente(contexto);
		meNetlinkCliente.setHilo(false);
		meNetlinkCliente.init(contexto);
		filtroNotificador.addSesionCreadaListener(meNetlinkCliente); //suscribe (luego la máquina se suscribe a lectura)

		/**
		 * Canal suscripción
		 */
		aceptadorNetlinkObservador = new AceptadorRawSocketNetlink();
		aceptadorNetlinkObservador.crear(PF_NETLINK, IMensajeNetlink.NetlinkProtocol.NETLINK_ROUTE.getValue());

		// Codec de Netlink
		filtroCodec = new FiltroCodec("Codec Netlink",
									new IMensajeCodecFactoria() {
										MensajeNetlinkCodec netlinkCodec = null;

										@Override
										public IMensajeDecodificador getDecodificador() {
											if (netlinkCodec == null)
												netlinkCodec = new MensajeNetlinkCodec();
											return netlinkCodec;
										}

										@Override
										public IMensajeCodificador getCodificador() {
											if (netlinkCodec == null)
												netlinkCodec = new MensajeNetlinkCodec();
											return netlinkCodec;
										}
		});
		aceptadorNetlinkObservador.getCadenaFiltros().addFirst(filtroCodec.getNombre(), filtroCodec);
		// Log de mensajes recibidos (ya decodificados)
		filtroLog = new FiltroLog("Log");
		filtroLog.setNivelLogMinimo(NivelLog.TRACE);
		aceptadorNetlinkObservador.getCadenaFiltros().addLast(filtroLog.getNombre(), filtroLog);

		// Patr�n observable para el canal (permite observadores ISesionCreadaListener, ILecturaListener, IEscrituraListener)
		filtroNotificador = new FiltroNotificador("Notificador");
		aceptadorNetlinkObservador.getCadenaFiltros().addLast(filtroNotificador.getNombre(), filtroNotificador);

		// notificador
		TestNetlink test2 = new TestNetlink();
		filtroNotificador.addSesionCreadaListener(test2); //suscribe



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Construye un canal rawSocket, con formato de pdu OSPF.
		// Aceptador modo RawSocket

		for (int x=0; x < localIps.length; x++) {
			aceptadorOSPFv2 = new AceptadorRawSocket(); // Genera un lector y un escritor
			//aceptadorOSPFv2.crear(RawSocket.AF_PACKET, RawSocket.IPPROTO_RAW);
			//aceptadorOSPFv2.crear(RawSocket.PF_INET, 255);
			aceptadorOSPFv2.crear(RawSocket.PF_INET, 89);
			//aceptadorNetlinkCliente.crear(PF_NETLINK, IMensajeNetlink.NetlinkProtocol.NETLINK_ROUTE.getValue());

			// Codec de pdu OSPF
			filtroCodec = new FiltroCodec("Codec OSPFv2",
										new IMensajeCodecFactoria() {
											MensajeOSPFv2Codec ospfv2Codec = null;

											@Override
											public IMensajeDecodificador getDecodificador() {
												if (ospfv2Codec == null)
													ospfv2Codec = new MensajeOSPFv2Codec();
												return ospfv2Codec;
											}

											@Override
											public IMensajeCodificador getCodificador() {
												if (ospfv2Codec == null)
													ospfv2Codec = new MensajeOSPFv2Codec();
												return ospfv2Codec;
											}
			});
			aceptadorOSPFv2.getCadenaFiltros().addFirst(filtroCodec.getNombre(), filtroCodec);
			// Log de mensajes recibidos (ya decodificados)
			filtroLog = new FiltroLog("Log_" + x);
			filtroLog.setNivelLogMinimo(NivelLog.TRACE);
			aceptadorOSPFv2.getCadenaFiltros().addLast(filtroLog.getNombre(), filtroLog);

			// Patron observable para el canal
			filtroNotificador = new FiltroNotificador("Notificador_" + x);
			aceptadorOSPFv2.getCadenaFiltros().addLast(filtroNotificador.getNombre(), filtroNotificador);

			// Suscribe la m�quina de estados a los eventos del canal  de mensajes
			ConfiguracionOSPFv2 confOSPFv2= ConfiguracionOSPFv2.getInstance();
			confOSPFv2.allSPFRouters = InetAddress.getByName("224.0.0.5");
			confOSPFv2.allDRouters = InetAddress.getByName("224.0.0.6");
			confOSPFv2.routerID = Inet4Address.toInt(InetAddress.getByName(localIps[x]));
			confOSPFv2.tablaRutas = tablaRutas;

			FactoriaFSMMaquinaEstadosOSPFv2Interfaz factoriaOSPFv2Interfaz = new FactoriaFSMMaquinaEstadosOSPFv2Interfaz(tablaRutas, meNetlinkCliente, confOSPFv2);
			//suscribe para sesi�n creada
			filtroNotificador.addSesionCreadaListener(factoriaOSPFv2Interfaz);

			aceptadoresOSPFV2[x] = aceptadorOSPFv2;
			
		}

	}

	public void run() {
		// Establece un manejador para apagados abruptos
		AceptadorShutdown aceptadorShutdown = AceptadorShutdown.crear();
		aceptadorShutdown.setAceptador(aceptadoresOSPFV2[0]);
		Runtime.getRuntime().addShutdownHook(aceptadorShutdown);



		// bind a un puerto
		try {
			/** Bind del canal Netlink Cliente */
			RawSocketNetlink tmp = new RawSocketNetlink();
			NetlinkAddress netlinkAddress = tmp.new NetlinkAddress();
			netlinkAddress.pid = 0;
			netlinkAddress.grupos = RawSocketNetlink.NetlinkMulticastGroup.RTMGRP_IPV4_ROUTE.getValue();
			aceptadorNetlinkCliente.bind(netlinkAddress);

			Thread hiloNetlinkCliente = new Thread(aceptadorNetlinkCliente);
			hiloNetlinkCliente.start();


			/** Bind del canal Netlink Observador */
			RawSocketNetlink tmp2 = new RawSocketNetlink();
			netlinkAddress = tmp2.new NetlinkAddress();

			// al hacer el bind, si se rellena el pid, bind falla indicando que la direcci�n ya est� asignada. Dejar en 0 y que elija el sistema.
			netlinkAddress.pid = 0; // (int)RawSocketNetlink.getProcessId();
			netlinkAddress.grupos = 0;
			aceptadorNetlinkObservador.bind(netlinkAddress);

			Thread hiloNetlinkObservador = new Thread(aceptadorNetlinkObservador);
			hiloNetlinkObservador.start();

			for (int x=0; x < localIps.length; x++) {
			/** Bind del canal OSPF */
					InetAddress inetA = InetAddress.getByName(localIps[x]);
					System.out.println("Bind del canal OSPF iniciado, interfaz "+ inetA.getHostAddress());
					//Para a�adir m�s interfaces, hacer otro aceptadorOSPFv2 y otro bind, todo igual que este menos el bind (con la nueva ip)
					aceptadoresOSPFV2[x].bind(InetAddress.getByName(localIps[x]));
					///AllSPFRouters
					((AceptadorRawSocket)aceptadoresOSPFV2[x]).unirGrupo(InetAddress.getByName("224.0.0.5"));
					///AlldRouters
					((AceptadorRawSocket)aceptadoresOSPFV2[x]).unirGrupo(InetAddress.getByName("224.0.0.6"));
					// Pone en marcha ospf (usa para ello el hilo actual)
					System.out.println("Bind del canal OSPF terminado unido a  " + aceptadoresOSPFV2[x].getGrupos().size() + " grupos");
					aceptadoresOSPFV2[x].start();
		  }


		} catch (SocketException e) {
			System.out.println("En bind() del aceptador(): m�todo .bind del socket");
			e.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			System.out.println("En bind() del aceptador: getByName() nombre host desconocido");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("En bind() del aceptador(): unirGrupo() suelta IOException");
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// 172.24.1.2,172.24.3.2
		ProtocoloOSPFv2 ospfv2;
		try {

			String[] localIps = args[0].split(",", 0);
			String[] otrasIps = args[1].split(",", 0);
			ospfv2 = new ProtocoloOSPFv2(localIps);
			ospfv2.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static class AceptadorShutdown extends Thread
	{
		private AceptadorRawSocket aceptador;

		public static AceptadorShutdown crear() {
			return new AceptadorShutdown();
		}

		public void setAceptador(AceptadorRawSocket aceptador) {
			this.aceptador = aceptador;
		}

		public void run() {
			System.out.println("En aceptador shutdown...");

			// Destruye los filtros
			for (Filtro filtro: aceptador.getCadenaFiltros())
				filtro.destroy();

			// Abandona los grupos multicast a los que se ha unido
			if (aceptador.isMulticast()) {
				AceptadorRawSocket _aceptador = (AceptadorRawSocket)aceptador;

				for (InetAddress grupo: _aceptador.getGrupos()){
					/*try {
						//llamar a abandonar grupo con todos los grupos
						//((MulticastSocket)_aceptador.getSocket()).leaveGroup(grupo);
						//TODO: leaveGroup sin implementar
					} catch (IOException e) {
						e.printStackTrace();
					}*/
				}
			}
	    }

	}

}

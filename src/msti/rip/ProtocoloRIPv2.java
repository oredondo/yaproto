/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import msti.fsm.FSMContexto;
import msti.io.AceptadorDatagrama;
import msti.io.AceptadorDatagramaMulticast;
import msti.io.AceptadorRawSocketNetlink;
import msti.io.Filtro;
import msti.io.MqttCliente;
import msti.io.FiltroCodec;
import msti.io.FiltroLog;
import msti.io.FiltroMQTT;
import msti.io.FiltroLog.NivelLog;
import msti.io.FiltroMQTT.NivelLogMQTT;
import msti.io.mensaje.IMensajeCodecFactoria;
import msti.io.mensaje.IMensajeCodificador;
import msti.io.mensaje.IMensajeDecodificador;
import msti.io.FiltroNotificador;
import msti.netlink.fsm.client.FSMMaquinaEstadosNetlinkCliente;
import msti.netlink.mensaje.IMensajeNetlink;
import msti.netlink.mensaje.MensajeNetlinkCodec;
import msti.rip.fsm.FactoriaFSMMaquinaEstadosRIP;
import msti.rip.mensaje.MensajeRIPCodec;
import msti.util.RawSocketNetlink;
import msti.util.RawSocketNetlink.NetlinkAddress;
import org.eclipse.paho.client.mqttv3.MqttClient;

//////////////////////////////////
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//////////////////////////////////

import static msti.util.RawSocketNetlink.PF_NETLINK;

public class ProtocoloRIPv2 implements Runnable {

	AceptadorRawSocketNetlink aceptadorNetlinkCliente;
	AceptadorRawSocketNetlink aceptadorNetlinkObservador;
	AceptadorDatagramaMulticast aceptadorRIP;
	TablaRutas tablaRutas;

	/**
	 * @param args
	 * @throws IOException
	 */
	public ProtocoloRIPv2(String port) throws IOException {
		FiltroCodec filtroCodec;
		FiltroLog filtroLog;
		FiltroMQTT filtroMqtt;
		FiltroNotificador filtroNotificador;

		// Instancia una tabla de rutas
		TablaRutas tablaRutas = new TablaRutas();


		/**
		 * Canal comandos
		 */
		aceptadorNetlinkCliente = new AceptadorRawSocketNetlink();

		aceptadorNetlinkCliente.crear(PF_NETLINK, IMensajeNetlink.NetlinkProtocol.NETLINK_ROUTE.getValue());
		MqttCliente mqtt;
		mqtt = new MqttCliente(port);
		MqttClient clientemqtt;
		clientemqtt = mqtt.getMqtt();

		// Codec de pdu RIP. TODO: Estos filtros se instancia uno, o varios por cada sesi�n?????
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
		filtroMqtt = new FiltroMQTT("LogRip", clientemqtt);
		filtroMqtt.setNivelLogMinimo(NivelLogMQTT.TRACE);
		aceptadorNetlinkCliente.getCadenaFiltros().addLast(filtroLog.getNombre(), filtroLog);
		aceptadorNetlinkCliente.getCadenaFiltros().addLast(filtroMqtt.getNombre(), filtroMqtt);
		// Patrón observable para el canal
		filtroNotificador = new FiltroNotificador("Notificador", clientemqtt);
		aceptadorNetlinkCliente.getCadenaFiltros().addLast(filtroNotificador.getNombre(), filtroNotificador);

		// Instancia una máq. estados dedicada para enviar mandatos a la tabla forwarding
		FSMContexto contexto = new FSMContexto();
		contexto.setMqttClient(clientemqtt);
		FSMMaquinaEstadosNetlinkCliente meNetlinkCliente = new FSMMaquinaEstadosNetlinkCliente(contexto);
		meNetlinkCliente.setHilo(false);
		meNetlinkCliente.init(contexto);
		filtroNotificador.addSesionCreadaListener(meNetlinkCliente); //suscribe (luego la máquina se suscribe a lectura)

		/**
		 * Canal suscripción
		 */
		aceptadorNetlinkObservador = new AceptadorRawSocketNetlink();
		aceptadorNetlinkObservador.crear(PF_NETLINK, IMensajeNetlink.NetlinkProtocol.NETLINK_ROUTE.getValue());

		// Codec de pdu RIP. TODO: Estos filtros se instancia uno, o varios por cada sesi�n?????
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
		// filtroMqtt = new FiltroMQTT("LogRip", port);
		// filtroMqtt.setNivelLogMinimo(NivelLogMQTT.TRACE);
		aceptadorNetlinkObservador.getCadenaFiltros().addLast(filtroLog.getNombre(), filtroLog);
		aceptadorNetlinkObservador.getCadenaFiltros().addLast(filtroMqtt.getNombre(), filtroMqtt);

		// Patr�n observable para el canal (permite observadores ISesionCreadaListener, ILecturaListener, IEscrituraListener)
		filtroNotificador = new FiltroNotificador("Notificador", clientemqtt);
		aceptadorNetlinkObservador.getCadenaFiltros().addLast(filtroNotificador.getNombre(), filtroNotificador);

		// Suscribe la m�quina de estados a los eventos del canal udp de mensajes
		// TODO: No es as�, en cada sesionCreada, deber�a instanciarse una m�quina de estados y suscribirla a los eventos del
		// notificador
		TestNetlink test2 = new TestNetlink();
		filtroNotificador.addSesionCreadaListener(test2); //suscribe


		// Instancia una m�quina de Estados
		// TODO: Aceptador debe instanciar un productor y una m�quina diferente para cada sesi�n nueva --aceptada--(y pasar a la m�quina la sesi�n para poder realizar la acci�n de salida)
		// La instancia el notificador. Lo que habr�a que hacer es construir una clase que construya las m�quina de estados
		// y suscribir esta m�quina al notificador para cada sesi�n (ilecturalistener).
		// esta clase suscribir� la maquina de estados creada a los eventos de la sesion asociada
//		maquinaEstados = new MaquinaEstados();

		// TODO

		// Construye un canal datagrama, con formato de pdu RIP.
		// Aceptador modo datagrama

///////	aceptador = new AceptadorDatagrama(); // Genera un lector y un escritor
		aceptadorRIP = new AceptadorDatagramaMulticast(); // Genera un lector y un escritor

		//TODO: generar una cadena de filtros que los a�ade y enlace.
		// Codec de pdu RIP. TODO: Estos filtros se instancia uno, o varios por cada sesi�n?????
		filtroCodec = new FiltroCodec("Codec RIP",
									new IMensajeCodecFactoria() {
										MensajeRIPCodec ripCodec = null;

										@Override
										public IMensajeDecodificador getDecodificador() {
											if (ripCodec == null)
												ripCodec = new MensajeRIPCodec();
											return ripCodec;
										}

										@Override
										public IMensajeCodificador getCodificador() {
											if (ripCodec == null)
												ripCodec = new MensajeRIPCodec();
											return ripCodec;
										}
		});
		aceptadorRIP.getCadenaFiltros().addFirst(filtroCodec.getNombre(), filtroCodec);
		// Log de mensajes recibidos (ya decodificados)
		filtroLog = new FiltroLog("Log");
		filtroLog.setNivelLogMinimo(NivelLog.TRACE);
		aceptadorRIP.getCadenaFiltros().addLast(filtroLog.getNombre(), filtroLog);
		aceptadorRIP.getCadenaFiltros().addLast(filtroMqtt.getNombre(), filtroMqtt);

		// Patr�n observable para el canal
		filtroNotificador = new FiltroNotificador("Notificador", clientemqtt);
		aceptadorRIP.getCadenaFiltros().addLast(filtroNotificador.getNombre(), filtroNotificador);

		// Suscribe la m�quina de estados a los eventos del canal udp de mensajes
		// TODO: No es as�, en cada sesionCreada, deber�a instanciarse una m�quina de estados y suscribirla a los eventos del
		// notificador

		// TODO: Esto debe estar en un método de alguna clase SesionCreadaListener
		FSMContexto contexto2 = new FSMContexto();
		contexto2.setMqttClient(clientemqtt);
		FactoriaFSMMaquinaEstadosRIP factoriaMeRIP = new FactoriaFSMMaquinaEstadosRIP(tablaRutas, meNetlinkCliente, contexto);
		filtroNotificador.addSesionCreadaListener(factoriaMeRIP); //suscribe para sesión creada (debería instanciar la máquina aquí

		// Instancia un filtro para crear máquinas por ruta y pasar los eventos


	}

	public void run() {
		// Establece un manejador para apagados abruptos
		AceptadorShutdown aceptadorShutdown = AceptadorShutdown.crear();
		aceptadorShutdown.setAceptador(this.aceptadorRIP);
		Runtime.getRuntime().addShutdownHook(aceptadorShutdown);



		// bind a un puerto
		try {
			/** Bind del canal Netlink Cliente */
			RawSocketNetlink tmp = new RawSocketNetlink(); //TODO quitar cuando lo siguiente no sea clase anidada
			NetlinkAddress netlinkAddress = tmp.new NetlinkAddress();  //TODO: Quitar de clase anidada
			netlinkAddress.pid = 0;
			netlinkAddress.grupos = RawSocketNetlink.NetlinkMulticastGroup.RTMGRP_IPV4_ROUTE.getValue();
			aceptadorNetlinkCliente.bind(netlinkAddress);

			Thread hiloNetlinkCliente = new Thread(aceptadorNetlinkCliente);
			hiloNetlinkCliente.start();


			/** Bind del canal Netlink Observador */
			RawSocketNetlink tmp2 = new RawSocketNetlink(); //TODO quitar cuando lo siguiente no sea clase anidada
			netlinkAddress = tmp2.new NetlinkAddress();  //TODO: Quitar de clase anidada

			// al hacer el bind, si se rellena el pid, bind falla indicando que la dirección ya está asignada. Dejar en 0 y que elija el sistema.
			netlinkAddress.pid = 0; // (int)RawSocketNetlink.getProcessId();
			netlinkAddress.grupos = 0;
			aceptadorNetlinkObservador.bind(netlinkAddress);

			Thread hiloNetlinkObservador = new Thread(aceptadorNetlinkObservador);
			hiloNetlinkObservador.start();

			/** Bind del canal RIP */
			//			aceptador.bind(new InetSocketAddress("127.0.0.1", 8000));
			InetSocketAddress inetRIP = new InetSocketAddress(520);
			aceptadorRIP.bind(inetRIP);
///
			((AceptadorDatagramaMulticast)aceptadorRIP).unirGrupo(InetAddress.getByName("224.0.0.9"));
			// Pone en marcha RIP (usa para ello el hilo actual)
			aceptadorRIP.run();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("En bind() del aceptador(): m�todo .bind del socket");
			e.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			System.out.println("En bind() del aceptador: getByName() nombre host desconocido");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("En bind() del aceptador(): unirGrupo() suelta IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

			ProtocoloRIPv2 rip;
			String port = args[0];
		try {
			rip = new ProtocoloRIPv2(port);
			rip.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	// TODO: Llevar c�digo a aceptador

	public static class AceptadorShutdown extends Thread
	{
		private AceptadorDatagrama aceptador;

		public static AceptadorShutdown crear() {
			return new AceptadorShutdown();
		}

		public void setAceptador(AceptadorDatagrama aceptador) {
			this.aceptador = aceptador;
		}

		public void run() {
			System.out.println("En aceptador shutdown...");

			// Destruye los filtros
			for (Filtro filtro: aceptador.getCadenaFiltros())
				filtro.destroy();

			// Abandona los grupos multicast a los que se ha unido
			if (aceptador.isMulticast()) {
				AceptadorDatagramaMulticast _aceptador = (AceptadorDatagramaMulticast)aceptador;

				for (InetAddress grupo: _aceptador.getGrupos())
					try {
						((MulticastSocket)_aceptador.getSocket()).leaveGroup(grupo);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
	    }

	}

}

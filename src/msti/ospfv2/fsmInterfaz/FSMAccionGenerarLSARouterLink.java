/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.ospfv2.Dijkstra;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz.FSMIdAccionOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSARouterLinksLinks;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2Hello;
import msti.ospfv2.mensaje.MensajeOSPFv2LSARouterLinks;
import msti.ospfv2.mensaje.MensajeOSPFv2LSARouterLinksLinks;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateAdvertisementHeader;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMAccionGenerarLSARouterLink implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionGenerarLSARouterLink _instancia = new FSMAccionGenerarLSARouterLink();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionGenerarLSARouterLink() {
	}

	
	public void iniciarTemporizadorRouterLink(FSMContextoOSPFv2Interfaz contextoI){
		// Obtiene tarea temporizada de RouterLinkTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoI.getConfiguracion().getTemporizadorRouterLink();
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a MIN_LS_INTERVAL en segundos
		ScheduledThreadPoolExecutor stpe = contextoI.getConfiguracion().tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorRouterLink", (ITimerListener) contextoI.getMaquinaEstados()), 
				new Long(contextoI.getConfiguracion().MIN_LS_INTERVAL), TimeUnit.SECONDS );
		contextoI.getConfiguracion().setTemporizadorRouterLink(tareaPlanificada);
	}
	
	public void iniciarTemporizadorRouterLinkRefreshTime(FSMContextoOSPFv2Interfaz contextoI){
		// Obtiene tarea temporizada de RouterLinkRefreshTimeTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoI.getConfiguracion().getTemporizadorRouterLinkRefreshTime();
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a LSRefreshTime en segundos
		ScheduledThreadPoolExecutor stpe = contextoI.getConfiguracion().tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorRouterLinkRefreshTime", (ITimerListener) contextoI.getMaquinaEstados()), 
				new Long(contextoI.getConfiguracion().LS_REFRESH_TIME), TimeUnit.SECONDS );
		contextoI.getConfiguracion().setTemporizadorRouterLinkRefreshTime(tareaPlanificada);
	}
	
	
	public static MensajeOSPFv2LSARouterLinksLinks generarUnLink(int linkID, int linkData,
													IMensajeOSPFv2LSARouterLinksLinks.Type tipo, short cost){
		
		MensajeOSPFv2LSARouterLinksLinks.Builder routerLink = MensajeOSPFv2LSARouterLinksLinks.Builder.crear();
		//construir LSARouterLinkLinks
		routerLink.setLinkID(linkID);
		routerLink.setLinkData(linkData);
		routerLink.setType(tipo);
		routerLink.setNTOS((byte) 0);
		routerLink.setTOS0Metric(cost);
		//solo el TOS0
		List<Byte> toss = new ArrayList<Byte>();
		List<Short> metrics = new ArrayList<Short>();
		routerLink.setTOSs(toss);
		routerLink.setMetrics(metrics);

		return routerLink.build();		
	}
	
	
	
	public static List<IMensajeOSPFv2LSARouterLinksLinks> obetenerListaDeLinks(FSMContextoOSPFv2Interfaz contextoI){
		
		List<IMensajeOSPFv2LSARouterLinksLinks> listaLinks = new ArrayList<IMensajeOSPFv2LSARouterLinksLinks>();
		
		FSMContextoOSPFv2Interfaz contextoInterfazIterada;
		FSMEstadoOSPFv2Interfaz estadoInterfazIterada;
		MensajeOSPFv2LSARouterLinksLinks link;
		
		//Recorrer todas las interfaces del router que usen protocolo OSPFv2
		for(FSMMaquinaEstadosOSPFv2Interfaz interfaz: contextoI.getConfiguracion().listaFSMInterfaz){
			
			contextoInterfazIterada = (FSMContextoOSPFv2Interfaz) interfaz.getContexto();
			estadoInterfazIterada = (FSMEstadoOSPFv2Interfaz) interfaz.getEstadoActivo();
			//1. Si la red no pertenece a este area no se añaden links (trabajamos en monoarea)
			//2. Si el estado de la interfaz es Down no se añaden links
			if(!estadoInterfazIterada.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.DOWN)){
				
				//3. Si el estado es Point-to-Point... (en principio trabajamos en reades broadcast)
				if(estadoInterfazIterada.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.POINTTOPOINT)){
					
				//4. Si el estado es Loopback
				}else if(estadoInterfazIterada.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.LOOPBACK)){
					//añadir Link con los siguientes campos
					IMensajeOSPFv2LSARouterLinksLinks.Type tipo = IMensajeOSPFv2LSARouterLinksLinks.Type.ConnectionToAStubNetwork;
					link =  generarUnLink(contextoInterfazIterada.getIpInterfaceAddress(),0xFFFFFFFF,tipo, (short) 0);
					listaLinks.add(link);
				
				//5.5 Si el estado es Waiting
				}else if(estadoInterfazIterada.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.WAITING)){
					//añadir Link con los siguientes campos
					IMensajeOSPFv2LSARouterLinksLinks.Type tipo = IMensajeOSPFv2LSARouterLinksLinks.Type.ConnectionToAStubNetwork;
					link =  generarUnLink(contextoInterfazIterada.getIpInterfaceAddress(),contextoInterfazIterada.getIpInterfaceMask(),tipo, contextoInterfazIterada.getInterfaceOutputCost());
					listaLinks.add(link);					
				
				//Si el estado es Backup Dr o DrOther
				}else{
					//Si llegamos a este punto, se ha debido elegir un DR para la red
					//Si tenemos adyacencia plena con el DR o somos el DR y tenemos adyacencia plena con algun vecino,
					//TODO
					
					//sino, añadir link igual al del estado waiting
					IMensajeOSPFv2LSARouterLinksLinks.Type tipo = IMensajeOSPFv2LSARouterLinksLinks.Type.ConnectionToAStubNetwork;
					link =  generarUnLink(contextoInterfazIterada.getIpInterfaceAddress(),contextoInterfazIterada.getIpInterfaceMask(),tipo, contextoInterfazIterada.getInterfaceOutputCost());
					listaLinks.add(link);
				}

			}
		}
		
		
		return listaLinks;
	}
	
	
	
	
	public static MensajeOSPFv2LSARouterLinks generarMensajeOSPFv2LSARouterLinks(FSMContextoOSPFv2Interfaz contextoI) {
		MensajeOSPFv2LSARouterLinks.Builder mensajeLSARouterLinks = MensajeOSPFv2LSARouterLinks.Builder.crear();

		//construir primero header del LSA
		MensajeOSPFv2LinkStateAdvertisementHeader.Builder mensajeLSAHeader = MensajeOSPFv2LinkStateAdvertisementHeader.Builder.crear();
		
		mensajeLSAHeader.setLSAge((short)0);
		mensajeLSAHeader.setOptions(contextoI.getOptions());
		mensajeLSAHeader.setLSType(TipoLS.RouterLinks);
		mensajeLSAHeader.setLinkStateID(contextoI.getRouterID());
		mensajeLSAHeader.setAdvertisingRouter(contextoI.getRouterID());
		mensajeLSAHeader.setLSSequenceNumber(contextoI.getConfiguracion().getLsSequenceNumber());
		//mensajeLSAHeader.setLSChecksum();		Checksum y longitud calculado en el constructor del LSA
		//mensajeLSAHeader.setLength();
		
		//construir LSARouterLink
		mensajeLSARouterLinks.setHeader(mensajeLSAHeader.build());
		mensajeLSARouterLinks.setVEB((byte) 0);
		List<IMensajeOSPFv2LSARouterLinksLinks> links = obetenerListaDeLinks(contextoI);
		mensajeLSARouterLinks.setNLinks((short)links.size());
		mensajeLSARouterLinks.setRouterLinks(links);

		return mensajeLSARouterLinks.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		//Comprobar Temporizador RouterLink, si está activo, posponer la generación del nuevo
		if(contextoI.getConfiguracion().getTemporizadorRouterLink() != null){
			//Si ya se ha creado hace menos de MinLSInterval esperamos a que termine el temporizador
			contextoI.getConfiguracion().routerLinkPospuesto=true;
			
		}else{
			contextoI.getConfiguracion().routerLinkPospuesto=false;
			
			//La última instancia de RouterLink se creó hace más de MinLSInterval asíque podemos generar una nueva (si es necesario)			
			boolean instalarLSA=false;
			
			MensajeOSPFv2LSARouterLinks mensajeNuevo = generarMensajeOSPFv2LSARouterLinks(contextoI);
			
			//Si no hay instancia anterior del LSA se instancia directamente
			long claveLSA = contextoI.getConfiguracion().claveLSA(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.RouterLinks, contextoI.getRouterID());
			if(!contextoI.getConfiguracion().database.containsKey(claveLSA)){
				instalarLSA=true;				
			}else{				
				//Crear nuevo LSA (creado más arriba) y comparar campos con el anteirormente instanciado 
				//Instalarlo en la base de datos si ha vencido el RouterLinkRefreshTime o si algún campo es diferente del anterior
				if(contextoI.getConfiguracion().getTemporizadorRouterLinkRefreshTime() == null){
					instalarLSA=true;
				}else{
					MensajeOSPFv2LSARouterLinks mensajeViejo = (MensajeOSPFv2LSARouterLinks) contextoI.getConfiguracion().database.get(claveLSA);
					if(!mensajeViejo.toString().equals(mensajeNuevo.toString())){
						instalarLSA=true;					
					}
				}
			}
			
			if (instalarLSA){
				//incrementar LSSequenceNumber
				contextoI.getConfiguracion().setLsSequenceNumber(contextoI.getConfiguracion().getLsSequenceNumber() + 1);
			
				//instalarlo en el database
				contextoI.getConfiguracion().agregarLSA(mensajeNuevo);
				//contextoI.getConfiguracion().database.put(mensajeNuevo.getHeader().getLinkStateID(), mensajeNuevo);
				//difundirlo
				FSMIdAccionOSPFv2Interfaz.DIFUNDIR_LSA_NUEVO_POR_INTERFACES.getInstance().execute(contexto, mensajeNuevo);
				//recalcular ruta	
				Dijkstra.recalcularTabla(contextoI.getConfiguracion(), contextoI.getConfiguracion().tablaRutas);
				
				//Si se instala, se inicia el temporizadorRouterLink y el temporizadorRouterLinkRefreshTime
				iniciarTemporizadorRouterLink(contextoI);
				iniciarTemporizadorRouterLinkRefreshTime(contextoI);
				
			}

		}
		
	}
	
	

}
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
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMEstadoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSARouterLinksLinks;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2LSANetworkLinksAdvertisements;
import msti.ospfv2.mensaje.MensajeOSPFv2LSARouterLinks;
import msti.ospfv2.mensaje.MensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS;
import msti.util.ITimerListener;
import msti.util.TimerEventProducer;

public class FSMAccionGenerarLSANetworkLink implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionGenerarLSANetworkLink _instancia = new FSMAccionGenerarLSANetworkLink();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionGenerarLSANetworkLink() {
	}
	
	
	public void iniciarTemporizadorNetworkLink(FSMContextoOSPFv2Interfaz contextoI){
		// Obtiene tarea temporizada de NetworkLinkTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoI.getConfiguracion().getTemporizadorNetworkLink();
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a MIN_LS_INTERVAL en segundos
		ScheduledThreadPoolExecutor stpe = contextoI.getConfiguracion().tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorNetworkLink", (ITimerListener) contextoI.getMaquinaEstados()), 
				new Long(contextoI.getConfiguracion().MIN_LS_INTERVAL), TimeUnit.SECONDS );
		contextoI.getConfiguracion().setTemporizadorNetworkLink(tareaPlanificada);
	}
	
	public void iniciarTemporizadorNetworkLinkRefreshTime(FSMContextoOSPFv2Interfaz contextoI){
		// Obtiene tarea temporizada de NetworkLinkRefreshTimeTimer. Lo reprograma, cancelando previamente el posible anterior
		ScheduledFuture tareaPlanificada = contextoI.getConfiguracion().getTemporizadorNetworkLinkRefreshTime();
		if (tareaPlanificada != null)
			tareaPlanificada.cancel(false);

		//  reinicia el temporizador con tiempo igual a LSRefreshTime en segundos
		ScheduledThreadPoolExecutor stpe = contextoI.getConfiguracion().tablaRutas.getScheduledThreadPoolExecutor();
		tareaPlanificada = stpe.schedule( 
				new TimerEventProducer("TemporizadorNetworkLinkRefreshTime", (ITimerListener) contextoI.getMaquinaEstados()), 
				new Long(contextoI.getConfiguracion().LS_REFRESH_TIME), TimeUnit.SECONDS );
		contextoI.getConfiguracion().setTemporizadorNetworkLinkRefreshTime(tareaPlanificada);
	}
	
	
	
	
	public static MensajeOSPFv2LSANetworkLinksAdvertisements generarMensajeOSPFv2LSANetworkLinksAdvertisements(FSMContextoOSPFv2Interfaz contextoI) {
		MensajeOSPFv2LSANetworkLinksAdvertisements.Builder mensajeLSANetworkLinks = MensajeOSPFv2LSANetworkLinksAdvertisements.Builder.crear();

		//construir primero header del LSA
		MensajeOSPFv2LinkStateAdvertisementHeader.Builder mensajeLSAHeader = MensajeOSPFv2LinkStateAdvertisementHeader.Builder.crear();
		
		mensajeLSAHeader.setLSAge((short)0);
		mensajeLSAHeader.setOptions(contextoI.getOptions());
		mensajeLSAHeader.setLSType(TipoLS.NetworkLinks);
		mensajeLSAHeader.setLinkStateID(contextoI.getIpInterfaceAddress());
		mensajeLSAHeader.setAdvertisingRouter(contextoI.getRouterID());
		mensajeLSAHeader.setLSSequenceNumber(contextoI.getConfiguracion().getLsSequenceNumber());
		//mensajeLSAHeader.setLSChecksum();		Checksum y longitud calculado en el constructor del LSA
		//mensajeLSAHeader.setLength();
		
		//construir LSARouterLink
		mensajeLSANetworkLinks.setHeader(mensajeLSAHeader.build());
		mensajeLSANetworkLinks.setNetworkMask(contextoI.getIpInterfaceMask());
		
		List<Integer> attachedRouters = new ArrayList<Integer>();
		//llenar la lista con los vecinos que esten en estado Full
		
		FSMContextoOSPFv2Vecino contextoV;
		FSMEstadoOSPFv2Vecino estadoVecino;
		for(FSMMaquinaEstadosOSPFv2Vecino vecino: contextoI.getListOfNeighbouringRouters().values()){
			 contextoV = (FSMContextoOSPFv2Vecino) vecino.getContexto() ;
			 estadoVecino = (FSMEstadoOSPFv2Vecino) vecino.getEstadoActivo();
			if(estadoVecino.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.FULL)){
				attachedRouters.add(contextoV.getNeighborID());	
			}	
		}
		mensajeLSANetworkLinks.setAttachedRouters(attachedRouters);

		return mensajeLSANetworkLinks.build();		
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		//Comprobar Temporizador NetworkLink, si está activo, posponer la generación del nuevo
		if(contextoI.getConfiguracion().getTemporizadorNetworkLink() != null){
			//Si ya se ha creado hace menos de MinLSInterval esperamos a que termine el temporizador
			contextoI.getConfiguracion().networkLinkPospuesto=true;
			
		}else{
			contextoI.getConfiguracion().networkLinkPospuesto=false;
			
			//La última instancia de NetworkLink se creó hace más de MinLSInterval asíque podemos generar una nueva (si es necesario)			
			boolean instalarLSA=false;
			
			MensajeOSPFv2LSANetworkLinksAdvertisements mensajeNuevo = generarMensajeOSPFv2LSANetworkLinksAdvertisements(contextoI);
			
			//Si no hay instancia anterior del LSA se instancia directamente
			long claveLSA = contextoI.getConfiguracion().claveLSA(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks, contextoI.getIpInterfaceAddress());
			if(!contextoI.getConfiguracion().database.containsKey(claveLSA)){
				instalarLSA=true;				
			}else{
				//Crear nuevo LSA (creado más arriba) y comparar campos con el anteriormente instanciado
				//Instalarlo en la base de datos si ha vencido el NetworkLinkRefreshTime o si algún campo es diferente del anterior
				if(contextoI.getConfiguracion().getTemporizadorNetworkLinkRefreshTime() == null){
					instalarLSA=true;
				}else{
					MensajeOSPFv2LSANetworkLinksAdvertisements mensajeViejo = (MensajeOSPFv2LSANetworkLinksAdvertisements) contextoI.getConfiguracion().database.get(claveLSA);
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
				iniciarTemporizadorNetworkLink(contextoI);
				iniciarTemporizadorNetworkLinkRefreshTime(contextoI);
			}

		}

		
		
	}

}
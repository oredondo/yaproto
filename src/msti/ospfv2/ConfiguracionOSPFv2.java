package msti.ospfv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import msti.ospfv2.fsmInterfaz.FSMContextoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;

/**
 * Clase de configuración OSPFv2
 *
 */
public class ConfiguracionOSPFv2 {

	protected static ConfiguracionOSPFv2 instancia = null;
	
	//OSPFv2, configuracion de área
	public int areaID=1;
	public short auType=0;
	public boolean externalRoutingCapability = false;
	
	public int routerID;
	
	//Database, map: (Type+LinkStateID), lsa
	public  Map<Long,IMensajeOSPFv2LSA> database = new HashMap<Long,IMensajeOSPFv2LSA>();
	private int lsSequenceNumber = 0x80000001;
	//RouterLinkTimer
	private ScheduledFuture temporizadorRouterLink=null;
	private ScheduledFuture temporizadorRouterLinkRefreshTime=null;
	public boolean routerLinkPospuesto = false;
	//NetworkLinkTimer
	private ScheduledFuture temporizadorNetworkLink=null;
	private ScheduledFuture temporizadorNetworkLinkRefreshTime=null;
	public boolean networkLinkPospuesto = false;
	
	public List<FSMMaquinaEstadosOSPFv2Interfaz> listaFSMInterfaz = new ArrayList<FSMMaquinaEstadosOSPFv2Interfaz>();
	public TablaRutas tablaRutas;
	public String idTablaRutas = "0";
	
	
	
	//Architectural Constants
	public short LS_REFRESH_TIME = 1800; 
	public short MIN_LS_INTERVAL = 5;
	public short MAX_AGE = 3600; 
	public short CHECK_AGE = 300;
	public short MAX_AGE_DIF = 900;
	public int LS_INFINITY = 0xFFFFFF;
	public String DEFAULT_DESTINATION = "0.0.0.0";
	
	public static ConfiguracionOSPFv2 getInstance() {
		
		if(instancia==null){
			instancia = new ConfiguracionOSPFv2();
		}		
		return instancia;
	}
	
	private ConfiguracionOSPFv2(){
		
	}
	
	
	public ScheduledFuture getTemporizadorRouterLink() {
		return temporizadorRouterLink;
	}
	public void setTemporizadorRouterLink(ScheduledFuture temporizadorRouterLink) {
		this.temporizadorRouterLink = temporizadorRouterLink;
	}
	public ScheduledFuture getTemporizadorRouterLinkRefreshTime() {
		return temporizadorRouterLinkRefreshTime;
	}
	public void setTemporizadorRouterLinkRefreshTime(ScheduledFuture temporizadorRouterLinkRefreshTime) {
		this.temporizadorRouterLinkRefreshTime = temporizadorRouterLinkRefreshTime;
	}
	
	public ScheduledFuture getTemporizadorNetworkLink() {
		return temporizadorNetworkLink;
	}
	public void setTemporizadorNetworkLink(ScheduledFuture temporizadorNetworkLink) {
		this.temporizadorNetworkLink = temporizadorNetworkLink;
	}
	public ScheduledFuture getTemporizadorNetworkLinkRefreshTime() {
		return temporizadorNetworkLinkRefreshTime;
	}
	public void setTemporizadorNetworkLinkRefreshTime(
			ScheduledFuture temporizadorNetworkLinkRefreshTime) {
		this.temporizadorNetworkLinkRefreshTime = temporizadorNetworkLinkRefreshTime;
	}
	
	public int getLsSequenceNumber() {
		return lsSequenceNumber;
	}
	public void setLsSequenceNumber(int lsSequenceNumber) {
		this.lsSequenceNumber = lsSequenceNumber;
	}

	
	
	//funciones para añadir o listar LSA del database
	
	/**
	 * Agrega un LSA al database
	 * @param lsa El LSA que queremos añadir al database
	 * @return el lsa anterior si había uno guardado con la misma clave (tipo-linkStateID), null si no había
	 */
	public IMensajeOSPFv2LSA agregarLSA(IMensajeOSPFv2LSA lsa){
		return database.put(claveLSA(lsa.getHeader().getLSType(),lsa.getHeader().getLinkStateID()), lsa);
		
	}
	
	/**
	 * Lista los LSA de tipo RouterLinkAdvertisment que hay en la database
	 * @return lista de LSA (todos ellos serán de tipo RouterLinkAdvertisment)
	 */
	public List<IMensajeOSPFv2LSA> getLSARouterLinksInDatabase(){
		List<IMensajeOSPFv2LSA> listaRouterLinks = new ArrayList<IMensajeOSPFv2LSA>();
		for(IMensajeOSPFv2LSA lsa: database.values()){
			if(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.RouterLinks)){
				listaRouterLinks.add(lsa);
			}						
		}
		
		/*
		for(Long clave: database.keySet()){
			Long tipo = clave;
			tipo >>=32;
			tipo = tipo & 0xff;
			if(tipo.byteValue()==IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.RouterLinks.getCodigo()){
				listaRouterLinks.add(database.get(clave));
			}
		}*/
		return listaRouterLinks;
	}
	
	/**
	 * Lista los LSA de tipo NetworkLinkAdvertisment que hay en la database
	 * @return lista de LSA (todos ellos serán de tipo NetworkLinkAdvertisment)
	 */
	public List<IMensajeOSPFv2LSA> getLSANetworkLinksInDatabase(){
		List<IMensajeOSPFv2LSA> listaNetworkLinks = new ArrayList<IMensajeOSPFv2LSA>();
		
		for(IMensajeOSPFv2LSA lsa: database.values()){
			if(lsa.getHeader().getLSType().equals(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks)){
				listaNetworkLinks.add(lsa);
			}						
		}
		/*
		for(Long clave: database.keySet()){
			Long tipo = clave;
			tipo >>=32;
			tipo = tipo & 0xff;
			if(tipo.byteValue()==IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS.NetworkLinks.getCodigo()){
				listaNetworkLinks.add(database.get(clave));
			}
		}
		*/
		return listaNetworkLinks;
		
	}
	
	/**
	 * Calcula la clave que usaremos para guardar el LSA en la database,
	 * compuesta por el tipo (primeros 4 bytes) y el likState ID (últimos 4 bytes) concatenados en un long
	 * @param tipo Valor del TipoLS del LSA
	 * @param linkStateID Valor del linkStateID del LSA
	 * @return clave utilizada en el database
	 */
	public long claveLSA(IMensajeOSPFv2LinkStateAdvertisementHeader.TipoLS tipo, int linkStateID){
		
		long clave = tipo.getCodigo();
		clave <<=32;
		clave+=linkStateID;
		
		return clave;
	}
	
	/**
	 * Busca un LSA de tipo RouterLink que haya sido emitido por un router concreto (AdvertisingRouter)
	 * @param routerID Del router que buscamos
	 * @return lsa o null si no existe
	 */
	public IMensajeOSPFv2LSA getRouterLinkWithRouterID(int routerID){
		List<IMensajeOSPFv2LSA> listaRouterLinks = getLSARouterLinksInDatabase();
		for(IMensajeOSPFv2LSA lsa: listaRouterLinks){
			if(lsa.getHeader().getAdvertisingRouter()==routerID){
				return lsa;
			}
		}
		return null;
	}

	/**
	 * Lista los prefijos de red de todas las interfaces dadas de alta en el protocolo
	 * @return lista de Prefijos
	 */
	public List<Integer> getPrefijosInterfaces(){
		
		List<Integer> listaPrefijos = new ArrayList<Integer>();
		FSMContextoOSPFv2Interfaz contextoI;
		
		for(FSMMaquinaEstadosOSPFv2Interfaz interfaz: listaFSMInterfaz){
			contextoI = (FSMContextoOSPFv2Interfaz) interfaz.getContexto();
			listaPrefijos.add(contextoI.getIpInterfaceAddress() & contextoI.getIpInterfaceMask());			
		}
		return listaPrefijos;
	}
	
}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import msti.fsm.FSMContexto;
import msti.io.Sesion;
import msti.ospfv2.ConfiguracionOSPFv2;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;
import msti.util.Inet4Address;

public class FSMContextoOSPFv2Interfaz extends FSMContexto {


	/*
	// Temporizador 30s y marca de activado 
	private Timer temporizador30s;
	private boolean temporizador30sActivo = false;
	*/
	
	//Type
	private boolean isPointToPoint;
	private boolean isNetworkNonBroadcast;
	
	private String estadoDesignated;  //"Backup", "Dr" o "DrOther"
	private int routerID;
	
	
	private int ipInterfaceAddress;
	private int ipInterfaceMask;
	private int areaID;
	private short helloInterval;
	private int routerDeadInterval;
	private int infTransDelay;
	private byte routerPriority;
	private byte options;
	
	//helloTimer
	private ScheduledFuture temporizadorHello;
	//WaitTimer
	private ScheduledFuture temporizadorWait;
	
	private Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters;		//mapa de IDs/ FSMVecino
	private int designatedRouter;			//id (la ip la busco en la lista)
	private int backupDesignatedRouter;		//id
	private short interfaceOutputCost; //(metric)
	private int rxmtInterval;
	private long authenticationKey;
	
	//Area Parameters
	private short auType;
	private boolean externalRoutingCapability;
	
	private Sesion sesion;
	private ConfiguracionOSPFv2 configuracion;
	
	
	public FSMContextoOSPFv2Interfaz() {
		super();
		
		//iniciar Temporizadores en null, ya que al resetear se comprueba si están inicializados o no (para cancelar la tarea en su caso)
		setTemporizadorHello(null);
		setTemporizadorWait(null);
		this.resetearVariables();	
	}
	
	public void resetearVariables(){
		//Type
		this.isPointToPoint=false;
		this.isNetworkNonBroadcast=false;
		
		this.setEstadoDesignated(new String("DrOther"));
		
		
		//this.setRouterID(0);					//Se elige en el ConfiguracionOSPFv2
		 
		try {
			this.setIpInterfaceAddress(Inet4Address.toInt(InetAddress.getByName("192.168.1.1")));
			this.setIpInterfaceMask(Inet4Address.toInt(InetAddress.getByName("255.255.255.0")));
		} catch (UnknownHostException e) {
		}
		//this.setIpInterfaceMask(0xFFFFFF00);
		//this.setAreaID(0);
		this.setHelloInterval((short) 10);		//debe ser igual para todos los routers de la red
		this.setRouterDeadInterval(40);			//debe ser igual para todos los routers de la red
		this.setInfTransDelay(1);
		this.setRouterPriority((byte) 1);				//Priority 0 == no designable	
		this.setOptions((byte) 0);
		
		//helloTimer
		if (temporizadorHello != null) {
			temporizadorHello.cancel(false);
			setTemporizadorHello(null);
		}
		//WaitTimer
		if (temporizadorWait != null) {
			temporizadorWait.cancel(false);
			setTemporizadorWait(null);
		}
		
		this.setListOfNeighbouringRouters(new ConcurrentHashMap<Integer, FSMMaquinaEstadosOSPFv2Vecino>());
		this.setDesignatedRouter(0);			//Iniciado a 0 si aún no hay (ip)
		this.setBackupDesignatedRouter(0);		//Iniciado a 0 si aún no hay (ip)
		this.setInterfaceOutputCost((short) 1);
		this.setRxmtInterval(5);
		this.setAuthenticationKey((long) 1234);
		
		//Area Parameters
		//this.setAuType((short) 0);	//0: No authentication, 1: Simple password
		//this.setExternalRoutingCapability(true);
		
		//setSesion(null);
		//setConfiguracion(null);
	}

	//Get/set
	
	
	public boolean isPointToPoint() {
		return isPointToPoint;
	}

	public boolean isNetworkNonBroadcast() {
		return isNetworkNonBroadcast;
	}

	public String getEstadoDesignated() {
		return estadoDesignated;
	}
	public void setEstadoDesignated(String estadoDesignated) {
		this.estadoDesignated = estadoDesignated;
	}

	public int getRouterID() {
		return routerID;
	}
	public void setRouterID(int routerID) {
		this.routerID = routerID;
	}
	
	public int getIpInterfaceAddress() {
		return ipInterfaceAddress;
	}
	public void setIpInterfaceAddress(int ipInterfaceAddress) {
		this.ipInterfaceAddress = ipInterfaceAddress;
	}

	public int getIpInterfaceMask() {
		return ipInterfaceMask;
	}
	public void setIpInterfaceMask(int ipInterfaceMask) {
		this.ipInterfaceMask = ipInterfaceMask;
	}

	public int getAreaID() {
		return areaID;
	}
	public void setAreaID(int areaID) {
		this.areaID = areaID;
	}

	public short getHelloInterval() {
		return helloInterval;
	}
	public void setHelloInterval(short helloInterval) {
		this.helloInterval = helloInterval;
	}

	public int getRouterDeadInterval() {
		return routerDeadInterval;
	}
	public void setRouterDeadInterval(int routerDeadInterval) {
		this.routerDeadInterval = routerDeadInterval;
	}

	public int getInfTransDelay() {
		return infTransDelay;
	}
	public void setInfTransDelay(int infTransDelay) {
		this.infTransDelay = infTransDelay;
	}

	public byte getRouterPriority() {
		return routerPriority;
	}
	public void setRouterPriority(byte routerPriority) {
		this.routerPriority = routerPriority;
	}

	public ScheduledFuture getTemporizadorHello() {
		return temporizadorHello;
	}
	public void setTemporizadorHello(ScheduledFuture temporizadorHello) {
		this.temporizadorHello = temporizadorHello;
	}

	public ScheduledFuture getTemporizadorWait() {
		return temporizadorWait;
	}
	public void setTemporizadorWait(ScheduledFuture temporizadorWait) {
		this.temporizadorWait = temporizadorWait;
	}

	public Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> getListOfNeighbouringRouters() {
		return listOfNeighbouringRouters;
	}
	public void setListOfNeighbouringRouters(
			Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters) {
		this.listOfNeighbouringRouters = listOfNeighbouringRouters;
	}

	public int getDesignatedRouter() {
		return designatedRouter;
	}
	public void setDesignatedRouter(int designatedRouter) {
		this.designatedRouter = designatedRouter;
	}

	public int getBackupDesignatedRouter() {
		return backupDesignatedRouter;
	}
	public void setBackupDesignatedRouter(int backupDesignatedRouter) {
		this.backupDesignatedRouter = backupDesignatedRouter;
	}

	public short getInterfaceOutputCost() {
		return interfaceOutputCost;
	}
	public void setInterfaceOutputCost(short interfaceOutputCost) {
		this.interfaceOutputCost = interfaceOutputCost;
	}

	public int getRxmtInterval() {
		return rxmtInterval;
	}
	public void setRxmtInterval(int rxmtInterval) {
		this.rxmtInterval = rxmtInterval;
	}

	public long getAuthenticationKey() {
		return authenticationKey;
	}

	public void setAuthenticationKey(long authenticationKey) {
		this.authenticationKey = authenticationKey;
	}

	public short getAuType() {
		return auType;
	}
	public void setAuType(short auType) {
		this.auType = auType;
	}

	public boolean isExternalRoutingCapability() {
		return externalRoutingCapability;
	}
	public void setExternalRoutingCapability(boolean externalRoutingCapability) {
		this.externalRoutingCapability = externalRoutingCapability;
	}

	public byte getOptions() {
		return options;
	}
	public void setOptions(byte options) {
		this.options = options;
	}

	public Sesion getSesion() {
		return sesion;
	}
	public void setSesion(Sesion sesion) {
		this.sesion = sesion;
	}

	public ConfiguracionOSPFv2 getConfiguracion() {
		return configuracion;
	}
	public void setConfiguracion(ConfiguracionOSPFv2 configuracion) {
		this.configuracion = configuracion;
	}



	
}

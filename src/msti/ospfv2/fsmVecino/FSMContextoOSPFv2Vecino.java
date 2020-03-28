/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import msti.fsm.FSMContexto;
import msti.io.Sesion;
import msti.ospfv2.fsmInterfaz.FSMContextoOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.ospfv2.mensaje.IMensajeOSPFv2LinkStateAdvertisementHeader;
import msti.ospfv2.mensaje.MensajeOSPFv2DatabaseDescription;

public class FSMContextoOSPFv2Vecino extends FSMContexto {

	
	private String estadoAdjacencia;
	
	//InactivityTimer
	private ScheduledFuture temporizadorInactivity;
	//EnvioDDPconIMMSTimer
	private ScheduledFuture temporizadorDDPconIMMS;
	//LinkStateRequestListTimer
	private ScheduledFuture temporizadorLinkStateRequestList;
	 
	private boolean isMaster;
	private boolean AllDatabaseDescriptionPacketSent;
	private int ddSequenceNumber;
	private MensajeOSPFv2DatabaseDescription ultimoDDPEnviado;
	
	private int neighborID;
	private byte neighborPriority;
	private int neighborIPAddress;
	private byte neighborOptions;
	private int neighborDesignatedRouter;
	private int neighborBackupDesignatedRouter;
	
	//map: LinkStateID, lsa
	private Map<Long,IMensajeOSPFv2LSA> linkStateRetransmissionList;
	private Map<Long,IMensajeOSPFv2LSA> databaseSummaryList;
	//map: LinkStateID, header
	private Map<Long, IMensajeOSPFv2LinkStateAdvertisementHeader> linkStateRequestList;
	
	private FSMContextoOSPFv2Interfaz contextoInterfaz;
	//private Sesion sesion;
	
	public FSMContextoOSPFv2Vecino() {
		super();
		
		setEstadoAdjacencia("");
		
		setTemporizadorInactivity(null);
		setTemporizadorDDPconIMMS(null);
		setTemporizadorLinkStateRequestList(null);
		
		setMaster(false);				//1 Master, 0 Slave
		setAllDatabaseDescriptionPacketSent(false);
		setDdSequenceNumber(-1);		//-1 o 0 ???
		setUltimoDDPEnviado(null);
		
		setNeighborID(0);
		setNeighborPriority((byte) 0);
		setNeighborIPAddress(0);					//IP? como la saco?
		setNeighborOptions((byte)0);
		setNeighborDesignatedRouter(0);				//ip del DR
		setNeighborBackupDesignatedRouter(0);		//ip del BDR
		
		setLinkStateRetransmissionList( new HashMap<Long,IMensajeOSPFv2LSA>());
		setDatabaseSummaryList( new HashMap<Long,IMensajeOSPFv2LSA>());
		setLinkStateRequestList( new HashMap<Long,IMensajeOSPFv2LinkStateAdvertisementHeader>());
		
		//setSesion(null);
		
	}


	public String getEstadoAdjacencia() {
		return estadoAdjacencia;
	}
	public void setEstadoAdjacencia(String estadoAdjacencia) {
		this.estadoAdjacencia = estadoAdjacencia;
	}


	public ScheduledFuture getTemporizadorInactivity() {
		return temporizadorInactivity;
	}
	public void setTemporizadorInactivity(ScheduledFuture temporizadorInactivity) {
		this.temporizadorInactivity = temporizadorInactivity;
	}


	public ScheduledFuture getTemporizadorDDPconIMMS() {
		return temporizadorDDPconIMMS;
	}
	public void setTemporizadorDDPconIMMS(ScheduledFuture temporizadorDDPconIMMS) {
		this.temporizadorDDPconIMMS = temporizadorDDPconIMMS;
	}


	public boolean isMaster() {
		return isMaster;
	}
	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}


	public int getDdSequenceNumber() {
		return ddSequenceNumber;
	}
	public void setDdSequenceNumber(int ddSequenceNumber) {
		this.ddSequenceNumber = ddSequenceNumber;
	}


	public int getNeighborID() {
		return neighborID;
	}
	public void setNeighborID(int neighborID) {
		this.neighborID = neighborID;
	}


	public byte getNeighborPriority() {
		return neighborPriority;
	}
	public void setNeighborPriority(byte neighborPriority) {
		this.neighborPriority = neighborPriority;
	}


	public int getNeighborIPAddress() {
		return neighborIPAddress;
	}
	public void setNeighborIPAddress(int neighborIPAddress) {
		this.neighborIPAddress = neighborIPAddress;
	}


	public byte getNeighborOptions() {
		return neighborOptions;
	}
	public void setNeighborOptions(byte neighborOptions) {
		this.neighborOptions = neighborOptions;
	}


	public int getNeighborDesignatedRouter() {
		return neighborDesignatedRouter;
	}
	public void setNeighborDesignatedRouter(int neighborDesignatedRouter) {
		this.neighborDesignatedRouter = neighborDesignatedRouter;
	}


	public int getNeighborBackupDesignatedRouter() {
		return neighborBackupDesignatedRouter;
	}
	public void setNeighborBackupDesignatedRouter(
			int neighborBackupDesignatedRouter) {
		this.neighborBackupDesignatedRouter = neighborBackupDesignatedRouter;
	}


	public Map<Long,IMensajeOSPFv2LSA> getLinkStateRetransmissionList() {
		return linkStateRetransmissionList;
	}
	public void setLinkStateRetransmissionList(
			Map<Long,IMensajeOSPFv2LSA> linkStateRetransmissionList) {
		this.linkStateRetransmissionList = linkStateRetransmissionList;
	}


	public Map<Long,IMensajeOSPFv2LSA> getDatabaseSummaryList() {
		return databaseSummaryList;
	}
	public void setDatabaseSummaryList(Map<Long,IMensajeOSPFv2LSA> databaseSummaryList) {
		this.databaseSummaryList = databaseSummaryList;
	}


	public Map<Long,IMensajeOSPFv2LinkStateAdvertisementHeader> getLinkStateRequestList() {
		return linkStateRequestList;
	}
	public void setLinkStateRequestList(Map<Long,IMensajeOSPFv2LinkStateAdvertisementHeader> linkStateRequestList) {
		this.linkStateRequestList = linkStateRequestList;
	}


	public FSMContextoOSPFv2Interfaz getContextoInterfaz() {
		return contextoInterfaz;
	}
	public void setContextoInterfaz(FSMContextoOSPFv2Interfaz contextoInterfaz) {
		this.contextoInterfaz = contextoInterfaz;
	}


	/*public Sesion getSesion() {
		return sesion;
	}
	public void setSesion(Sesion sesion) {
		this.sesion = sesion;
	}*/


	public ScheduledFuture getTemporizadorLinkStateRequestList() {
		return temporizadorLinkStateRequestList;
	}
	public void setTemporizadorLinkStateRequestList(
			ScheduledFuture temporizadorLinkStateRequestList) {
		this.temporizadorLinkStateRequestList = temporizadorLinkStateRequestList;
	}


	public boolean isAllDatabaseDescriptionPacketSent() {
		return AllDatabaseDescriptionPacketSent;
	}
	public void setAllDatabaseDescriptionPacketSent(
			boolean allDatabaseDescriptionPacketSent) {
		AllDatabaseDescriptionPacketSent = allDatabaseDescriptionPacketSent;
	}


	public MensajeOSPFv2DatabaseDescription getUltimoDDPEnviado() {
		return ultimoDDPEnviado;
	}
	public void setUltimoDDPEnviado(MensajeOSPFv2DatabaseDescription ultimoDDPEnviado) {
		this.ultimoDDPEnviado = ultimoDDPEnviado;
	}
	
	

	


	
}

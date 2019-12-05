/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.util.Iterator;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
//import msti.rip.mensaje.MensajeRIPRespuesta;

public class FSMAccionDeterminarSiSeFormaAdjacencia implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDeterminarSiSeFormaAdjacencia _instancia = new FSMAccionDeterminarSiSeFormaAdjacencia();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionDeterminarSiSeFormaAdjacencia() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;

		boolean crearAdjacencia=false;
		
		//Si la red es point-to-point o virtual link
		if(contextoV.getContextoInterfaz().isPointToPoint()){
			crearAdjacencia=true;
			
		//Si el router es Designated Router
		}else if(contextoV.getContextoInterfaz().getDesignatedRouter() == contextoV.getContextoInterfaz().getIpInterfaceAddress()){
			crearAdjacencia=true;
			
		//Si el router es Backup Designater Router
		}else if(contextoV.getContextoInterfaz().getBackupDesignatedRouter() == contextoV.getContextoInterfaz().getIpInterfaceAddress()){
			crearAdjacencia=true;
			
		//Si el vecino es Designated Router
		}else if(contextoV.getNeighborDesignatedRouter() == contextoV.getNeighborIPAddress()){
			crearAdjacencia=true;
			
		//Si el vecino es Backup Designater Router
		}else if(contextoV.getNeighborBackupDesignatedRouter() == contextoV.getNeighborIPAddress()){
			crearAdjacencia=true;
		}
			
		
		//Modificar atributo "EstadoAdjacencia" para el vecino
		if(crearAdjacencia){
			contextoV.setEstadoAdjacencia("Crear adjacencia");				
		}else{
			contextoV.setEstadoAdjacencia("No crear adjacencia");	
		}
		
		
	}

}
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

public class FSMAccionDeterminarSiSeConservaAdjacencia implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionDeterminarSiSeConservaAdjacencia _instancia = new FSMAccionDeterminarSiSeConservaAdjacencia();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionDeterminarSiSeConservaAdjacencia() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		boolean conservarAdjacencia=false;
		
		//Si la red es point-to-point o virtual link
		if(contextoV.getContextoInterfaz().isPointToPoint()){
			conservarAdjacencia=true;
			
		//Si el router es Designated Router
		}else if(contextoV.getContextoInterfaz().getDesignatedRouter() == contextoV.getContextoInterfaz().getIpInterfaceAddress()){
			conservarAdjacencia=true;
			
		//Si el router es Backup Designater Router
		}else if(contextoV.getContextoInterfaz().getBackupDesignatedRouter() == contextoV.getContextoInterfaz().getIpInterfaceAddress()){
			conservarAdjacencia=true;
			
		//Si el vecino es Designated Router
		}else if(contextoV.getNeighborDesignatedRouter() == contextoV.getNeighborIPAddress()){
			conservarAdjacencia=true;
			
		//Si el vecino es Backup Designater Router
		}else if(contextoV.getNeighborBackupDesignatedRouter() == contextoV.getNeighborIPAddress()){
			conservarAdjacencia=true;
		}	
			
		
		//Modificar atributo "EstadoAdjacencia" para el vecino
		if(conservarAdjacencia){
			contextoV.setEstadoAdjacencia("Conservar adjacencia");			
		}else{
			contextoV.setEstadoAdjacencia("Destruir adjacencia");
		}
		
		
		
	}

}
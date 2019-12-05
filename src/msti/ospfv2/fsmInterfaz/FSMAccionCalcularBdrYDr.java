/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.fsm.FSMEvento;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.TablaRutas.EstadoRuta;
import msti.ospfv2.TablaRutas.Ruta;
import msti.ospfv2.fsmInterfaz.FSMMaquinaEstadosOSPFv2Interfaz.FSMIdAccionOSPFv2Interfaz;
import msti.ospfv2.fsmVecino.FSMContextoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMEstadoOSPFv2Vecino;
import msti.ospfv2.fsmVecino.FSMIdEventoOSPFv2Vecino;
//import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.ospfv2.fsmVecino.FSMMaquinaEstadosOSPFv2Vecino;

public class FSMAccionCalcularBdrYDr implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionCalcularBdrYDr _instancia = new FSMAccionCalcularBdrYDr();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciaciÃ³n
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionCalcularBdrYDr() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {

		FSMContextoOSPFv2Interfaz contextoI=(FSMContextoOSPFv2Interfaz) contexto;
		
		Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters=contextoI.getListOfNeighbouringRouters();
		Set<Integer> keySetNeighboringRouters = listOfNeighbouringRouters.keySet();
				
		//Se crea una lista con los routers que pueden ser elegidos DR
		List<Integer> listaRoutersElegibles = new ArrayList();
		
		//En ella se incluye este router y todos los vecinos cuyo estado sea mayor o igual que 2-way (siempre que sean designables)
		if(contextoI.getRouterPriority()!=0)
			listaRoutersElegibles.add(contextoI.getRouterID());
		
		for(Integer i : keySetNeighboringRouters){
			FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();
			
			if(contextoV.getNeighborPriority()!=0){
				FSMEstado estadoActualNeighbor = (FSMEstado) contextoV.getMaquinaEstados().getEstadoActivo();
				//if Estado>=2way
				if(estadoActualNeighbor.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.TWOWAY) ||
					estadoActualNeighbor.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXSTART) ||	
					estadoActualNeighbor.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.EXCHANGE) ||
					estadoActualNeighbor.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.LOADING) ||
					estadoActualNeighbor.getId().equals(FSMEstadoOSPFv2Vecino.FSMIdEstadoOSPFv2Vecino.FULL)){
					
						listaRoutersElegibles.add(i);
				}
			}						
		}
		
		//Despues se eliminan de la lista los routers no designables (prioridad=0) (hecho en el paso anterior)		
		//Seguir los 7 pasos del Algoritmo de elección de DR con los routers de la lista
		
		//Paso 1. Anotar el DR y BDR actuales.
		int drAnterior=contextoI.getDesignatedRouter();
		int bdrAnterior=contextoI.getBackupDesignatedRouter();
		
		//Paso 2.Cálculo del nuevo BDR		
		int bdrNuevo=paso2(listaRoutersElegibles, listOfNeighbouringRouters, contextoI);		
		
		//Paso 3. Cálculo del nuevo DR		
		int drNuevo=paso3(listaRoutersElegibles, listOfNeighbouringRouters, contextoI, bdrNuevo);
		
		//Paso 4. Si el router ha cambiado repetir pasos 2 y 3
		if(drAnterior==contextoI.getRouterID() && drNuevo!=contextoI.getRouterID() ||
			bdrAnterior==contextoI.getRouterID() && bdrNuevo!=contextoI.getRouterID() ||	
			drAnterior!=contextoI.getRouterID() && drNuevo==contextoI.getRouterID() ||
			bdrAnterior!=contextoI.getRouterID() && bdrNuevo==contextoI.getRouterID()){
			
				bdrNuevo=paso2(listaRoutersElegibles, listOfNeighbouringRouters, contextoI);
				drNuevo=paso3(listaRoutersElegibles, listOfNeighbouringRouters, contextoI, bdrNuevo);
		}
		
		
		//Paso 5. Elegir el nuevo estado de la interfaz (Backup, Dr o DrOther)
			//(si hemos hecho bien lo anterior debería ser imposible que el router sea DR y BDR a la vez)
			//actualizamos además los valores en el ContextoInterfaz y llamamos a la acción de generar RouterLink si el DR ha cambiado
		
		contextoI.setBackupDesignatedRouter(bdrNuevo);
		contextoI.setDesignatedRouter(drNuevo);
		if(drAnterior!=drNuevo){
			//LLama a la accion de generacion de routerLinks
			FSMIdAccionOSPFv2Interfaz.GENERAR_LSA_ROUTER_LINKS.getInstance().execute(contextoI, o);
		}
		
		if(drNuevo==contextoI.getRouterID()){
			contextoI.setEstadoDesignated("Dr");
		}else if(bdrNuevo==contextoI.getRouterID()){
			contextoI.setEstadoDesignated("Backup");
		}else{
			contextoI.setEstadoDesignated("DrOther");
		}
		
		//Paso 6. Si la red es non-Broadcast y el router es ahora DR o BDR, generar evento start en vecinos que no puedan ser DR o BDR
		if(contextoI.isNetworkNonBroadcast()){
			if(drNuevo==contextoI.getRouterID() || bdrNuevo==contextoI.getRouterID()){
				
				for(Integer i : keySetNeighboringRouters){
					FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();						
					//Generar evento Start en todos los vecinos con prioridad=0
					if(contextoV.getNeighborPriority()==0){
						FSMEvento eventoStart = new FSMEvento(FSMIdEventoOSPFv2Vecino.START, i);
						((FSMMaquinaEstadosOSPFv2Vecino)contextoV.getMaquinaEstados()).encolarEvento(eventoStart);
					}								
				}
				
			}
		}		
		
		//Paso 7. Si ha cambiado el Dr o el Bdr, evaluar Adjacencias (generar ADJOK en vecinos)
		if(drAnterior!=drNuevo || bdrAnterior!=bdrNuevo){
			
			for(Integer i : keySetNeighboringRouters){
				FSMMaquinaEstadosOSPFv2Vecino vecino = listOfNeighbouringRouters.get(i);				
				
				//Generar evento ADJOK en todos los vecinos
				FSMEvento eventoAdjok = new FSMEvento(FSMIdEventoOSPFv2Vecino.ADJOK, i);
				vecino.encolarEvento(eventoAdjok);						
			}
			
		}
		
		
		//Fin
		
		
		
		
	}
	
	

	public int paso2(List<Integer> listaRoutersElegibles, Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters,
			FSMContextoOSPFv2Interfaz contextoI){
		//Cálculo de nuevo BDR
		//Listar los routers que no se declaran DR a sí mismos
		List<Integer> listaNoDRs = new ArrayList();
		
		for(Integer i : listaRoutersElegibles){
			//se trata diferente si es este router o un vecino
			if(i==contextoI.getRouterID()){
				if(contextoI.getDesignatedRouter()!=contextoI.getRouterID())
					listaNoDRs.add(i);
			}else{
				FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();				
				if(contextoV.getNeighborDesignatedRouter()!=contextoV.getNeighborID())
					listaNoDRs.add(i);
			}							
		}
		
		//Entre estos, listar los routers que se declaran BDR a sí mismos
		List<Integer> listaBDRs = new ArrayList();
		
		for(Integer i : listaNoDRs){
			//se trata diferente si es este router o un vecino
			if(i==contextoI.getRouterID()){
				if(contextoI.getBackupDesignatedRouter()==contextoI.getRouterID())
					listaBDRs.add(i);
			}else{
				FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();				
				if(contextoV.getNeighborBackupDesignatedRouter()==contextoV.getNeighborID())
					listaBDRs.add(i);
			}							
		}
		
		//si no hay ninguno, se elige como BDR al que tenga mayor prioridad o en caso de empate al de mayor id
		//dentro de los no declarados DR
		int bdrMayorPrioridad;
		int prioridadMayor;
		
		if (listaBDRs.isEmpty()){
			bdrMayorPrioridad=0;
			prioridadMayor=0;
			
			for(Integer i : listaNoDRs){
				//se trata diferente si es este router o un vecino
				if(i==contextoI.getRouterID()){
					if(contextoI.getRouterPriority()>prioridadMayor){
						prioridadMayor=contextoI.getRouterPriority();
						bdrMayorPrioridad=i;
					}else if(contextoI.getRouterPriority()==prioridadMayor){
						if(i>bdrMayorPrioridad){
							bdrMayorPrioridad=i;
						}
					}
				}else{
					FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();
					
					if(contextoV.getNeighborPriority()>prioridadMayor){
						prioridadMayor=contextoV.getNeighborPriority();
						bdrMayorPrioridad=i;
					}else if(contextoV.getNeighborPriority()==prioridadMayor){
						if(i>bdrMayorPrioridad){
							bdrMayorPrioridad=i;
						}
					}
				}
			}
			
			return bdrMayorPrioridad;
			
		//de lo contrario se elige como BDR al que tenga mayor prioridad o en caso de empate al de mayor id
		//dentro de los declarados BDR
		}else{
			bdrMayorPrioridad=0;
			prioridadMayor=0;
			
			for(Integer i : listaBDRs){
				//se trata diferente si es este router o un vecino
				if(i==contextoI.getRouterID()){
					if(contextoI.getRouterPriority()>prioridadMayor){
						prioridadMayor=contextoI.getRouterPriority();
						bdrMayorPrioridad=i;
					}else if(contextoI.getRouterPriority()==prioridadMayor){
						if(i>bdrMayorPrioridad){
							bdrMayorPrioridad=i;
						}
					}
				}else{
					FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();
					
					if(contextoV.getNeighborPriority()>prioridadMayor){
						prioridadMayor=contextoV.getNeighborPriority();
						bdrMayorPrioridad=i;
					}else if(contextoV.getNeighborPriority()==prioridadMayor){
						if(i>bdrMayorPrioridad){
							bdrMayorPrioridad=i;
						}
					}
				}
			}
			
			return bdrMayorPrioridad;			
			
		}

	}
	
	
	
	public int paso3(List<Integer> listaRoutersElegibles, Map<Integer, FSMMaquinaEstadosOSPFv2Vecino> listOfNeighbouringRouters,
			FSMContextoOSPFv2Interfaz contextoI, int bdrNuevo){
		//Cálculo de nuevo DR		
		//Listar los routers que se declaran DR a sí mismos
		List<Integer> listaDRs = new ArrayList();
		
		for(Integer i : listaRoutersElegibles){
			//se trata diferente si es este router o un vecino
			if(i==contextoI.getRouterID()){
				if(contextoI.getDesignatedRouter()==contextoI.getRouterID())
					listaDRs.add(i);
			}else{
				FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();				
				if(contextoV.getNeighborDesignatedRouter()==contextoV.getNeighborID())
					listaDRs.add(i);
			}							
		}
		
		//si no hay ninguno, se elige como DR al recien elegido BDR
		if (listaDRs.isEmpty()){
			return bdrNuevo;
			
		//de lo contrario se elige al que tenga mayor prioridad o en caso de empate al de mayor id
		}else{
			int drMayorPrioridad=0;
			int prioridadMayor=0;
			
			for(Integer i : listaDRs){
				//se trata diferente si es este router o un vecino
				if(i==contextoI.getRouterID()){
					if(contextoI.getRouterPriority()>prioridadMayor){
						prioridadMayor=contextoI.getRouterPriority();
						drMayorPrioridad=i;
					}else if(contextoI.getRouterPriority()==prioridadMayor){
						if(i>drMayorPrioridad){
							drMayorPrioridad=i;
						}
					}
				}else{
					FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) listOfNeighbouringRouters.get(i).getContexto();
					
					if(contextoV.getNeighborPriority()>prioridadMayor){
						prioridadMayor=contextoV.getNeighborPriority();
						drMayorPrioridad=i;
					}else if(contextoV.getNeighborPriority()==prioridadMayor){
						if(i>drMayorPrioridad){
							drMayorPrioridad=i;
						}
					}
				}
			}
			
			return drMayorPrioridad;			
		}

	}
	
	
	
	
	
	
	
	
	
}


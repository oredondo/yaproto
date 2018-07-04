/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import msti.fsm.FSMContexto;
import msti.fsm.FSMEstado;
import msti.io.ILecturaListener;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.fsmInterfaz.FSMContextoOSPFv2Interfaz;
import msti.ospfv2.fsmInterfaz.FSMEstadoOSPFv2Interfaz;
import msti.ospfv2.mensaje.IMensajeOSPFv2;
import msti.ospfv2.mensaje.MensajeOSPFv2;
import msti.util.Inet4Address;

/**
 * Esta clase recoge los eventos de UpdateRuta(N,D), dirigidos a cualquier N, y los
 * propaga sin cambios a sus observadores que implementan IUpdateRuta.
 * 
 * Su única acción es: 
 *   - si N no existe en la tabla de rutas, invoca la factoría de la máquina de estados para obtener una nueva
 *   - notifica a sus observadores (ya incluye la nueva máquina para la nueva entrada)
 *   
 *   NOTA: No mantiene enlaces a las máquinas creadas, los localiza en la tabla de rutas, por lo cual si la nueva
 *   máquina no llegase a insertar una entrada nueva en la tabla de rutas (p.ej, debido a D>=15) con ella misma 
 *   como referencia asociada, el recolector basura java recogería la máquina. *  
 */
public class FiltroFactoria implements ILecturaListener {

	/**
	 *  Mapa hash para localizar el listener (sólo permite uno) suscrito al evento upDateRuta(N,D) para cada N
	 *  
	 *  La clave es la (direccion de red << 16) + prefijoRed, asociada a la tabla de rutas
	 */
	private Map<Integer, ILecturaListener> mapaVecinoUpdate;
	private IFactoriaFSMMaquinaEstadosOSPFv2Vecino factoriaMaquinaEstadosVecino;
	private FSMContextoOSPFv2Interfaz contextoInterfaz;
	
	public FiltroFactoria(IFactoriaFSMMaquinaEstadosOSPFv2Vecino factoria, FSMContexto contextoInterfaz) {
		mapaVecinoUpdate = new ConcurrentHashMap<Integer, ILecturaListener>(2);
		this.factoriaMaquinaEstadosVecino = factoria;
		this.contextoInterfaz=(FSMContextoOSPFv2Interfaz) contextoInterfaz;
	}
	/**
	 * A�ade un listener de lectura
	 * @param lecturaListener listener
	 * @param direccionRed Se suscribe a los eventos de ruta que tengan esta dirección de red destino
	 */
	
	public void addLecturaListener(ILecturaListener lecturaListener, int idVecino) {		
		mapaVecinoUpdate.put(idVecino, lecturaListener); 
	}
	

	public void removeLecturaListener(ILecturaListener lecturaListener, int idVecino) {	
		mapaVecinoUpdate.remove(idVecino);
	}
	
	/**
	 * Notificar a los listeners de la aparición de un evento update sobre una ruta
	 * @param mensaje  Ruta afectada. Contiene internamente la dirección de red y prefijo de red.
	 */
	public void notificarEventoLectura(Sesion sesion, Lectura lectura) {
		System.out.println(this.getClass().getName() + ": notificarEventoLectura a #" + mapaVecinoUpdate.size());
		((MensajeOSPFv2) lectura.getMensaje()).getRouterID();
		
		/** Si encuentra un listener, notifica.
		 * Si no hay listener, instancia maq. estados vecino (Si el mensaje es Hello, sino se descarta). 
		 */
		ILecturaListener listener = (ILecturaListener) mapaVecinoUpdate.get(((MensajeOSPFv2) lectura.getMensaje()).getRouterID());
		if (listener != null) {
			// notifica evento
			listener.mensajeRecibido(sesion, lectura);
			System.out.println(this.getClass().getName() + ": notificarEventoLectura: existe instancia FSMOSPFv2Vecino: notificada");
		}
		else {
			//Listener no encontrado, si el mensaje es tipo Hello creamos nuevo vecino y notificamos
			if (((MensajeOSPFv2) lectura.getMensaje()).getTipo().equals(IMensajeOSPFv2.Tipo.OSPFHello)) {
			
				System.out.println(this.getClass().getName() + ": notificarEventoLectura: creada instancia FSMOSPFv2Vecino: notificada");
		
				FSMMaquinaEstadosOSPFv2Vecino maquinaEstadosVecino = factoriaMaquinaEstadosVecino.getInstance();
				// Añade en el contexto la referencia al observable de ILectura (la m.e, para que
				//  luego se pueda suscribir
				maquinaEstadosVecino.getContexto().put("FSMEventoLecturaProductor", this);
				
				//A�ade la sesi�n
				//((FSMContextoOSPFv2Vecino)maquinaEstadosVecino.getContexto()).setSesion(sesion);
				
				//A�adir listener al mapa de Vecinos
				this.addLecturaListener(maquinaEstadosVecino,((MensajeOSPFv2) lectura.getMensaje()).getRouterID());
				
				//A�adir la m�quina a la lista de vecinos de la interfaz
				contextoInterfaz.getListOfNeighbouringRouters().put(((MensajeOSPFv2) lectura.getMensaje()).getRouterID(), maquinaEstadosVecino); 
				
				//A�ador contexto de la interfaz a la maquinaEstadosVecino
				((FSMContextoOSPFv2Vecino) maquinaEstadosVecino.getContexto()).setContextoInterfaz(contextoInterfaz);
				
				// Primera transición
				maquinaEstadosVecino.init(maquinaEstadosVecino.getContexto());
				// Máquina síncrona
				maquinaEstadosVecino.setHilo(true);
				Thread hilo = new Thread(maquinaEstadosVecino);
				hilo.start();
							
				// Notifica el evento hacia esta máquina
				maquinaEstadosVecino.mensajeRecibido(sesion, lectura);			
			}
		}
	}
	
	
	
	
	
	@Override
	public void sesionInactiva(Sesion sesion) {
		System.out.println("FiltroFactoria::sesionInactiva()");
	}
	@Override
	public void sesionCerrada(Sesion sesion) {
		System.out.println("FiltroFactoria::sesionCerrada()");
		
	}
	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
				
		//Verificaci�n de IP ya hecha
		
		//Verificaci�n OSPFv2
		if (lectura.getMensaje() instanceof MensajeOSPFv2){
			boolean procesar =true;
			
			//Lo primero comprobamos el checksum para no tener errores con datos corruptos
			if (((MensajeOSPFv2)lectura.getMensaje()).getIsChecksumOK()){
			
				//Versi�n debe ser 2
				if (((MensajeOSPFv2) lectura.getMensaje()).getVersion()!=(byte) 2)
					procesar=false;
				//Comprobar checksum
					//ya comprobado arriba
				//Comprobar AreaID, debe cumplirse uno de estos casos
				if (((MensajeOSPFv2) lectura.getMensaje()).getAreaID()!=contextoInterfaz.getAreaID()){				
					if (((MensajeOSPFv2) lectura.getMensaje()).getAreaID()!=0){
						procesar=false;
					}else{
						//(es el backbone, es decir AreaID=0) Se puede comprobar, viendo si el router es un "area border router"
						//el routerID debe ser el otro extremo de un virtualLink
						//el router debe estar enganchado al "transit area" del virtualLink
					}
				}else{
					//(si es igual que AreaID) Se puede comprobar comparando direcciones con la m�scara de red
				}
				
				//TODO: filtrar por interfaz, procesar si  es AllSPFRouters o si es unicast a la dirIP de la interfaz o
				//Si el destino del datagrama IP es AllDRouters (224.0.0.6), solo debe aceptarse si la interfaz es DR o Backup
				//DireccionLocal en lectura = DireccionDestino del mensaje
				try {
					if (Inet4Address.toInt(((InetSocketAddress)lectura.getDireccionLocal()).getAddress()) == 
							msti.util.Inet4Address.toInt(InetAddress.getByName("224.0.0.6"))){
						FSMEstado estadoActualInterfaz = (FSMEstado) contextoInterfaz.getMaquinaEstados().getEstadoActivo();
						if(!(estadoActualInterfaz.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.DR) ||
							estadoActualInterfaz.getId().equals(FSMEstadoOSPFv2Interfaz.FSMIdEstadoOSPFv2Interfaz.BACKUP)))
								procesar=false;
					}			
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.out.println("FiltroFactoria: error al traducir InetAddress");				
				}	
				//Comprobar AuType
				if (((MensajeOSPFv2) lectura.getMensaje()).getAutype()!=contextoInterfaz.getAuType())
					procesar=false;					
				//Autenticaci�n
				if (((MensajeOSPFv2) lectura.getMensaje()).getAutype()==1){	//(simple password)
					if (((MensajeOSPFv2) lectura.getMensaje()).getAuthentication()!=contextoInterfaz.getAuthenticationKey())
						procesar=false;		
				}
				
				
				
				//procesar mensaje
				if (procesar)
					notificarEventoLectura(sesion, lectura);
			}
		}	
		
		return true;
	}
	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		
	}

}

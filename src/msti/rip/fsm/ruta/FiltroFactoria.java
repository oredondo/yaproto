/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import msti.rip.TablaRutas;
import msti.rip.fsm.IFSMEventoUpdateRutaListener;
import msti.rip.mensaje.IMensajeRIPRuta;

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
public class FiltroFactoria implements IFSMEventoUpdateRutaListener {

	/**
	 *  Mapa hash para localizar el listener (sólo permite uno) suscrito al evento upDateRuta(N,D) para cada N
	 *  
	 *  La clave es la (direccion de red << 16) + prefijoRed, asociada a la tabla de rutas
	 */
	private Map<String, IFSMEventoUpdateRutaListener> mapaRutaUpdate;

	private IFactoriaFSMMaquinaEstadosRIPRuta factoriaMaquinaEstadosRuta;
	
	public FiltroFactoria(IFactoriaFSMMaquinaEstadosRIPRuta factoria) {
		mapaRutaUpdate = new ConcurrentHashMap<String, IFSMEventoUpdateRutaListener>(2);
		this.factoriaMaquinaEstadosRuta = factoria;
	}
	/**
	 * Anade un listener de los eventos update de ruta
	 * @param updateRutaListener listener
	 * @param direccionRed Se suscribe a los eventos de ruta que tengan esta dirección de red destino
	 */
	public void addRutaUpdateListener(IFSMEventoUpdateRutaListener updateRutaListener, 
			InetAddress direccionRed, int prefijoRed) {
		String clave = TablaRutas.generarClaveTablaRutas(direccionRed, prefijoRed);

		mapaRutaUpdate.put(clave, updateRutaListener); 
	}

	public void removeLecturaListener(IFSMEventoUpdateRutaListener updateRutaListener, 
			InetAddress direccionRed, int prefijoRed) {
		String clave = TablaRutas.generarClaveTablaRutas(direccionRed, prefijoRed);
		
		mapaRutaUpdate.remove(clave);
	}
	
	/**
	 * Notificar a los listeners de la aparición de un evento update sobre una ruta
	 * @param mensaje  Ruta afectada. Contiene internamente la dirección de red y prefijo de red.
	 */
	public void notificarEventoUpdateRuta(IMensajeRIPRuta mensaje) {
		System.out.println(this.getClass().getName() + ": notificarEventoUpdateRuta a #" + mapaRutaUpdate.size());
		String clave = TablaRutas.generarClaveTablaRutas(mensaje.getDireccionIP(), mensaje.getLongitudPrefijoRed());

		/** Si encuentra un listener, notifica. Si no hay listener, instancia maq. estados ruta. 
		 * Esta máquina se suscribirá ella misma, si llega a hacerlo, y se almacenará asociada a la ruta
		 */
		IFSMEventoUpdateRutaListener listener = (IFSMEventoUpdateRutaListener) mapaRutaUpdate.get(clave);
		if (listener != null) {
			// notifica evento
			listener.updateRuta(mensaje);
			System.out.println(this.getClass().getName() + ": notificarEventoUpdateRuta: existe instancia FSMRIPRuta: notificada");
		}
		else {
			System.out.println(this.getClass().getName() + ": notificarEventoUpdateRuta: creada instancia FSMRIPRuta: notificada");

			FSMMaquinaEstadosRIPRuta maquinaEstadosRuta = factoriaMaquinaEstadosRuta.getInstance();
			// Anade en el contexto la referencia al observable de IUpdateRuta (la m.e, para que
			//  luego se pueda suscribir
			maquinaEstadosRuta.getContexto().put("FSMEventoUpdateRutaProductor", this);
			// Máquina síncrona
			maquinaEstadosRuta.setHilo(false); // No se va a ejecutar como hilo aparte
			// Primera transición
			maquinaEstadosRuta.init(maquinaEstadosRuta.getContexto());
			// Notifica el evento hacia esta máquina
			maquinaEstadosRuta.updateRuta(mensaje); //TODO: falta informar de interfaz de entrada (para split horizon)
			// TODO: máquina debe ser síncrona, se debe haber almacenado en la tabla de rutas, si ha anadido la ruta
			// si la máquina fuera un hilo, habría concurrencia con recolector basura java
			//suscribir máquina de estados...? o pasar este observable y que se suscriba ella si quiere
			// pero si no guardamos la referencia, hasta que ella se guarde al anadir la entrada... el 
			// recolector podría eliminarla
		}

	}
	
	/** IFSMEventoUpdateRutaListener */
	@Override
	public void updateRuta(IMensajeRIPRuta mensaje) {
		notificarEventoUpdateRuta(mensaje);
	}

}

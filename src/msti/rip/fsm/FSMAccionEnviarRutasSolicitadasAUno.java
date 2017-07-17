/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import com.savarese.rocksaw.net.RawSocket;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.Lectura;
import msti.io.SesionDatagrama;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.Ruta;
import msti.rip.mensaje.IMensajeRIPRuta;
import msti.rip.mensaje.MensajeRIPPeticion;
import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMAccionEnviarRutasSolicitadasAUno implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarRutasSolicitadasAUno _instancia = new FSMAccionEnviarRutasSolicitadasAUno();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	
	public FSMAccionEnviarRutasSolicitadasAUno() {
	}

	private static MensajeRIPRuta ruta2MensajeRIPRuta(Ruta ruta) {

		MensajeRIPRuta.Builder mensajeRuta = MensajeRIPRuta.Builder.crear();
		mensajeRuta.setIdFamiliaDirecciones((short) RawSocket.PF_INET);

		synchronized (ruta) {	
			mensajeRuta.setDireccionIP(ruta.destino);
			mensajeRuta.setLongitudPrefijoRed(ruta.prefijoRed);
			mensajeRuta.setDireccionProximoSalto(ruta.proximoSalto);
			mensajeRuta.setMetrica(ruta.distancia);
		}
		return mensajeRuta.build();
		
	}
	public static MensajeRIPRespuesta generarMensajeRIPRespuesta(TablaRutas tablaRutas, 
			MensajeRIPPeticion peticion) {
		MensajeRIPRespuesta.Builder respuesta = MensajeRIPRespuesta.Builder.crear() ;

		if (tablaRutas == null)
			throw new IllegalArgumentException("Tabla de rutas null");

		if (peticion.esPeticionTablaCompleta()) {
			System.out.println("FSMRIP:AccionEnviarRutasSolicitadasAUno(): esTablaCompleta!!");
			// Toda la tabla
			// TODO: hay que aplicar split-horizon, split-horizon/poison-reverse en base a interfaz
			if (! tablaRutas.isEmpty()) 
				for (Ruta ruta: tablaRutas)
					respuesta.addRIPRuta(ruta2MensajeRIPRuta(ruta));
		}
		else {
			// Sólo copia las solicitadas
			if (peticion.hasRIPRutas())
				for (IMensajeRIPRuta peticionRuta: peticion.getRIPRutas() ) {
					String clave = TablaRutas.generarClaveTablaRutas(peticionRuta.getDireccionIP(), 
							peticionRuta.getLongitudPrefijoRed());
					Ruta ruta = tablaRutas.getRuta(clave);

					/* Si no existe la ruta no se envía respuesta respecto a la misma*/
					if (ruta != null) {
						// En las peticiones no se aplica split horizon (se supone que son diagnóstico)
						respuesta.addRIPRuta(ruta2MensajeRIPRuta(ruta));
					}
				}
		}
		respuesta.setVersion((byte) 2);
		return respuesta.build();
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		SesionDatagrama sesion = (SesionDatagrama) contexto.get("SesionRIP");  //TODO: una por interfaz
		Lectura lectura = (Lectura) o;
		MensajeRIPPeticion peticion = (MensajeRIPPeticion) lectura.getMensaje();
		MensajeRIPRespuesta respuesta;

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");
		
		/* Generar respuesta */
		respuesta = generarMensajeRIPRespuesta(tablaRutas, peticion);
		System.out.println("FSMRIP:AccionEnviarRutasSolicitadasAUno(): rellena respuesta con " + (respuesta.hasRIPRutas() ? respuesta.getRIPRutas().size() : 0 ) + " rutas.");

		/* siempre lo transmite hacia el destino que lo ha recibido */
		Escritura escritura = new Escritura(respuesta);
		escritura.setDireccionDestino(lectura.getDireccionOrigen());
		sesion.escribir(escritura);
	}

}

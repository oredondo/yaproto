/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.net.InetSocketAddress;
import java.util.Iterator;

import com.savarese.rocksaw.net.RawSocket;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.SesionDatagrama;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.EstadoRuta;
import msti.rip.TablaRutas.Ruta;
import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMAccionEnviarRutasModificadasATodos implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarRutasModificadasATodos _instancia = new FSMAccionEnviarRutasModificadasATodos();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	
	public FSMAccionEnviarRutasModificadasATodos() {
	}

	public static MensajeRIPRespuesta generarMensajeRIPRespuesta(FSMContexto contexto) {
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		Ruta ruta;
		MensajeRIPRespuesta.Builder respuesta = MensajeRIPRespuesta.Builder.crear();
		Iterator<Ruta> iterador;
		
		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");

		iterador = tablaRutas.iteratorRutaModificada();
		contexto.put("IteradorRutasModificadas", iterador);
		while (iterador.hasNext()) {
			   ruta = (Ruta) iterador.next();

			   synchronized (ruta) {
				   MensajeRIPRuta.Builder mensajeRuta = MensajeRIPRuta.Builder.crear();

				   if (ruta.estado != EstadoRuta.NO_MODIFICADA) {
					   //TODO: aplicar split-horizon, o split-horizon con poison reverse
					   // y una lista diferente para cada interfaz de red
					   mensajeRuta.setIdFamiliaDirecciones((short) RawSocket.PF_INET);
					   mensajeRuta.setEtiquetaRuta((short) 0);
					   mensajeRuta.setDireccionIP(ruta.destino);
					   mensajeRuta.setLongitudPrefijoRed(ruta.prefijoRed);
					   mensajeRuta.setDireccionProximoSalto(ruta.proximoSalto);
					   mensajeRuta.setMetrica(ruta.distancia);
					   
					   respuesta.addRIPRuta(mensajeRuta.build());
				   }
			   }
		}
		respuesta.setVersion((byte) 2);
		return respuesta.build();		
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		SesionDatagrama sesion = (SesionDatagrama) contexto.get("SesionRIP");  //TODO: una sesión por interfaz
		MensajeRIPRespuesta mensaje = generarMensajeRIPRespuesta(contexto);

		/* si hay modificadas, lo transmite */
		if (mensaje.hasRIPRutas() && (! mensaje.getRIPRutas().isEmpty())) {

			Escritura escritura = new Escritura(mensaje);  //TODO: una por interfaz de salida suscrita a rip, con mensaje diferente
			escritura.setDireccionDestino(new InetSocketAddress("224.0.0.9", 520)); //TODO: definir cte RIP_MULTICAST */
			sesion.escribir(escritura);
		}
	}

}
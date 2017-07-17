/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.net.InetSocketAddress;

import com.savarese.rocksaw.net.RawSocket;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.io.Escritura;
import msti.io.Lectura;
import msti.io.Sesion;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.Ruta;
import msti.rip.mensaje.MensajeRIPRespuesta;
import msti.rip.mensaje.MensajeRIPRuta;

public class FSMAccionEnviarTablaCompletaATodos implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionEnviarTablaCompletaATodos _instancia = new FSMAccionEnviarTablaCompletaATodos();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	public static MensajeRIPRespuesta generarMensajeRIPRespuesta(TablaRutas tablaRutas) {
		MensajeRIPRespuesta.Builder respuesta = MensajeRIPRespuesta.Builder.crear() ;
		
		for (Ruta ruta: tablaRutas) {
			   synchronized (ruta) {
				   MensajeRIPRuta.Builder mensajeRuta = MensajeRIPRuta.Builder.crear();

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
		respuesta.setVersion((byte) 2);
		return respuesta.build();
		
	}

	public FSMAccionEnviarTablaCompletaATodos() {
	}

	@Override
	public void execute(FSMContexto contexto, Object o) {
		MensajeRIPRespuesta respuesta;
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		Sesion sesionRIP = (Sesion) contexto.get("SesionRIP");

		if (tablaRutas == null)
			throw new IllegalArgumentException("Objeto con clave 'TablaRutas' no existente en el contexto.");

		respuesta = generarMensajeRIPRespuesta(tablaRutas);
		
		/* si hay alguna ruta en la tabla, lo transmite */
		if (respuesta.hasRIPRutas() && (! respuesta.getRIPRutas().isEmpty())) {
			System.out.println("AccionEnviarTablaCompletaATodos: anuncia " + respuesta.getRIPRutas().size() + " rutas.");
			Escritura escritura = new Escritura(respuesta);
			escritura.setDireccionDestino(new InetSocketAddress("224.0.0.9", 520)); //TODO configurar dirección
			sesionRIP.escribir(escritura);
		}
		else 
			System.out.println("AccionEnviarTablaCompletaATodos: no existen rutas.");
	}

}

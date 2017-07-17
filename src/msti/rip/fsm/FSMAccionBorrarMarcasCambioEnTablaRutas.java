/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.util.Iterator;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;
import msti.rip.TablaRutas;
import msti.rip.TablaRutas.EstadoRuta;
import msti.rip.TablaRutas.Ruta;
import msti.rip.mensaje.MensajeRIPRespuesta;

public class FSMAccionBorrarMarcasCambioEnTablaRutas implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionBorrarMarcasCambioEnTablaRutas _instancia = new FSMAccionBorrarMarcasCambioEnTablaRutas();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionBorrarMarcasCambioEnTablaRutas() {
	}

	/** 
	 * Borra las marcas de cambio.
	 * @param contexto
	 * @param iterador
	 * @return Devuelve true si ha realizado algún cambio en la tabla de rutas
	 */
	public static boolean borrarMarcasCambioEnTablaRutas(FSMContexto contexto, Iterator<Ruta> iterador) {
		boolean hayModificados = false;
		Ruta ruta;
		
		/* Itera los modificados y establece estado no modificado */
		while (iterador.hasNext()) {
			ruta = (Ruta) iterador.next();
			hayModificados = true;
	
			synchronized (ruta) {
				ruta.estado = EstadoRuta.NO_MODIFICADA;
			}
		}
		return hayModificados;
	}
	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		Iterator<Ruta> iterador = (Iterator<Ruta>) contexto.get("IteradorRutasModificadas");
		TablaRutas tablaRutas = (TablaRutas) contexto.get("TablaRutas");
		
		/* Por defecto, obtiene el iterador del entorno (búsqueda anterior), para que no haya cambios entre
		 * la lista difundida antes y los que se marcan como no modificados. Si no, lo crea */
		if (iterador == null) {
			iterador = tablaRutas.iteratorRutaModificada();
		}
		/* clear de cambios sin difundir en la tabla de rutas */
		if (borrarMarcasCambioEnTablaRutas(contexto, iterador))
			tablaRutas.setTablaModificada();
	}

}
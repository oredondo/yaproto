/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.util.Calendar;
import java.util.GregorianCalendar;

import msti.fsm.FSMAccion;
import msti.fsm.FSMContexto;

//import msti.rip.mensaje.MensajeRIPRespuesta;

public class FSMAccionIncrementarDDSequenceNumberDelVecino implements FSMAccion {
	/** La instancia singleton */
	protected static FSMAccionIncrementarDDSequenceNumberDelVecino _instancia = new FSMAccionIncrementarDDSequenceNumberDelVecino();

	/**
	 * Devuelve una instancia. Resuelve internamente posibles problemas de concurrencia en la instanciación
	 * @return Instancia
	 */
	public static FSMAccion getInstance() {
		return _instancia;
	}

	protected FSMAccionIncrementarDDSequenceNumberDelVecino() {
	}

	
	@Override
	public void execute(FSMContexto contexto, Object o) {
		FSMContextoOSPFv2Vecino contextoV=(FSMContextoOSPFv2Vecino) contexto;
		
		int nuevoDDSequenceNumber;

		//Si es la primera vez que se intenta crea la adjacencia el DDSequenceNumber lo sacamos de la hora actual del reloj del sistema
		if(contextoV.getDdSequenceNumber()== -1){
			
			Calendar calendario = Calendar.getInstance();
			calendario = new GregorianCalendar();
			nuevoDDSequenceNumber= calendario.get(Calendar.MILLISECOND) + 1000*calendario.get(Calendar.SECOND)
					+ 100000*calendario.get(Calendar.MINUTE) + 10000000*calendario.get(Calendar.HOUR_OF_DAY);
			contextoV.setDdSequenceNumber(nuevoDDSequenceNumber);
			
		}else{
			nuevoDDSequenceNumber= contextoV.getDdSequenceNumber() + 1;
			contextoV.setDdSequenceNumber(nuevoDDSequenceNumber);
		}
		
		
		
	}

}
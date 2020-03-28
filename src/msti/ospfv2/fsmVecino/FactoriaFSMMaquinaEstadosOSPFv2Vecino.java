/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import msti.fsm.FSMContexto;
import msti.io.FiltroNotificador;
import msti.netlink.fsm.client.INetlinkOrden;
import msti.ospfv2.TablaRutas;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;


public class FactoriaFSMMaquinaEstadosOSPFv2Vecino implements IFactoriaFSMMaquinaEstadosOSPFv2Vecino {

	private TablaRutas tablaRutas;
	private INetlinkOrden tablaForwarding;

	public FactoriaFSMMaquinaEstadosOSPFv2Vecino(TablaRutas tablaRutas, INetlinkOrden netlinkOrden) {
		this.tablaRutas = tablaRutas;
		this.tablaForwarding = netlinkOrden;
		if ((tablaRutas == null) || (tablaForwarding == null)) 
			throw new IllegalArgumentException("tablaRutas o tablaForwarding son null");
	}
	@Override
	public FSMMaquinaEstadosOSPFv2Vecino getInstance() {
		FSMContexto contexto = new FSMContextoOSPFv2Vecino();

		contexto.put("TablaRutas", tablaRutas);
		contexto.put("TablaForwarding", tablaForwarding);
				
		return new FSMMaquinaEstadosOSPFv2Vecino(contexto);
	}


	
}

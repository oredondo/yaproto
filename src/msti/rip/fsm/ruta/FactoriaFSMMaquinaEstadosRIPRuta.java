/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMContexto;
import msti.netlink.fsm.client.INetlinkOrden;
import msti.rip.TablaRutas;

public class FactoriaFSMMaquinaEstadosRIPRuta implements IFactoriaFSMMaquinaEstadosRIPRuta {

	private TablaRutas tablaRutas;
	private INetlinkOrden tablaForwarding;

	public FactoriaFSMMaquinaEstadosRIPRuta(TablaRutas tablaRutas, INetlinkOrden netlinkOrden) {
		this.tablaRutas = tablaRutas;
		this.tablaForwarding = netlinkOrden;
		if ((tablaRutas == null) || (tablaForwarding == null)) 
			throw new IllegalArgumentException("tablaRutas o tablaForwarding son null");
	}
	@Override
	public FSMMaquinaEstadosRIPRuta getInstance() {
		FSMContexto contexto = new FSMContexto();

		contexto.put("TablaRutas", tablaRutas);
		contexto.put("TablaForwarding", tablaForwarding);
		return new FSMMaquinaEstadosRIPRuta(contexto);
	}

}

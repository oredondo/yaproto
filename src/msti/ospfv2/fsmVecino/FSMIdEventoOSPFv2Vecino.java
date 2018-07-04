/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmVecino;

import msti.fsm.FSMEvento.FSMIdEvento;

public enum FSMIdEventoOSPFv2Vecino implements FSMIdEvento {
		KILLNBR,
		INACTIVITYTIMER,
		LLDOWN,
		START,
		HELLORECEIVED,
		TWOWAYRECEIVED,
		ONEWAYRECEIVED,
		SEQNUMBERMISMATCH,
		BADLSREQ,
		NEGOTIATIONDONE,
		EXCHANGEDONE,
		LOADINGDONE,
		ADJOK,
		DDPCONIMMSTIMER;
}

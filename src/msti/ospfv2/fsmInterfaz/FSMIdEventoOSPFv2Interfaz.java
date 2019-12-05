/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.ospfv2.fsmInterfaz;

import msti.fsm.FSMEvento.FSMIdEvento;

public enum FSMIdEventoOSPFv2Interfaz implements FSMIdEvento {
		LOOPIND,
		UNLOOPIND,
		INTERFACEDOWN,
		INTERFACEUP,
		WAITTIMER,
		BACKUPSEEN,
		NEIGHBORCHANGE,
		HELLOTIMER;
}

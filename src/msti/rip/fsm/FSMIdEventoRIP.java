/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import msti.fsm.FSMEvento.FSMIdEvento;

public enum FSMIdEventoRIP implements FSMIdEvento {
		PETICION,
		RESPUESTA,
		TABLARUTASCAMBIADA,
		TEMPORIZADORDIFUSIONPERIODICA,
		TEMPORIZADORTU;
}

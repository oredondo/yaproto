/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm.ruta;

import msti.fsm.FSMEvento.FSMIdEvento;

public enum FSMIdEventoRIPRuta implements FSMIdEvento {
		ACTUALIZACIONRUTA,
		TEMPORIZADORRUTAEXPIRADA,
		TEMPORIZADORRUTAELIMINAR;
}

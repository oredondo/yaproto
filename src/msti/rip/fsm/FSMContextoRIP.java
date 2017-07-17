/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip.fsm;

import java.util.Timer;

import msti.fsm.FSMContexto;

public class FSMContextoRIP extends FSMContexto {

	/* Temporizador 30s y marca de activado */
	private Timer temporizador30s;
	private boolean temporizador30sActivo = false;
	
	public FSMContextoRIP() {
	}

	public void setTemporizador30s(Timer temporizador30s) {
		this.temporizador30s = temporizador30s;
	}

	public Timer getTemporizador30s() {
		return temporizador30s;
	}

	public void setTemporizador30sActivo(boolean temporizador30sActivo) {
		this.temporizador30sActivo = temporizador30sActivo;
	}

	public boolean isTemporizador30sActivo() {
		return temporizador30sActivo;
	}

	
}

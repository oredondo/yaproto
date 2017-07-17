/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.util;

import java.util.EventListener;

public interface ITimerListener extends EventListener {

	/** Se ejecuta cuando el Timer indicado expira */
	public void expiredTimer(TimerEventProducer producer);
}

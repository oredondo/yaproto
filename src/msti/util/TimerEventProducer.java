/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.util;

public class TimerEventProducer implements Runnable {

	String id;
	ITimerListener listener;
	
	public TimerEventProducer(String id, ITimerListener listener) {
		this.id = id;
		this.listener = listener;
	}
	
	public String getId() {
		return this.id;
	}

	@Override
	public void run() {
		System.out.println("TimerEventProducer: id=" + getId() + " expira.");
		listener.expiredTimer(this);
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

public class SesionConfiguracion {

	protected int maxRcvdBytes;
	protected int maxSndBytes;

	public synchronized void setMaxRcvdBytes(int maxRcvdBytes) {
		this.maxRcvdBytes = maxRcvdBytes;
	}

	public synchronized int getMaxRcvdBytes() {
		// TODO Auto-generated method stub
		return maxRcvdBytes;
	}

	public synchronized void setMaxSndBytes(int maxSndBytes) {
		this.maxSndBytes = maxSndBytes;
	}

	public synchronized int getMaxSndBytes() {
		// TODO Auto-generated method stub
		return maxSndBytes;
	}

}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttCliente {

	private MqttClient mqtt;

	public MqttCliente(String host) {
			try {
						MemoryPersistence persistence = new MemoryPersistence();
						String url;
						url = "tcp://" + host;
						mqtt = new MqttClient(url, "mqttloger", persistence);
						MqttConnectOptions connOpts = new MqttConnectOptions();
						connOpts.setCleanSession(true);
						mqtt.connect(connOpts);
					} catch (MqttException e) {
							e.printStackTrace();
				}

	     }
  public MqttClient getMqtt(){
    return mqtt;
  }
}

/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.paho.client.mqttv3.MqttClient;


public class FSMContexto {

	/** Id contexto */
	private final static AtomicInteger generadorId = new AtomicInteger(0);
	private final int id;

	/** Máquina de estados */
	private FSMMaquinaEstados maquinaEstados;
	public MqttClient client;

	/** Objetos para acciones, etc. */
	private Map<String, Object> map;

	public FSMContexto() {
		map = new HashMap<String, Object>();
		id = generadorId.addAndGet(1);
	}

	public int getId() {
		return this.id;
	}

	public void setMaquinaEstados(FSMMaquinaEstados maquinaEstados) {
		this.maquinaEstados = maquinaEstados;
	}

	public FSMMaquinaEstados getMaquinaEstados() {
		return maquinaEstados;
	}
	public void setMqttClient(MqttClient client) {
		this.client = client;
	}

	public MqttClient getMqttClient() {
		return client;
	}

	/**
	 * Introduce un objeto en el mapa
	 * @param s
	 * @param o
	 */
	public void put(String s, Object o) {
		map.put(s, o);
	}

	/**
	 * Extrae un objeto del mapa
	 * @param s
	 * @return
	 */
	public Object get(String s) {
		return map.get(s);
	}
}

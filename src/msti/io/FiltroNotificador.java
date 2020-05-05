/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class FiltroNotificador extends Filtro implements ILecturaListener,IEscrituraListener {
	private MqttClient logger;
	// Mapa hash para localizar los listeners suscritos a cada idSesion->CopyOnWriteArrayList<ILecturaListener>
	Map<Long, List<ILecturaListener>> mapaLectura;
	// Mapa hash para localizar los listeners suscritos a cada idSesion->CopyOnWriteArrayList<ILecturaListener>
	Map<Long, List<IEscrituraListener>> mapaEscritura;
	// Map� hash para localizar los listeners suscritos a la creaci�n de sesiones
	CopyOnWriteArrayList<ISesionCreadaListener> alSesionCreada;

	private MqttMessage message;
	private MqttMessage message1;
	private MqttMessage message2;

	public FiltroNotificador(String nombre, MqttClient mqtt) {
		super(nombre);
		logger = mqtt;
		mapaLectura = new ConcurrentHashMap<Long, List<ILecturaListener>>();
		mapaEscritura = new ConcurrentHashMap<Long, List<IEscrituraListener>>();
		alSesionCreada = new CopyOnWriteArrayList<ISesionCreadaListener>();
	}

	public void addLecturaListener(ILecturaListener lecturaListener, long idSesion)
	{
		List<ILecturaListener> list;

		list = mapaLectura.get(new Long(idSesion));
		if (list == null) {
			list = new CopyOnWriteArrayList<ILecturaListener>();
			list.add(lecturaListener);
			mapaLectura.put(new Long(idSesion), list);
		}
		else {
			list.add(lecturaListener);
		}
	}

	public void removeLecturaListener(ILecturaListener lecturaListener, long idSesion)
	{
		List<ILecturaListener> list;

		list = mapaLectura.get(new Long(idSesion));
		if (list != null) {
			list.remove(lecturaListener);
			if (list.isEmpty())
				mapaLectura.remove(new Long(idSesion));  // elimina entrada para esta sesi�n
		}
	}

	public void addEscrituraListener(IEscrituraListener escrituraListener, long idSesion)
	{
		List<IEscrituraListener> list;

		list = mapaEscritura.get(new Long(idSesion));
		if (list == null) {
			list = new CopyOnWriteArrayList<IEscrituraListener>();
			list.add(escrituraListener);
			mapaEscritura.put(new Long(idSesion), list);
		}
		else {
			list.add(escrituraListener);
		}
	}

	public void removeEscrituraListener(IEscrituraListener escrituraListener, long idSesion)
	{
		List<IEscrituraListener> list;

		list = mapaEscritura.get(new Long(idSesion));
		if (list != null) {
			list.remove(escrituraListener);  //podr�a no estar, de ah� no borrar el mapa directamente
			if (list.isEmpty())
				mapaEscritura.remove(new Long(idSesion));  // elimina listener y array, pues no hay listeners para la sesi�n
		}
	}

	public void addSesionCreadaListener(ISesionCreadaListener sesionCreadaListener) {
		alSesionCreada.add(sesionCreadaListener);
	}

	public void removeSesionCreadaListener(ISesionCreadaListener sesionCreadaListener) {
		alSesionCreada.remove(sesionCreadaListener);
	}

	/* IFiltro */

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/* ILecturaListener */

	@Override
	public void sesionCreada(Sesion sesion) {
		System.out.println("FiltroNotificador: sesionCreada(): notificada a " + alSesionCreada.size());
		try {
      message = new MqttMessage(("FiltroNotificador: sesionCreada(): notificada a " + alSesionCreada.size()).getBytes());
      logger.publish("Notificador", message);
    } catch (MqttException ex) {
          ex.printStackTrace();
    }
		if (! alSesionCreada.isEmpty())
			for (ISesionCreadaListener sesionCreadaListener: alSesionCreada)
				sesionCreadaListener.sesionCreada(sesion);
	}

	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		System.out.println("FiltroNotificador: mensajeRecibido() idLectura= " + lectura.getId());
		try {
      message1 = new MqttMessage(("FiltroNotificador: mensajeRecibido() idLectura= " + lectura.getId()).getBytes());
      logger.publish("Notificador", message1);
    } catch (MqttException ex) {
          ex.printStackTrace();
    }
		if (! mapaLectura.isEmpty())
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (ILecturaListener lecturaListener: mapaLectura.get(new Long(sesion.idSesion))) {
				lecturaListener.mensajeRecibido(sesion, lectura);
				System.out.println("Filtro notificador: mensajeRecibido notificado.");
				try {
          message2 = new MqttMessage(("Filtro notificador: mensajeRecibido notificado.").getBytes());
          logger.publish("Notificador", message2);
        } catch (MqttException ex) {
              ex.printStackTrace();
        }
			}
		return true;
	}

	@Override
	public void sesionInactiva(Sesion sesion) {
		if (! mapaLectura.isEmpty())
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (ILecturaListener lecturaListener: mapaLectura.get(new Long(sesion.idSesion)))
				lecturaListener.sesionInactiva(sesion);
	}

	@Override
	public void sesionCerrada(Sesion sesion) {
		if (! mapaLectura.isEmpty())
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (ILecturaListener lecturaListener: mapaLectura.get(new Long(sesion.idSesion)))
				lecturaListener.sesionCerrada(sesion);
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Lectura lectura, Throwable e) {
		if (! mapaLectura.isEmpty())
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (ILecturaListener lecturaListener: mapaLectura.get(new Long(sesion.idSesion)))
				lecturaListener.excepcionCapturada(sesion, lectura, e);
	}


	/* IEscrituraListener */
	@Override
	public void escrituraFinalizada(Sesion sesion, Escritura escritura) {
		if (! mapaEscritura.isEmpty())
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (IEscrituraListener escrituraListener: mapaEscritura.get(new Long(sesion.idSesion)))
				escrituraListener.escrituraFinalizada(sesion, escritura);
	}

	@Override
	public void excepcionCapturada(Sesion sesion, Escritura escritura, Throwable e) {
		if (! mapaEscritura.isEmpty())
			// El iterador es un copyonwrite, por lo que no hay problema con suscripciones concurrentes de nuevos listener en la misma sesion
			for (IEscrituraListener escrituraListener: mapaEscritura.get(new Long(sesion.idSesion)))
				escrituraListener.excepcionCapturada(sesion, escritura, e);
	}

	@Override
	public int getMaxInputBytes() {
		return 0;
	}

}

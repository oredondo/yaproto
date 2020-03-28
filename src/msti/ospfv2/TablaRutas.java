package msti.ospfv2;

import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import msti.rip.fsm.ruta.FSMMaquinaEstadosRIPRuta;
import msti.util.ITimerListener;

import msti.util.TimerEventProducer;

/*
 * Tabla de rutas OSPF
 * 
 * (no confundir con la tabla que se utiliza para el forwarding)
 */
public class TablaRutas implements ITimerListener, Iterable<TablaRutas.Ruta> {


	public enum EstadoRuta {
		CREADA,
		MODIFICADA,
		BORRADA,
		NO_MODIFICADA;
	};
	public enum TipoRuta {
		ESTATICA, /* configurados por administrador: no se alteran con OSPF */
		SISTEMA,  /* otros procedentes de otros protocolos: no se alteran con OSPF */
		RIP,	  /* Aprendida con RIP */
		OSPF;	  /* Aprendida con OSPF */
	};
	public class Ruta {
		/* Red destino */
		public InetAddress destino;
		public int prefijoRed;

		/* Dirección origen */
		public InetAddress origen;

		/* Próximo salto */
		public InetAddress proximoSalto = null;
		public int indiceInterfazSalida;
		public int distancia;
		public int metrica; /* distancia administrativa */
		
		/* Clasificación de la ruta */
		public TipoRuta tipo = TipoRuta.OSPF;
		public int flags;


		/* Router que nos la comunicó o red desde donde entró*/
		public InetAddress aprendidaDesde;
		public int indiceInterfazEntrada;
		/* Id tabla de rutas donde está el original de la ruta */
		public int tablaRutas;
		
		/* Datos internos sobre la ruta */
		public Date tiempoActiva;
		public EstadoRuta estado;  /* estado interno de la ruta: modificada, borrada, etc. */
		
		/* RIP */
		public FSMMaquinaEstadosRIPRuta maquinaEstados; //TODO: podría ser sólo el contexto de la máquina
		public ScheduledFuture<TimerEventProducer> temporizador; /* temporizador asociado a la ruta (para expiración, eliminación */
		
		/* OSPF */
		public int areaID=1;
	}		
	
	
	/* En las actualizaciones internas, espera este tipo para que no salte el evento en el primer add, teniendo
	 * en cuenta que se procesa cada actualización de ruta de forma independiente.
	 */
	public final static long RETARDO_ACUMULACION = 500; //ms

	/* Id de la tabla de rutas */
	int id;
	
	/* Nombre de la tabla de rutas */
	String nombre;

	/* Mapa para acceso rápido */
	private Map<String, Ruta> mapaRutas;
	/* Lista de rutas para acceso como colección */
	private List<Ruta> listaRutas;
	
	// Pool de hilos temporizadores
	ScheduledThreadPoolExecutor stpe;
	/* Tarea programada para retardo de acumulación en cambios en la tabla de rutas */
	ScheduledFuture tareaTemporizada;
	// Mapa hash para localizar los listeners suscritos a la modificación de rtuas
	CopyOnWriteArrayList<ITablaRutasModificadaListener> listeners;

	public TablaRutas() {
		mapaRutas = new ConcurrentHashMap<String, Ruta>(2); // máx 2 escritores y N lectores
		listaRutas = new CopyOnWriteArrayList<Ruta>();
		
		listeners = new CopyOnWriteArrayList<ITablaRutasModificadaListener>();
		
		// pool de temporizadores (podría recibirlo de forma externa y compartirlo con otros
		stpe = new ScheduledThreadPoolExecutor(1);
	}
	
	public static String generarClaveTablaRutas(InetAddress direccionRed, int prefijoRed) {
		StringBuilder sb = new StringBuilder();
		sb.append(direccionRed); // TODO: falta por seguridad aplicar máscara prefijo red
		sb.append("/");
		sb.append(prefijoRed);  
		return sb.toString();
	}

	/* Patrón observable */
	public void addTablaRutasModificadaListener (ITablaRutasModificadaListener listener) {
		listeners.add(listener);
	}

	public void removeTablaRutasModificadaListener (ITablaRutasModificadaListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifica a los listeners de una modificación
	 * @param milisegundosRetardo. Si se pide retardo 0 la notificación es inmediata. Si es un número mayor 
	 * que 0 se realiza la notificación transcurrida ese tiempo.
	 * 
	 * Si entretanto se espera un retardo sucede alguna otra notificación con tiempo superior a cero, la 
	 * ignora y no cancela la notificación retardada pendiente. 
	 */
	public void notificarEventoTablaRutasModificada(long milisegundosRetardo) {
		// Si piden retardo, se habilita temporizador
		if (milisegundosRetardo > 0) {
			// Sólo programa tarea si no está ya temporizada una anterior.
			if ((tareaTemporizada == null) || tareaTemporizada.isCancelled() || tareaTemporizada.isDone()) 
				tareaTemporizada = this.stpe.schedule(
						new TimerEventProducer("TemporizadorTablaRutas", this), 
						500, TimeUnit.MILLISECONDS);
		}
		else if (milisegundosRetardo == 0) {
			// Si estaba activada actualización retardada, la desactiva
			if (tareaTemporizada != null) {
				tareaTemporizada.cancel(false);
				tareaTemporizada = null;
			}
			for (ITablaRutasModificadaListener listener: listeners)
				listener.tablaRutasModificada(this.id);
		}
		else
			throw new IllegalArgumentException("número de milisegundos debe ser mayor o igual que 0");
	}

	/**
	 * Devuelve la referencia al pool de hilos temporizadores que usa la tabla de rutas
	 * @return
	 */
	public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
		return this.stpe;
	}
	
	@Override
	public void expiredTimer(TimerEventProducer timer) {
		// Solicita la notificación inmediata
		notificarEventoTablaRutasModificada(0);
	}

	/* Adición, modificación y eliminación de rutas RIP */
	
	public void addRutaRIP(String clave, InetAddress destino, int prefijoRed, int distancia, InetAddress proximoSalto, boolean flagModificado) {
		// Buscar y actualizar la ruta
		Ruta ruta;
		synchronized (this) {
			ruta = mapaRutas.get(clave);
		}
		boolean esNueva = (ruta == null);
		
		if (ruta == null) {
			// Creación
			ruta = new Ruta();
			ruta.estado = (flagModificado ? EstadoRuta.CREADA : EstadoRuta.NO_MODIFICADA);
			ruta.tiempoActiva = new Date();
			// Común a modificación o creación
			ruta.destino = destino;
			ruta.prefijoRed = prefijoRed;
			ruta.distancia = distancia;
			ruta.proximoSalto = proximoSalto;
		}
		else {
			synchronized (ruta) {
				ruta.estado = (flagModificado ? EstadoRuta.MODIFICADA : EstadoRuta.NO_MODIFICADA);
				// Común a modificación o creación
				ruta.destino = destino;
				ruta.prefijoRed = prefijoRed;
				ruta.distancia = distancia;
				ruta.proximoSalto = proximoSalto;
			}
		}

		//ruta.indiceInterfazEntrada = /* */; //TODO: falta para implementar split horizon						
		//ruta.tablaRutas =   //número de tabla externa

		// Si es nueva, la inserta
		if (esNueva) {
			synchronized (this) {
				listaRutas.add(ruta);
				mapaRutas.put(clave, ruta);
			}
		}

	}
	
	public void addRutaOSPFv2(String clave, InetAddress destino, int prefijoRed, int distancia, InetAddress proximoSalto) {
		// Buscar y actualizar la ruta
		Ruta ruta;
		synchronized (this) {
			ruta = mapaRutas.get(clave);
		}
		boolean esNueva = (ruta == null);
		
		if (ruta == null) {
			// Creación
			ruta = new Ruta();
			// Común a modificación o creación
			ruta.destino = destino;
			ruta.prefijoRed = prefijoRed;
			ruta.distancia = distancia;
			ruta.proximoSalto = proximoSalto;
		}
		else {
			synchronized (ruta) {
				// Común a modificación o creación
				ruta.destino = destino;
				ruta.prefijoRed = prefijoRed;
				ruta.distancia = distancia;
				ruta.proximoSalto = proximoSalto;
			}
		}

		// Si es nueva, la inserta
		if (esNueva) {
			synchronized (this) {
				listaRutas.add(ruta);
				mapaRutas.put(clave, ruta);
			}
		}

	}
	
	
	

	public void addRuta(String clave, Ruta ruta) {
		synchronized (ruta) {
			ruta.tiempoActiva = new Date();
		}
		synchronized (this) {
			if (mapaRutas.get(clave) == null) {
				listaRutas.add(ruta);
			}
			mapaRutas.put(clave, ruta);
		}
		System.out.println("TablaRutas:addRuta()");
		System.out.println(this.toString());

		// Notificación retardada del cambio
		this.notificarEventoTablaRutasModificada(RETARDO_ACUMULACION);
	}

	public Ruta getRuta(String clave) {
		return mapaRutas.get(clave); // No debe modificarlo
	}
	
	/**
	 * Si las rutas se obtienen con getRuta y se modifican directamente sobre los objetos Ruta, se
	 * debe notificar a la tabla de rutas para que realice las acciones oportunas, como notificar a los
	 * observadores del evento tabla de rutas modificada.
	 */
	public void setTablaModificada () {
		// Solicita notificación retardada del cambio
		this.notificarEventoTablaRutasModificada(RETARDO_ACUMULACION);		
	}

	public Iterator<Ruta> iterator() {
		return listaRutas.iterator();
	}

	public Iterator<Ruta> iteratorRutaModificada() {
		return new RutasModificadasIterador(listaRutas.iterator());
	}
	
	public void removeRuta(Long clave) {
		Ruta ruta;
		synchronized (this) {
			ruta = mapaRutas.get(clave);
			if (ruta != null) {
				mapaRutas.remove(clave);
				listaRutas.remove(ruta);
			}
		}
		
		System.out.println("TablaRutas:removeRuta()");
		System.out.println(this.toString());

		// Notificación retardada del cambio
		this.notificarEventoTablaRutasModificada(RETARDO_ACUMULACION);
	}
	
	public int size() {
		return listaRutas.size();
	}

	public boolean isEmpty() {
		return listaRutas.isEmpty();
	}

	/**
	 * Imprime la tabla de rutas
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Ruta ruta: this.listaRutas) {
			sb.append("TablaRutas:---Ruta #"); sb.append(i++);
			sb.append(ruta.destino); sb.append("/"); sb.append(ruta.prefijoRed); 
			sb.append("->"); sb.append(ruta.proximoSalto); 
			sb.append(" d="); sb.append(ruta.distancia);
			sb.append(" "); sb.append(ruta.estado);	
			sb.append("\n");
		}
		return sb.toString();
	}


	
    private class RutasModificadasIterador implements Iterator<TablaRutas.Ruta> {

        private Iterator<Ruta> iterador;   // iterador sobre la lista
        private Ruta siguiente = null; // siguiente ya localizado

        public RutasModificadasIterador(Iterator<Ruta> iterador) {
        	this.iterador = iterador;
        }

		@Override
		public boolean hasNext() {
				boolean siguienteEsRutaNoModificada = false;

				while ((! siguienteEsRutaNoModificada) && iterador.hasNext()) {
					siguiente = iterador.next();
					/* Avanza si no esta modificada la ruta */
					if (siguiente.estado != EstadoRuta.NO_MODIFICADA)
						siguienteEsRutaNoModificada = true;
				}
				return (siguienteEsRutaNoModificada);
		}

		@Override
		public Ruta next() {
			return siguiente;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("no soportado borrado");
		}
		
    }

}

 package msti.ospfv2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import msti.ospfv2.mensaje.IMensajeOSPFv2LSA;
import msti.ospfv2.mensaje.IMensajeOSPFv2LSARouterLinksLinks;
import msti.ospfv2.mensaje.MensajeOSPFv2LSANetworkLinksAdvertisements;
import msti.ospfv2.mensaje.MensajeOSPFv2LSARouterLinks;
import msti.util.Inet4Address;
 
public class Dijkstra implements ITablaRutasModificadaListener{
  /*
	private static final Graph.Edge[] GRAPH = {
      new Graph.Edge("a", "b", 7),
      new Graph.Edge("a", "c", 9),
      new Graph.Edge("a", "f", 14),
      new Graph.Edge("b", "c", 10),
      new Graph.Edge("b", "d", 15),
      new Graph.Edge("c", "d", 11),
      new Graph.Edge("c", "f", 2),
      new Graph.Edge("d", "e", 6),
      new Graph.Edge("e", "f", 9),
   };
   private static final String START = "a";
   private static final String END = "e";
 
   public static void main(String[] args) {
      Graph g = new Graph(GRAPH);
      g.dijkstra(START);
      // g.printPath(END);
      g.printAllPaths();
      //g.getRuta(END);
      g.getAllRutas();
   }
   */


	/**
	 * Por medio del algoritmo de Dijkstra se calcula el árbol de la red con los LSA del database
	 * Se calcula la ruta a cada destino y se añade a la tabla
	 * @param conf Clase con valores de Configuración
	 * @param tabla Tabla de rutas donde queremos introducir los valores de las nuevas rutas
	 */
	public static void recalcularTabla(ConfiguracionOSPFv2 conf, TablaRutas tabla){
	   
		List<IMensajeOSPFv2LSA> listaRouterLinks = conf.getLSARouterLinksInDatabase();
		List<IMensajeOSPFv2LSA> listaNetworkLinks = conf.getLSANetworkLinksInDatabase();
		List<Graph.Edge> listaEdges = new ArrayList<Graph.Edge>();
	   
		//sacar LSARouterLinks del database y añadir cada link a la lista de Edges
		for(IMensajeOSPFv2LSA lsa: listaRouterLinks){
			if(!lsa.equals(null)){
				//Si el LSAge es igual a MAX_AGE no se tiene en cuenta para el calculo
				if(lsa.getHeader().getLSAge()<conf.MAX_AGE){
					//Si es de tipo RouterLink añadir cada link desde el origen hasta el destino con su métrica
					MensajeOSPFv2LSARouterLinks routerLinksLSA = (MensajeOSPFv2LSARouterLinks) lsa;
					for(IMensajeOSPFv2LSARouterLinksLinks link: routerLinksLSA.getRouterLinks()){
						//origen: routerID
						   InetAddress src = Inet4Address.fromInt(routerLinksLSA.getHeader().getAdvertisingRouter());
						   //destino: interfaceIPAddress & InterfaceIPMask (prefijo de red)
						   InetAddress dst = Inet4Address.fromInt(link.getLinkID() & link.getLinkData());
						   listaEdges.add(new Graph.Edge(src.getHostAddress(), dst.getHostAddress(), link.getTOS0Metric()));
					}
				} 
			}
		}
	   
		//sacar LSANetworkLinks del database y añadir cada prefijo a la lista de Edges
		for(IMensajeOSPFv2LSA lsa: listaNetworkLinks){ 
			if(!lsa.equals(null)){
				//Si el LSAge es igual a MAX_AGE no se tiene en cuenta para el calculo
				if(lsa.getHeader().getLSAge()<conf.MAX_AGE){
					//Si es NetworkLink, añadir ruta con origen dirRed, destino cada AttachedRouter y coste 0
					MensajeOSPFv2LSANetworkLinksAdvertisements networkLinkLSA = (MensajeOSPFv2LSANetworkLinksAdvertisements) lsa;
					for(Integer attachedRouter: networkLinkLSA.getAttachedRouters()){
						//origen: interfaceIPAddress del DR (de la red) con la máscara de red (prefijo de red
						InetAddress src = Inet4Address.fromInt(networkLinkLSA.getHeader().getLinkStateID()
							   										& networkLinkLSA.getNetworkMask());
						//destino: cada routerID de los vecinos de esta red
						InetAddress dst = Inet4Address.fromInt(attachedRouter);
						listaEdges.add(new Graph.Edge(src.getHostAddress(), dst.getHostAddress(), 0));
					}
   
				} 
			}
		}
		//Resto de LSA para Áreas
	   
	   
		//Convertir lista en array y crear Graf
		Graph.Edge[] graf = listaEdges.toArray(new Graph.Edge[listaEdges.size()]);
	   
		Graph g = new Graph(graf);
		InetAddress router = Inet4Address.fromInt(conf.routerID);
		g.dijkstra(router.getHostAddress());
	   
		g.actualizarTabla(conf,tabla);
		g.printAllPaths();
   }
   
   
   
   
	/**
	 * Método del Observer ITablaRutasModificada, que se llama cuando una entidad externa modifica la tabla de rutas
	 * @param idTablaRutas
	 */
   @Override
	public void tablaRutasModificada(int idTablaRutas) {
		//Cuando una entidad externa modifica la tabla de rutas, se recalcula de igual manera
	   recalcularTabla(ConfiguracionOSPFv2.getInstance(), ConfiguracionOSPFv2.getInstance().tablaRutas);
		
	}
   
   
   
}
 
class Graph {
   private final Map<String, Vertex> graph; // mapping of vertex names to Vertex objects, built from a set of Edges
 
   /** One edge of the graph (only used by Graph constructor) */
   public static class Edge {
      public final String v1, v2;
      public final int dist;
      public Edge(String v1, String v2, int dist) {
         this.v1 = v1;
         this.v2 = v2;
         this.dist = dist;
      }
   }
 
   /** One vertex of the graph, complete with mappings to neighbouring vertices */
  public static class Vertex implements Comparable<Vertex>{
	public final String name;
	public int dist = Integer.MAX_VALUE; // MAX_VALUE assumed to be infinity
	public Vertex previous = null;
	public final Map<Vertex, Integer> neighbours = new HashMap<Vertex, Integer>();
 
	public Vertex(String name)
	{
		this.name = name;
	}
 
	private void printPath()
	{
		if (this == this.previous)
		{
			System.out.printf("%s", this.name);
		}
		else if (this.previous == null)
		{
			System.out.printf("%s(unreached)", this.name);
		}
		else
		{
			this.previous.printPath();
			System.out.printf(" -> %s(%d)", this.name, this.dist);
		}
	}
	
	private void getPathArray(List<String> arrayStrings, List<Integer> arrayDist){
		
		if((this != this.previous) && (this.previous !=null)){
			this.previous.getPathArray(arrayStrings, arrayDist);
			arrayStrings.add(this.name);
			arrayDist.add(this.dist);
		}
		
	}
	
	
	private String getNextHop(String endName){
		String nextHope=null;
		if (this == this.previous)
		{
			//System.out.printf("%s", this.name);
			return this.name;
		}
		else if (this.previous == null)
		{
			//System.out.printf("%s(unreached)", this.name);
		}
		else
		{
			nextHope=this.previous.getNextHop(endName);
			//System.out.printf(" -> %s(%d)", this.name, this.dist);
			if(this.previous == this.previous.previous){
				return this.name;
			}
			
		}
	
		return nextHope;
	}
	
	private int getCost(String endName){
		if (this == this.previous)
		{
			//System.out.printf("%s", this.name);
			return 0;
		}
		else if (this.previous == null)
		{
			//System.out.printf("%s(unreached)", this.name);
			return -1;
		}
		else
		{
			this.previous.getCost(endName);
			//System.out.printf(" -> %s(%d)", this.name, this.dist);
			if(this.name.equals(endName)){
				//System.out.printf("Para llegar a: " + this.name + ", la distancia es: " +  this.dist);
				return this.dist;
			}
		}
		return  0;
	}
 
	public int compareTo(Vertex other)
	{
		if (dist == other.dist)
			return name.compareTo(other.name);
 
		//return Integer.compare(dist, other.dist);
		return Integer.valueOf(dist).compareTo(Integer.valueOf(other.dist));
	}
 
	@Override public String toString()
	{
		return "(" + name + ", " + dist + ")";
	}
}
 
   /** Builds a graph from a set of edges */
   public Graph(Edge[] edges) {
      graph = new HashMap<String, Vertex>(edges.length);
 
      //one pass to find all vertices
      for (Edge e : edges) {
         if (!graph.containsKey(e.v1)) graph.put(e.v1, new Vertex(e.v1));
         if (!graph.containsKey(e.v2)) graph.put(e.v2, new Vertex(e.v2));
      }
 
      //another pass to set neighbouring vertices
      for (Edge e : edges) {
         graph.get(e.v1).neighbours.put(graph.get(e.v2), e.dist);
         //graph.get(e.v2).neighbours.put(graph.get(e.v1), e.dist); // also do this for an undirected graph
      }
   }
 
   /** Runs dijkstra using a specified source vertex */ 
   public void dijkstra(String startName) {
      if (!graph.containsKey(startName)) {
         System.err.printf("Graph doesn't contain start vertex \"%s\"\n", startName);
         return;
      }
      final Vertex source = graph.get(startName);
      NavigableSet<Vertex> q = new TreeSet<Vertex>();
 
      // set-up vertices
      for (Vertex v : graph.values()) {
         v.previous = v == source ? source : null;
         v.dist = v == source ? 0 : Integer.MAX_VALUE;
         q.add(v);
      }
 
      dijkstra(q);
   }
 
   /** Implementation of dijkstra's algorithm using a binary heap. */
   private void dijkstra(final NavigableSet<Vertex> q) {      
      Vertex u, v;
      while (!q.isEmpty()) {
 
         u = q.pollFirst(); // vertex with shortest distance (first iteration will return source)
         if (u.dist == Integer.MAX_VALUE) break; // we can ignore u (and any other remaining vertices) since they are unreachable
 
         //look at distances to each neighbour
         for (Map.Entry<Vertex, Integer> a : u.neighbours.entrySet()) {
            v = a.getKey(); //the neighbour in this iteration
 
            final int alternateDist = u.dist + a.getValue();
            if (alternateDist < v.dist) { // shorter path to neighbour found
               q.remove(v);
               v.dist = alternateDist;
               v.previous = u;
               q.add(v);
            } 
         }
      }
   }
 
   /** Prints a path from the source to the specified vertex */
   public void printPath(String endName) {
      if (!graph.containsKey(endName)) {
         System.err.printf("Graph doesn't contain end vertex \"%s\"\n", endName);
         return;
      }
 
      graph.get(endName).printPath();
      System.out.println();
   }
   /** Prints the path from the source to every vertex (output order is not guaranteed) */
   public void printAllPaths() {
      for (Vertex v : graph.values()) {
         v.printPath();
         System.out.println();
      }
   }  

   public void getRuta(String endName) {
      if (!graph.containsKey(endName)) {
         System.err.printf("Graph doesn't contain end vertex \"%s\"\n", endName);
         return;
      }
      System.out.println("Siguiente salto: " +  graph.get(endName).getNextHop(endName));
      System.out.println("Coste total: " +  graph.get(endName).getCost(endName));
   }
   
   public void getAllRutas() {
      for (Vertex v : graph.values()) {
    	 System.out.println("Ruta hacia " + v.name +". Siguiente salto: " +  v.getNextHop(v.name) +", coste: " + v.getCost(v.name));
      }
   }
   
   
   /**
	 * Para cada prefijo del árbol de red (al que no este conectado alguna de nuestras interfaces),
	 * se calcula la ruta (dirección IP del proximo salto y coste total) y se añade a la tabla de rutas
	 * @param conf Clase con valores de Configuración
	 * @param tabla Tabla de rutas donde queremos introducir los valores de las nuevas rutas
	 */
   public void actualizarTabla(ConfiguracionOSPFv2 conf, TablaRutas tabla){
	   
	   for(IMensajeOSPFv2LSA lsa: conf.getLSANetworkLinksInDatabase()){
		   try {
			   
			   	 MensajeOSPFv2LSANetworkLinksAdvertisements networkLinkLSA = (MensajeOSPFv2LSANetworkLinksAdvertisements) lsa;
	    		 int prefijo =  networkLinkLSA.getHeader().getLinkStateID() & networkLinkLSA.getNetworkMask();
	    		 
	    		//Excluir los prefijos de mis Interfaces.
				if(conf.getPrefijosInterfaces().contains(prefijo)){
					//Una de las interfaces del router está en esta red, por lo que no es necesario añadir la ruta
				}else{
					//El router no tiene interfaces en esta red, por lo que debemos añadir la ruta
					
		    		 String prefijoString = Inet4Address.fromInt(prefijo).getHostAddress();
		    		 if (graph.containsKey(prefijoString)){
		    			 System.err.printf("Graph doesn't contain end vertex \"%s\"\n", prefijoString);
		    	         return;
		    		 }
		    		 List<String> arrayStrings = new ArrayList<String>();
		    		 List<Integer> arrayDist = new ArrayList<Integer>();
		    		 
		    		 //Pasamos las listas al método PathArray que nos añade los nombres de los nodos y el coste acumulado
		    		 graph.get(prefijoString).getPathArray(arrayStrings,arrayDist);       		         		 
		    		 
		    		 //el primer String es el prefijo de red del próximo salto
		    		 String prefijoRedProximoSalto= arrayStrings.get(0);
		    		 //el segundo String es el RouterID del próximo salto
		    		 String routerIDProximoSalto = arrayStrings.get(1);
		    		 InetAddress ipAddressProximoSalto = null;
		    		 int ipAddressMask = 0;
		    		 //busco la LSA que tenga este RouterID
		    		 MensajeOSPFv2LSARouterLinks lsaRouterLink = (MensajeOSPFv2LSARouterLinks) conf.getRouterLinkWithRouterID(Inet4Address.toInt(InetAddress.getByName(routerIDProximoSalto)));
		    		 if(lsaRouterLink==null){
		    			 System.err.printf("Dijkstra.actualizartabla: Error al buscar el RouterLink en el database, routerID no encontrado: " + routerIDProximoSalto);
		    	         return;
		    		 }
		    		 //Este LSA tiene todas las dirIP de este routerID, buscamos la que coincida con el prefijo del proximo salto.
		    		 for (IMensajeOSPFv2LSARouterLinksLinks link: lsaRouterLink.getRouterLinks()){
		    			 if((link.getLinkID() & link.getLinkData()) == Inet4Address.toInt(InetAddress.getByName(prefijoRedProximoSalto))){	
		    				 ipAddressProximoSalto  =  Inet4Address.fromInt(link.getLinkID());
		    				 ipAddressMask = link.getLinkData();
		    			 }
		    		 }
		    		 
		    		 //Si no lo encuentra produce error y no añade la ruta.
		    		 if(ipAddressProximoSalto==null){
		    			 System.err.printf("Dijkstra.actualizartabla: Error al buscar el dirIp en los Links del router con ID: " + routerIDProximoSalto);
		    	         return;
		    		 }
		    		 //Clave: idTabla de rutas
		    		 //Destino: prefijo
		    		 //Prefijo: máscara de red
		    		 //Siguientesalto: linkData del link
		    		 //Coste: valor del ultimo Integer del arrayDist
		    		 
		    		 tabla.addRutaOSPFv2(conf.idTablaRutas, InetAddress.getByName(prefijoString), ipAddressMask, arrayDist.get(arrayDist.size()-1), ipAddressProximoSalto);
				}
        	} catch (UnknownHostException e) {
        		e.printStackTrace();
        	}
	    }
  
   }
   
   
   
}
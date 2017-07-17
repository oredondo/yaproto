/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.util;

import java.util.HashMap;
import java.util.Map;


/**
 * Lista doblemente enlazada que, adem�s, mantiene un mapa hash de los nodos para acceso directo al elemento, y
 * a su inmediato anterior y posterior. Existe un LinkedHashMap en las bibliotecas JAVA, pero el mapa hash se
 * refiere directamente a los Item, por lo que no es posible avanzar o retroceder salvo usando iteradores.
 * 
 * No es viable hacerlo Map y, a la vez, hacerlo Coleccion , porque el m�todo remove() en las dos interfaces 
 * devuelve en un caso un Item y en otro un boolean.
 * 
 * @param <Key>
 * @param <Item>
 */
public class HashMappedDoublyLinkedList<Key, Item> extends DoublyLinkedList<Item> {

	/* Mapa que indexa los nodos de la lista (no los objetos que contienen los nodos) */
	private Map<Key, DoublyLinkedList<Item>.Node> mapa;

	
	public HashMappedDoublyLinkedList() {
		super();
		mapa = new HashMap<Key, DoublyLinkedList<Item>.Node>();
	}

    // a�adir al final
	@Deprecated
    public void addLast(Item item) {
		throw new IllegalArgumentException("Método obsoleto. No admite key. Usar addLast(key,item)");
    }
    // a�adir al principio
	@Deprecated
    public void addFirst(Item item) {
		throw new IllegalArgumentException("Método obsoleto. No admite key. Usar addFirst(key,item)");
    }

	 public void addLast(Key key, Item item) {
		 if (mapa.get(key) != null) 
			 throw new IllegalArgumentException("key");
		 super.addLast(item);
		 mapa.put(key, tail.prev); // el nuevo �ltimo nodo
	 }
	 
	 /** 
	  * A�ade al final de la lista. La clave no puede existir previamente.
	  */
	 public void addFirst(Key key, Item item) {
		 DoublyLinkedList<Item>.Node nodo = mapa.get(key);
		 if (nodo != null)  // repetido. habitualmente put sobreescribe el repetido en map, pero esto es a�adir nuevo.
			 throw new IllegalArgumentException("Ya existe un elemento con la key proporcionada");
		 super.addFirst(item);
		 mapa.put(key, head.next);  // el nuevo primer nodo
	 }
	 
	 /* 
	  * Obtiene el item asociado a la clave indicada. Devuelve null si no existe.
	  */
	 public Item get(Key key) {
		 DoublyLinkedList<Item>.Node nodo = mapa.get(key);
		 if (nodo == null)
			 return null;
		 return nodo.item;
	 }

	 /** A�ade al final el nodo. Sustituye el item del nodo, si ya existe la key */
	 public void put(Key key, Item item) {
		 DoublyLinkedList<Item>.Node nodo = mapa.get(key);
		 if (nodo != null) {
			 // Si existe, sobreescribe
			 nodo.item = item;
		 }
		 else {
			 // a�ade al final (sin volver a comprobar si existe como har�a el m�todo addLast)
			 super.addLast(item);
			 mapa.put(key, tail.prev); // el nuevo �ltimo nodo
		 }		 
	 }

	 /**
	  * Obtiene el elemento anterior al de la clave indicada.
	  * @param key
	  * @return Item anterior, o null si no existe (el indicado es el primero).
	  */
	 public Item getPrevious(Key key) {
		 DoublyLinkedList<Item>.Node nodo = mapa.get(key);
		 if ( (nodo == null) || (head == nodo.prev))
			 return null;
		 return nodo.prev.item;

	 }

	 /**
	  * Obtiene el elemento posterior al de la clave indicada.
	  * @param key
	  * @return Item anterior, o null si no existe (el indicado es el primero).
	  */
	 public Item getNext(Key key) {
		 DoublyLinkedList<Item>.Node nodo = mapa.get(key);
		 if ( (nodo == null) || (nodo.next == tail))
			 return null;
		 return nodo.next.item;

	 }

	 /**
	  * Obtiene el primer elemento que fue a�adido.
	  * @return Item, o null si no hay elementos.
	  */
	 public Item getFirst() {
		 if ( (head.next == tail))
			 return null;
		 return head.next.item;

	 }

	 /**
	  * Obtiene el �ltimo elemento a�adido.
	  * @return Item, o null si no hay elementos.
	  */
	 public Item getLast() {
		 if ( (head.next == tail))
			 return null;
		 return tail.prev.item;

	 }
	 }

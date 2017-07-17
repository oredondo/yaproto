/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.util;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class DoublyLinkedList<Item> implements Iterable<Item> {
    protected int num;        // número de elementos
    protected Node head;     // cabecera
    protected Node tail;    // centinela

    public DoublyLinkedList() {
        head  = new Node(null, null, null);
        tail = new Node(head, null, null);
        head.next = tail;
    }

    // Nodo de la lista
    protected class Node {
        protected Item item;
        protected Node next;
        protected Node prev;

        protected Node(Node prev, Node next, Item item) {
        	this.prev = prev;
        	this.next = next;
        	this.item = item;
        }
    }

    public boolean isEmpty() { 
    	return num == 0; 
    }
    public int size() { 
    	return num; 
    }

    // añadir al final
    public void addLast(Item item) {
        Node last = tail.prev;
        tail.prev = new Node(last, tail, item);
        last.next = tail.prev;
        num++;
    }
    // añadir al principio
    public void addFirst(Item item) {
        Node first = head.next;
        head.next = new Node(head, first, item);
        first.prev = head.next;
        num++;
    }

    public ListIterator<Item> iterator() { 
    	return new DoublyLinkedListIterator(); 
    }

    // asume no llamadas a DoublyLinkedList.addFirst() o .addLast() durante la iteración (para no recurrir a copyonwrite list)
    private class DoublyLinkedListIterator implements ListIterator<Item> {
        private Node current      = head.next;  // nodo actual, devuelto por next()
        private Node lastAccessed = null;      // último nodo devuelto por prev() or next()
                                               // reset a null en cualquier remove() o add()
        private int index = 0;

        public boolean hasNext() { 
        	return index < num; 
        }
        public boolean hasPrevious()  { 
        	return index > 0;
        }
        public int previousIndex() { 
        	return index - 1; 
        }
        public int nextIndex() { 
        	return index;
        }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastAccessed = current;
            Item item = current.item;
            current = current.next; 
            index++;
            return item;
        }

        public Item previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            current = current.prev;
            index--;
            lastAccessed = current;
            return current.item;
        }

        // modifica el item del último elemento accedido por next() o previous()
        // no deben existir llamadas a remove() o add() para 
        public void set(Item item) {
            if (lastAccessed == null) throw new IllegalStateException();
            lastAccessed.item = item;
        }

        // remove the element that was last accessed by next() or previous()
        // condition: no calls to remove() or add() after last call to next() or previous()
        public void remove() { 
            if (lastAccessed == null) throw new IllegalStateException();
            Node x = lastAccessed.prev;
            Node y = lastAccessed.next;
            x.next = y;
            y.prev = x;
            num--;
            index--;
            lastAccessed = null;
        }

        // añade elemento tras el actual
        public void add(Item item) {
            Node x = current.prev;
            current.prev = new Node(current.prev, current, item);
            x.next = current.prev;
            num++;
            index++;
            lastAccessed = null;
        }

    }
}

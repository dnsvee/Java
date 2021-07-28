import java.lang.StringBuilder;
import java.lang.RuntimeException.*;
import java.lang.*;

// linked list implementation
public class LinkedList<E> {
	static public void main(String[] args) {
		LinkedList<String> ll = new LinkedList<>();

		ll.push("one", "two", "three");
		ll.check("[one, two, three]", 3);

		ll.clear();
		ll.check("[]", 0);
		System.out.println();

		ll.push("hello");
		ll.check("[hello]", 1);
		ll.clear();

		ll.unshift("three", "two", "one");
		ll.check("[one, two, three]", 3);
		ll.clear();

		ll.push("1", "2", "3", "4", "5");
		ll.remove(1);
		ll.check("[1, 3, 4, 5]", 4);
		ll.remove(0);
		ll.check("[3, 4, 5]", 3);
		ll.remove(2);
		ll.check("[3, 4]", 2);
		ll.remove(1);
		ll.remove(0);
		ll.check("[]", 0);

		System.out.println("all tests passed");
	}

	void check(String s, int i) {
		if (!toString().equals(s) || i != size())
			throw new RuntimeException(String.format("assert fail: %s != %s or %d != %d\n", toString(), s, i, size()));
	}

	class Node {
		Node next;
		Node prev;

		E    elem;

		Node(E e) {
			elem = e;
			next = null;
			prev = null;
		}
	}

	private Node head = null;
	private Node last = null;

	private int  len  = 0;

	public int size() {
		return len;
	}

	public LinkedList() {
	}

	// add first elem. to list
	public void init(E e) {
		len  = 1;
		head = new Node(e);
		last = head;
	}

	public void push(E... es) {
		for (E e : es) push(e);
	}

	// add at end
	public void push(E e) {
		if (len == 0) {
			init(e);
			return;
		}

		last.next      = new Node(e);
		last.next.prev = last;
		last           = last.next;
		len++;
	}

	// remove from end and return
	public E pop() {
		if (len == 0) return null;

		Node l = last;
		last   = last.prev;
		if (last != null) 
			last.next = null;
		else
			head = null;

		len--;
		return l.elem;
	}

	// remove element at index i
	public void remove(int idx) {
		if (idx >= len) throw new IllegalArgumentException();

		Node cur = head;
		for(int i = 0; i < len; i++) {
			if (idx != i) {
				cur = cur.next;
			       	continue;
			}

			if (cur.prev == null) 
				head = cur.next;
			else
				cur.prev.next = cur.next;

			if (cur.next == null) 
				last = cur.prev;
			else
				cur.next.prev = cur.prev;

			len--;

			break;
		}

	}


	// insert at begin
	public void unshift(E e) {
		if (len == 0) {
			init(e);
			return;
		}

		head.prev      = new Node(e);
		head.prev.next = head;
		head           = head.prev;
		len++;
	}

	public void unshift(E... es) {
		for (E e : es) unshift(e);
	}


	// removes and returns head
	public Node shift(E e) {
		if (len == 0) return null;

		Node n = head;
		head = head.next;

		if (head != null)
			head.prev = null;
		else
			last = null;

		return n;
	}



	// remove all nodes
	public void clear() {
		while (size() != 0) pop();
	}

	// string repr. calls toString on each elem. and result == [elem1, elem2, elem3]
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		Node cur = head;
		sb.append("[");
		for(int i = 0; i < len; i++) {
			if (i > 0) sb.append(", ");
			sb.append(String.format("%s", cur.elem.toString()));
			cur = cur.next;
		}
		sb.append("]");
		return sb.toString();
	}

	void print() {
		System.out.println(toString());
	}

	// bunch of tests
	static public void test() {
	}
};

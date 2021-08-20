import java.lang.StringBuilder;
import java.lang.RuntimeException.*;
import java.lang.*;
import java.util.function.*;
import java.util.function.Function;

// double linked list implementation
public class LinkedList<E extends Object> {
	static public void main(String[] args) {
		// some tests
		// converts the list into a string for testing purposes
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
		ll.map((e) -> e += "!");
		ll.check("[1!, 2!, 3!, 4!, 5!]", 5);

		// add lots of randoms
		ll.forEach((e) -> System.out.printf("%s\n", e));
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

	// internal node impl.
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

	// head and tail of list
	private Node head = null;
	private Node last = null;

	// length of list
	private int len  = 0;

	// default constructor
	public LinkedList() {
	}

	// returns length
	public int length() {
		return len;
	}

	// add first element to list; private use
	public void init(E e) {
		len  = 1;
		head = new Node(e);
		last = head;
	}

	// add multiple leems to lsit
	public void push(E... es) {
		for (E e : es) 
			push(e);
	}

	// calls function 'f' on each element
	public void forEach(Consumer<E> f) {
		Node cur = head;
		for(int i = 0; i < len; i++) {
			f.accept(cur.elem);
			cur = cur.next;
		}
	}

	// calls function on each element and updates value with returned value of function 'f'
	public void map(Function<E, E> f) {
		Node cur = head;
		for(int i = 0; i < len; i++) {
			cur.elem = f.apply(cur.elem);
			cur = cur.next;
		}
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
		if (len == 0) 
			return null;

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
	public E remove(int idx) {
		if (idx >= len) 
			throw new IllegalArgumentException();

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

			return cur.elem;
		}

		return null;
	}

	// insert at head
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

	// insert multiple values at head
	public void unshift(E... es) {
		for (E e : es) 
			unshift(e);
	}

	// remove and return from head
	public E shift(E e) {
		if (len == 0) 
			return null;

		Node n = head;
		head = head.next;

		if (head != null)
			head.prev = null;
		else
			last = null;

		return n.elem;
	}

	// remove all nodes
	public void clear() {
		while (pop() != null);
	}

	// string represention of list; calls toString on each element
	// result looks like [1, 2, 3, 4, 5]
	public String toString() {
		StringBuilder sb = new StringBuilder("[");

		Node cur = head;

		for(int i = 0; i < len; i++) {
			sb.append(String.format("%s", cur.elem.toString()));

			if (i < len - 1) 
				sb.append(", ");

			cur = cur.next;
		}
		sb.append("]");

		return sb.toString();
	}

	// compares the string rep. of the LinkedList against the provided string s and checks if size i equal to i
	void check(String s, int i) {
		if (len != i) 
			throw new RuntimeException(String.format("assert fail: expected size == %s; found size == %s", i, len));

		if (toString().equals(s) == false)
			throw new RuntimeException(String.format("assert fail: %s != %s", toString(), s));
	}
};

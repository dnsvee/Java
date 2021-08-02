package Treap;

import java.lang.Integer;
import java.util.Random;
import java.lang.RuntimeException;

// a treap; a datastructure that combines a binary heap and a tree
public class Treap<K extends Comparable<K>> {
	Random rand = new Random();

	// nodes for the treap; prio is a random number that helps keep
	// the Treap balanced
	public class Node {
		K    elem;
		Node left, right;
		int  prio;

		public Node(K k) {
			elem = k;
			//prio = rand.nextInt(Integer.MAX_VALUE);
			prio = rand.nextInt(1000);
			System.out.printf("%d\n", prio);
		}
	}

	Node root = null;
	int  sz   = 0;

	// size of Treap
	public int size() {
		return sz;
	}

	// is Treap empty
	public boolean isEmpty() {
		return size() == 0;
	}

	// constructor
	public Treap() {
	}

	// utility for toString()
	public String toString(Node n) {
		String r = "(";

		if (n.left != null) 
			r += toString(n.left);
		else
			r += "null";

		r += ", " + n.elem.toString() + ", ";

		if (n.right != null) 
			r += toString(n.right);
		else
			r += "null";

		return r + ")";
	}

	// string repr. of Treap (fix:use StringBuilder)
	// if empty: ()
	// ex. output: ((null, 1, null), 3, (null, 5, ((null, 6, null), 7, null)))
	public String toString() {
		if (root == null) return new String("()");
		return toString(root);
	}

	// util method for search
	public Node search(Node n, K k) {
		if (n == null) return null;

		if (n.elem == k) return n;

		if (k.compareTo(n.elem) < 0) return search(n.left,  k);

		return search(n.right, k);
	}


	// search for element and return Node if found; otherwise null
	public Node search(K k) {
		if (root == null) return null;

		return search(root, k);
	}	

	// search for element and return true if found; otherwise null
	public boolean search(K k) {
		return search(root, k) == null;
	}

	// use for testing of Treap
	public class Test<K extends Comparable<K>> {
		Treap<K> t;

		public Test() {
			t = new Treap<>();
		}

		public void find(K... args) {
			for (K k : args) {
				if (t.search(k) == null) throw new RuntimeException("bummer");
			}
		}
	}

	public void findt(K k) {
		if (search(k) == null) throw new RuntimeException("findt failed: Element " + k.toString() + " not found");
	}

	public void findf(K k) {
		if (search(k) != null) throw new RuntimeException("findf failed: Element " + k.toString() + " found");
	}

	// remove element from Treap
	public void remove(K k) {
		root = remove(k, root);
	}

	// util method for remove
	// this methode may change the structure of the Treap starting from Node n
	// this method will return the root of the Treap it has operated on
	// return value == n if structure has not changed
	public Node remove(K k, Node n) {
		if (n == null) return null;

		// remove in left side in elem smaller
		if (k.compareTo(n.elem) < 0) {
			n.left = remove(k, n.left);
			return n;
		}

		// remove in right side if elem greater
		if (n.elem.compareTo(k) < 0) {
			n.right = remove(k, n.right);
			return n;
		}

		// if leaf node check if elem is equal to k; if so remove
		if (n.left == null && n.right == null) 
			if (n.elem == k) 
				return null;
			else
				return n;

		// node value equal to k
		//
		// if only left child exist or both children exist but priority of left node
		// is higher then rotate tree to the right and move the node down in the right
		// subtree
		if (n.left != null && (n.right == null || n.left.prio < n.right.prio)) {
			n = rotateRight(n);
			n.right      = remove(k, n.right);
			return n;
		} 

		// otherwise rotate left and move down into left subtree
		n = rotateLeft(n);
		n.left     = remove(k, n.left);
		return n;
	}

	// insert elem k in tree
	public void insert(K k) {
		// if empty tree insert as root
		if (root == null) {
			root = new Node(k);
			sz++;
			return;
		}
		// insert into root
		root = insert(k, root);
	}

	public Node rotateLeft(Node n) {
		Node t  = n.right;
		n.right = n.right.left;
		t.left  = n;

		return t;
	}

	public Node rotateRight(Node n) {
		Node t = n.left;
		n.left = n.left.right;
		t.right = n;

		return t;
	}

	// util. method for insert
	// may change the structure of the subtree with root n so this method will return
	// the root of the subtree it has inserted element into; or it returns the original
	// root
	public Node insert(K k, Node n) {
		if (k == n.elem) return n;

		// insert in left side
		if (k.compareTo(n.elem) < 0) {
			if (n.left != null)  
				// remove further down into subtree
				n.left = insert(k, n.left);
			else 
				// insert as new leaf
				n.left = new Node(k);

			// when inserted as leaf move back upwards
			// and fix each level by doing a proper rotate
			if (n.left.prio < n.prio) 
				return rotateRight(n);
			else 
				return n;
		} else {
			// do the same thing in the other side
			if (n.right != null) 
				n.right = insert(k, n.right);
			else 
				n.right = new Node(k);

			if (n.right.prio < n.prio) 
				return rotateLeft(n);
			else 
				return n;
		}
	}

	// test if node has valid ordering
	// smaller value  to the left; greater value to the right
	// highest priority as root
	boolean testNode(Node n) {
		if (n.left != null) {
			if (n.left.prio < n.prio || n.left.elem.compareTo(n.elem) > 0) 
				return false;

			return testNode(n.left);
		}

		if (n.right != null) {
			if (n.right.prio < n.prio || n.right.elem.compareTo(n.elem) < 0) 
				return testNode(n.right);
		}

		return true;
	}

	// test each node for valid Treap property
	public 	boolean testIfTreapValid() {
		if (root == null) return true;

		return testNode(root);
	}

}

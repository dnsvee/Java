package Treap;

import java.lang.Integer;
import java.lang.Comparable;
import java.util.Random;
import java.util.List;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;;
import java.util.Stack;
import java.lang.RuntimeException;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.Collection;

// a TREAP; a tree datastructure
// It's an alternative to a traditional red/black or AVL tree

// The tree remains balanced by assigning a random number to each node and
// making sure nodes with higher values (priority) are closer to the root than nodes
// with lower values (priority)

public class Treap<K extends Comparable<K>> implements Iterable<K>, Collection<K> {
	// used for generating random priorities
	Random rand = new Random();

	// Node structure	
	public class Node {
		K    elem;
		int  p; // priority of node; a random number
		Node left, right;

		public Node(K k) {
			elem = k;
			p = rand.nextInt(); // generate a random number as priority
		}
	}

	// root of the Treap
	Node root = null; 

	// size of the Treap
	int  sz   = 0;

	// Constructor
	public Treap() {
	}

	// TODO
	public Treap(Collection<? super K> c) {
	}

	// size of Treap
	public int size() {
		return sz;
	}

	// is Treap empty?
	public boolean isEmpty() {
		return size() == 0;
	}

	// convert treap to array
	public Object[] toArray() {
		Object[] arr = new Object[this.size()];

		this.forEach((K k, Integer i) -> arr[i] = k);
		return arr;
	}

	// string representation of Treap
	// example: ((null, 1, null), 3, (null, 5, ((null, 6, null), 7, null)))
	// example: ()
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("(");
		forEach((K k, Integer i) -> {
			sb.append(k.toString());
			if (i != size() - 1) sb.append(", ");
		});
		sb.append(")");
		return sb.toString();
	}

	// test if equal to another Treap or a TreeSet
	public boolean equals(Object o) {
		if (o == null || this == null) 
			return false;

		if (o == this) 
			return true;

		if (!(o instanceof Treap || o instanceof TreeSet)) 
			return false;

		Iterator<?> it1 = iterator();
		Iterator<?> it2 = ((Iterable<?>) o).iterator();

		// A treap is ordered so iterating over two treaps which are equal both will
		// produce their elements in the same order.
		
		// Two treaps are the same when the every element in the same position matches
		while (it1.hasNext() && it2.hasNext()) {
			if (!it1.next().equals(it2.next()))
				return false;
		}

		// all elements match 
		// only return true when both iterators are exhausted		
		// have been visited and compared succesfully
		return it1.hasNext() == it2.hasNext();
	}

	// search for element 'k' and return found Node or null	
	public Node search(K k) {
		if (root == null) return null;

		return search(root, k);
	}	

	// util method for search
	public Node search(Node n, K k) {
		if (n == null) return null; // node not found

		if (n.elem == k) return n; // found

		if (k.compareTo(n.elem) < 0) 
			return search(n.left,  k); // search left

		return search(n.right, k); // search right
	}

	// search for element and return true or false
	public boolean contains(K k) {
		return search(root, k) != null;
	}

	public boolean contains(Object o) {
		return contains((K) o);
	}	

	// checks if elements in K... are in Treap
	public boolean contains(K... ks) {
		for(K k : ks) {
			if (!contains(k)) 
				return false;
		}
		
		return true;
	}

	// check if all elements in c are in the Treap
	public boolean containsAll(Collection<?> c) {
		for(Object k : c) {
			if (!contains((K) k)) 
				return false;
		}
		
		return true;
	}	

	// remove element from Treap
	public boolean remove(K k) {
		int z = size();
		root = remove(k, root);
		
		// return true if element was removed else false
		return z != size();
	}

	// called by remove(K, k)
	// this method may change the structure of the Treap starting from Node n so it
	// returns the (possibly new) root of the sub-Treap it has operated on
	public Node remove(K k, Node n) {
		if (n == null) 
			return null;

		// remove in left side in element smaller
		if (k.compareTo(n.elem) < 0) {
			n.left = remove(k, n.left);
			return n;
		}

		// remove in right side if element greater
		if (n.elem.compareTo(k) < 0) {
			n.right = remove(k, n.right);
			return n;
		}

		// element == K
		
		// if node is a leaf then just remove by setting node to null and return null. 
		
		// the calling method will update the node by whatever this code returns
		if (n.left == null && n.right == null) 
			if (n.elem == k) {
				sz--;
				return null;
			} else 
				return n;

		// if only left child exist or both children exist but priority of left node
		// is higher then rotate the sub-tree to the right and move the node down in the right
		// subtree and call remove on the right node
		if (n.left != null && (n.right == null || n.left.p < n.right.p)) {
			n = rotateRight(n);
			n.right      = remove(k, n.right);
			return n;
		} 

		// do the same as above but instead rotate left
		n = rotateLeft(n);
		n.left     = remove(k, n.left);
		return n;
	}

	// remove Object o and return true if something was removed; otherwise False
	public boolean remove(Object o) {
		return remove((K) o);
	}

	// remove all elements also in c
	public boolean removeAll(Collection<?> c) {
		int z = c.size();
		removeAll((Collection<K>) c);
		return z != c.size();
	}	

	// clear the treap of all elements
	public void clear() {
		root = null;
		sz   = 0;
	}

	public int hashCode() {
		return 0;
	}

	// add all elements from c to treap
	// returns true if an element was added; otherwise false
	public boolean addAll(Collection<? extends K> c) {
		int z = size();

		for(K k : c) 
			add(k);

		return z != size();
	}

	// add all elements from arguments
	// returns true if an element was added else false
	public boolean addAll(K... c) {
		int z = size();

		for(K k : c) 
			add(k);

		return z != size();
	}

	// add elem k to treap if it doesn't exist 
	// return true if element was added else false	
	public boolean add(K k) {
		int z = size();

		// if empty tree add as root
		if (root == null) {
			root = new Node(k);
			sz++;
			return true;
		}

		// add into root
		root = add(k, root);

		return z != size();
	}

	// rotates root n to the left and right child becomes the new root
	// returns the new root	
	public Node rotateLeft(Node n) {
		Node t  = n.right;
		n.right = n.right.left;
		t.left  = n;

		return t;
	}

	// rotates root n to the right and left child becomes the new root
	// returns the new root	
	public Node rotateRight(Node n) {
		Node t = n.left;
		n.left = n.left.right;
		t.right = n;

		return t;
	}

	// called by add(K k)
	// may change the structure of the subtree 
	// returns the (possibly new) root
	public Node add(K k, Node n) {
		if (k == n.elem) 
			return n;

		// add in left side
		if (k.compareTo(n.elem) < 0) {
			if (n.left != null)  
				// add further down in subtree
				n.left = add(k, n.left);
			else {
				// add as new leaf
				sz++;
				n.left = new Node(k);
			}

			// when added as leaf move back upwards
			// and fix each level by doing the proper rotate
			if (n.left.p < n.p) 
				return rotateRight(n);
			else 
				return n;
		} else {
			// do the same thing in the other side
			if (n.right != null) 
				n.right = add(k, n.right);
			else {
				sz++;
				n.right = new Node(k);
			}

			if (n.right.p < n.p) 
				return rotateLeft(n);
			else 
				return n;
		}
	}

	// returns true if the Treap starting at 'n' is a valid Treap	
	// nodes to the left are smaller then the nodes on the right	
	// nodes closer to the root have higher priority
	boolean testNode(Node n) {
		if (n.left != null) {
			if (n.left.p < n.p || n.left.elem.compareTo(n.elem) > 0) 
				return false;

			return testNode(n.left);
		}

		if (n.right != null) {
			if (n.right.p < n.p || n.right.elem.compareTo(n.elem) < 0) 
				return false;

			return testNode(n.right);
		}

		return true;
	}

	// returns true if the treap is valid	
	// nodes to the left are smaller in value than nodes on the right	
	// nodes closer to the root have higher priority
	public 	boolean isValid() {
		if (root == null) 
			return true;

		return testNode(root);
	}

	// visit elements in order
	// call fun for each elem
	// start at Node n
	public void visit(Consumer<? super K> fun, Node n) {
		if (n.left != null) 
			visit(fun, n.left);

		fun.accept(n.elem);

		if (n.right!= null) 
			visit(fun, n.right);
	}

	// calls fun on each elem in order
	public void forEach(Consumer<? super K> fun) {
		if (root == null) 
			return;

		visit(fun, root);
	}	

	// utility method
	public int visit(BiConsumer<? super K, Integer> fun, Node n, int i) {
		if (n.left != null) 
			i = visit(fun, n.left, i);

		fun.accept(n.elem, i);
		i++;

		if (n.right!= null) 
			i = visit(fun, n.right, i);

		return i;
	}


	// calls fun on each elem in order and pass the index of the element and the element to fun
	// visit elements in order		
	public void forEach(BiConsumer<? super K, Integer> fun) {
		if (root == null) 
			return;

		visit(fun, root, 0);
	}

	// creates an iterator that will visit each element in-order
	// and can be used by an enhanced for loop
	public Iterator<K> iterator() {
		Treap<K> treap = this;

		// stack of nodes
		Stack<Node> st = new Stack<Node>();
	
		if (root != null) {	
			// init stack with all nodes that can be reached by taking only the left child
			// until hitting a leaf node
			st.push(root);
			while (st.peek().left != null) 
				st.push(st.peek().left);
		}

		return new Iterator<K>() {
			public boolean hasNext() {
				return !st.isEmpty() && treap.root != null;
			}

			public K next() {
				if (st.isEmpty() || treap.root == null) 
					return null;

				// pop the node on top of the stack
				// if this node has a right child then visit it and
				// put all the nodes reachable by taking only the left children				
				Node n = st.pop();

				if (n.right != null) {
					st.push(n.right);
					while (st.peek().left != null) 
						st.push(st.peek().left);
				}

				return n.elem;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	// write some special logic for this
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}	

	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}
}

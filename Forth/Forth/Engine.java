package Forth;

// implements a Forth interpreter in Java

import java.util.*;
import java.lang.*;
import java.nio.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.lang.*;
import java.util.function.*;
import java.util.Formatter.*;
import java.util.regex.*;
import java.lang.Math;
import java.util.AbstractMap.SimpleEntry;

public class Engine {
	// stack with values
	public Stack<Object>       Stack;

	// second stack
	public Stack<Object>       Second;

	// all compiled words and literals and builtins
	public Stack<Object>       Dict;

	// IP of current instruction running
	public int                 IP;

	// when calling a word the IP address from which it is called is stored
	// here; when the called function returns it returns to IP stored 
	// on the top of this stack
	public int[]               Trail   = new int[128];
	public int                 iTrail  = -1;

	// utility stack
	public Stack<Object>       Stuff;

	// the program that is passed for execution is split into individual words and
	// saved as a queue
	public ArrayDeque<String>  Words;

	// address of last compiled word
	public int                 top;

	// approximate comparison of doubles
	public boolean approx(double a, double b) {
		double epsilon = 0.000001;
		return Math.abs(a - b) <= ( (Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math.abs(a)) * epsilon);
	}

	// constructor
	public Engine() {
		Dict  = new Stack();
		Stack = new Stack();
		Second= new Stack();
		Stuff = new Stack();
		Words = new ArrayDeque<String>();
		IP    = 0;

		top   = -1;

		// this exception maybe throw a lot so it is cached
		RuntimeException done = new RuntimeException();

		// All precompiled builtins
		
		// this word is run when the input buffer is empty and compiling is done
		Dict.push((Consumer<Engine>) (Engine e) -> {
			throw done;
		});

		// outputs to standard output
		builtin("puts", (Engine E) -> { 
			System.out.printf("%s", Stack.pop());
			IP++;
		});

		// outputs a string followed by a newline
		builtin("putnl", (Engine E) -> { 
			System.out.printf("%s\n", Stack.pop());
			IP++;
		});

		// ( -- ) outputs newline to standard output
		builtin("nl", (Engine E) -> { 
			System.out.println();
			IP++;
		});

		// ( -- ) outputs a space to standard output
		builtin("space", (Engine E) -> { 
			System.out.printf(" ");
			IP++;
		});

		// ( -- b ) macro; parses a word from the input buffer and puts it on the stack
		macro("\"", (Engine E) -> { 
			parse();
			literal(Stack.pop());
		});

		// ( a -- b ) b is length of a:string
		builtin("strlen", (Engine E) -> { 
			Stack.push((double) ((String) Stack.pop()).length());
			IP++;
		});

		// ( b a -- ... ) split b:string using a:regex; ... are substrings
		builtin("split", (Engine E) -> { 
			String r = (String) Stack.pop();
			String s = (String) Stack.pop();
			String a[] = s.split(r);
			if (a != null) {
				for(int i = 0; i < a.length; i++) {
					Stack.push(a[i]);
				}
			}
			IP++;
		});

		// ( b a -- c ) c:string is b:string + a:string
		builtin("..", (Engine E) -> { 
			String s1 = (String) Stack.pop();
			String s0 = (String) Stack.pop();
			Stack.push(s0 + s1);
			IP++;
		});

		// ( a b -- c )    c is a + b
		builtin("+",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(a + b);
			IP++;
		});

		// ( a b -- c )    c is a - b
		builtin("-",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(a - b);
			IP++;
		});

		// ( a b -- c )	c is a * b
		builtin("*",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(a * b);
			IP++;
		});

		// ( a b -- c )	c is a / b
		builtin("/",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(a / b);
			IP++;
		});

		// ( a b -- c ) c is max(a, b)
		builtin("max",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(Math.max(a, b));
			IP++;
		});

		// ( a b -- c ) c is min(a, b)
		builtin("min",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(Math.min(a, b));
			IP++;
		});

		// ( a b -- c ) c is pow(a, b)
		builtin("pow",    (Engine E) -> {
			Double b = (Double) Stack.pop();
			Double a = (Double) Stack.pop();
			Stack.add(Math.pow(a, b));
			IP++;
		});

		// ( a b -- c) c is a > b
		builtin(">",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b) > 0);
			IP++;
		});

		// ( a b -- c) c is a >= b
		builtin(">=",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b) >= 0);
			IP++;
		});

		// ( a b -- c) c is a < b
		builtin("<",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b) < 0);
			IP++;
		});

		// ( a b -- c) c is a <= b
		builtin("<=",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b) <= 0);
			IP++;
		});

		// ( a b -- c) c is a == b
		builtin("==",    (Engine E) -> {
			Object b = Stack.pop();
			Object a = Stack.pop();
			Stack.add(a.equals(b));
			IP++;
		});

		// ( a b -- c) c is a != b
		macro("!=", (Engine E) -> {
			compile("==");
			compile("not");
		});

		// ( a -- b ) b is sqrt(a)
		builtin("sqrt",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.sqrt(a));
			IP++;
		});

		// ( a -- b ) b is abs(a)
		builtin("abs",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.abs(a));
			IP++;
		});

		// ( a b -- c )	c is cos(a)
		builtin("cos",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.cos(a));
			IP++;
		});

		// ( a b -- c )	c is sin(a)
		builtin("sin",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.sin(a));
			IP++;
		});

		// ( a b -- c )	c is tan(a)
		builtin("tan",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.tan(a));
			IP++;
		});

		// ( a -- b ) b is log(a)
		builtin("log",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.log(a));
			IP++;
		});

		// ( a -- b ) b is floor(a)
		builtin("floor",    (Engine E) -> {
			Double a = (Double) Stack.pop();
			Stack.add(Math.floor(a));
			IP++;
		});

		// formats stack elements as a string using String.format
		builtin("fmt",    (Engine E) -> {
			String a = (String) Stack.pop();
			int sz = 0;
			for(int i = 0; i < a.length(); i++) 
				if (a.charAt(i) == '%') 
					sz++;

			Object[] arr = new Object[sz];
			
			for(int i = 0; i < sz; i++)
				arr[sz - i - 1] = Stack.pop();

			Stack.add(String.format(a, arr));

			IP++;
		});

		// convert top element to string
		builtin("str!",    (Engine E) -> {
			Stack.add(Stack.pop().toString());
			IP++;
		});

		// sets named variable with value from stack
		macro("!", (Engine E) -> {
			parse();
			int i = find((String) Stack.pop());

			if (i == -1)
				throw new RuntimeException("!: var not found");

			Dict.push((Consumer<Engine>) (Engine) -> {
				Dict.set(i + 3, Stack.pop());
				IP++;
			});
		});

		// creates variable and initialize it with value from top of stack
		macro("var", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine e) -> {
				IP = IP + 5;
			});

			int n = Dict.size();
			parse();
			Dict.add((String) Stack.pop());
			Dict.add(3); // type 3 variable
			Dict.add(E.top);
			int v = Dict.size();
			Dict.add(999); // dummy value
			E.top = n;

			// sets when created with value from stack
			Dict.push((Consumer<Engine>) (Engine E0) -> {
				E0.Dict.set(v, E0.Stack.pop());
				IP++;
			});
		});

		// do while repeat
		macro("do", (Engine E) -> {
			Stuff.push(Dict.size());
		});

		macro("while", (Engine E) -> {
			Stuff.push(Dict.size() + 1);

			Dict.push((Consumer<Engine>) (Engine) -> {
				Boolean b = (Boolean) Stack.pop();
				if (b) 
					IP += 2;
				else
					IP = (Integer) Dict.get(IP + 1);
			});
			Dict.push(null);
		});

		macro("repeat", (Engine E) -> {
			int b = (int) Stuff.pop();
			int a = (int) Stuff.pop();

			Dict.push((Consumer<Engine>) (Engine) -> {
				IP = a;
			});

			Dict.set(b, Dict.size());
		});

		// if then else
		macro("if", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine) -> {
				Boolean b = (Boolean) Stack.pop();
				if (b) 
					IP += 2;
				else
					IP++;
			});
			Stuff.push(Dict.size());
			Dict.push(null);
		});

		macro("else", (Engine E) -> {
			int a = (Integer) Stuff.pop();

			Stuff.push(Dict.size());
			Dict.push(null);

			int sz = Dict.size();
			Dict.set(a, (Consumer<Engine>) (Engine) -> {
				IP = sz;
			});
		});

		macro("then", (Engine E) -> {
			int a = (int) Stuff.pop();
			int sz = Dict.size();
			Dict.set(a, (Consumer<Engine>) (Engine) -> {
				IP = sz;
			});
		});

		// displays the names of all compiled words
		builtin("words", (Engine E) -> {
			int cur = top;
			while (cur != -1) {
				System.out.println((String) Dict.get(cur));
				cur = (int) Dict.get(cur + 2);
			}
			IP++;
		});

		// duplicates top element
		builtin("dup",    (Engine E) -> {
			Stack.push(Stack.peek());
			IP++;
		});

		// duplicates element under the top
		builtin("over",    (Engine E) -> {
			Stack.push(Stack.get(Stack.size() - 2));
			IP++;
		});

		// drop top element
		builtin("drop",    (Engine E) -> {
			Stack.pop();
			IP++;
		});

		// remove element under the top
		builtin("nip",    (Engine E) -> {
			Stack.remove(Stack.size() - 2);
			IP++;
		});

		// move to second stack
		builtin("toss",    (Engine E) -> {
			Second.push(Stack.pop());
			IP++;
		});

		// grab from second stack
		builtin("grab",    (Engine E) -> {
			Stack.push(Second.pop());
			IP++;
		});

		// swao top of both stacks
		builtin("flip",    (Engine) -> {
			Object o = Stack.pop();
			Stack.push(Second.pop());
			Second.push(o);
			IP++;
		});

		// swap top two elements of stack
		builtin("swap",    (Engine E) -> {
			Object a = Stack.pop();
			Object b = Stack.pop();
			Stack.push(a);
			Stack.push(b);
			IP++;
		});

		// creates an empty dictionary
		builtin("map!",    (Engine E) -> {
			Stack.push(new HashMap<Object, Object>());
			IP++;
		});

		// creates an empty list constructor
		builtin("list!",    (Engine E) -> {
			Stack.push(new Stack<Object>());
			IP++;
		});

		// used as a marker that is placed on the stack; certain words use
		// this as an end marker 		
		class Marker {}

		// starts a list literal
		macro("(",    (Engine E) -> {
			literal(new Marker());
		});

		// list literal; since the list constructor looks like ( 1 2 3 4 5 ) in the
		// source code the expectation is that 1 is the head and 5 is the tail
		// but because Forth uses a stack the 5 is actually on top of the stack
		// and added first to the new list and 1 is added as the last item
		// because of this the list when build is reversed 
		builtin(")",    (Engine E) -> {
			Stack lst = new Stack();
			Object o = Stack.pop();

			while (!(o instanceof Marker)) {
				lst.add(o);
				o = Stack.pop();
			}

			for(int i = 0; i < lst.size() / 2; i++) {
				Object tmp = lst.get(i);
				lst.set(i, lst.get(lst.size() - 1 - i));
				lst.set(lst.size() - 1 - i, tmp);
			}

			Stack.push(lst);
			IP++;
		});

		// set! ( -- a ) a is a new set
		builtin("set!",    (Engine E) -> {
			Stack.push(new HashSet<Object>());
			IP++;
		});

		// starts a set literal, ex.: (+ 1 2 3 +)
		macro("(+",    (Engine E) -> {
			literal(new Marker());
		});

		// ends of the set literal
		builtin("+)",    (Engine E) -> {
			HashSet set = new HashSet();
			Object o = Stack.pop();
			while (!(o instanceof Marker)) {
				set.add(o);
				o = Stack.pop();
			}
			Stack.push(set);
			IP++;
		});

		// get value from map associated with the key
		builtin("mget*",    (Engine E) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			Object a = o.get(Stack.pop());
			if (a != null) {
				Stack.pop();
				Stack.push(a);
			}
			IP++;
		});

		builtin("mget",    (Engine E) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			Object a = o.get(Stack.pop());
			Stack.push(a);
			IP++;
		});

		// get value from list using specified index		
		builtin("aget",    (Engine E) -> {
			Stack<Object> o = (Stack<Object>) Stack.pop();
			Stack.push(o.get((Integer) Stack.pop()));
			IP++;
		});

		// set a value of specified index
		builtin("aset",    (Engine E) -> {
			Stack<Object> o = (Stack<Object>) Stack.pop();
			o.set(((Double) Stack.pop()).intValue(), Stack.pop() );
			IP++;
		});

		// sadd ( b a - ) add value b to a:collection
		builtin("sadd",    (Engine E) -> {
			((HashSet<Object>) Stack.pop()).add(Stack.pop());
			IP++;
		});

		// Check if element part of set
		builtin("shas",    (Engine) -> {
			HashSet<Object> s = (HashSet<Object>) Stack.pop();
			Stack.push(s.contains(Stack.pop()));
			IP++;
		});

		// Put key/value in map
		builtin("mput",    (Engine E) -> {
			HashMap<Object, Object> m = (HashMap<Object, Object>) Stack.pop();
			m.put(Stack.pop(), Stack.pop());
			IP++;
		});

		// Remove element from set
		builtin("sdel",    (Engine E) -> {
			HashSet s = (HashSet) Stack.pop();
			s.remove(Stack.pop());
			IP++;
		});

		// Remove element from map
		builtin("mdel",    (Engine E) -> {
			HashMap<Object, Object> m = (HashMap) Stack.pop();
			m.remove(Stack.pop());
			IP++;
		});

		// ( b a -- b ) adds a:collection to b:collection
		builtin("caddall", (Engine) -> {
			Collection a = (Collection) Stack.pop();
			Collection b = (Collection) Stack.pop();
			a.addAll(b);
			Stack.push(a);
		        IP++;	
		});

		// union of sets
		macro("sunion", (Engine E) -> {
			compile("caddall");
		});

		// concat lists
		macro("aconcat", (Engine E) -> {
			compile("caddall");
		});

		// clears a collection
		builtin("cclear",    (Engine E) -> {
			((Collection) Stack.pop()).clear();
		        IP++;	
		});

		macro("sclear", (Engine E) -> compile("cclear"));

		macro("aclear", (Engine E) -> compile("cclear"));

		builtin("mclear", (Engine E) -> {
			((HashMap<Object, Object>) Stack.pop()).clear();
			IP++;
		});

		// Push to array
		builtin("apush",    (Engine E) -> {
			((Stack) Stack.pop()).push(Stack.pop());
			IP++;
		});

		// Pop from array
		builtin("apop",    (Engine E) -> {
			Stack s = (Stack) Stack.pop();
			Stack.push(s.pop());
			IP++;
		});

		// Size of collection
		builtin("size",    (Engine E) -> {
			Stack.push((double) ((Collection) Stack.pop()).size());
			IP++;
		});

		macro("asize", (Engine E) -> compile("size"));
		macro("ssize", (Engine E) -> compile("size"));

		builtin("msize", (Engine E) -> {
			HashMap<Object, Object> m = (HashMap) Stack.pop();
			Stack.push((double) m.size());
			IP++;
		});

		// Creates an iterator from an Iterable
		builtin("iter!",    (Engine E) -> {
			Iterable c = (Iterable) Stack.pop();
			Stack.push(c.iterator());
			IP++;
		});

		// If iterator has a next element replaces b with c otherwise c remains 
		builtin("next",    (Engine E) -> {
			Iterator i = (Iterator) Stack.pop();

			if (i.hasNext()) {
				Stack.pop();
				Stack.push(i.next());
			} 

			IP++;
		});

		// implements named functions
		macro("def", (Engine E) -> {
			Stuff.push(Dict.size());
			Dict.push(null);
			parse();
			String s = (String) Stack.pop();
			Dict.add(s);
			Dict.add(4);   // Type 4 == user defined function
			Dict.add(top);
			top = Dict.size() - 3;
		});

		// end of definition
		macro("end", (Engine E) -> {
			leave();
			Dict.set((Integer) Stuff.pop(), compile_jump(Dict.size()));
		});

		// implements anonymous functions
		macro("[", (Engine E) -> {
			Stuff.push(Dict.size());
			Dict.push(null);
		});

		macro("]", (Engine E) -> {
			leave();
			int i = (Integer) Stuff.pop();
			int sz = Dict.size();
			Dict.set(i, (Consumer<Engine>) (Engine) -> {
				IP = sz;
			});
			literal(i + 1);
		});

		builtin("call", (Engine E) -> {
			Trail[++iTrail] = IP + 1;
			IP = (Integer) Stack.pop();
		});

		// class? ( a -- b ) b is the class name of a:object
		builtin("class?", (Engine E) -> { 
			Stack.push(Stack.pop().getClass());
			IP++;
		});

		// clears stack
		builtin(".clear", (Engine E) -> { 
			Stack.clear();
			IP++;
		});

		// all constants
		constant("true",  true);
		constant("false", false);
		constant("PI",    Math.PI);
		constant("e",     Math.E);
		constant("null",  null);

		// logical operators
		builtin("and", (Engine) -> {
			Stack.push(((Boolean) Stack.pop()) && ((Boolean) Stack.pop()));
			IP++;
		});

		builtin("or", (Engine) -> {
			Stack.push(((Boolean) Stack.pop()) || ((Boolean) Stack.pop()));
			IP++;
		});

		builtin("not", (Engine) -> {
			Stack.push(!((Boolean) Stack.pop()));
			IP++;
		});

		// creates a pair of elements
		builtin("pair!", (Engine) -> {
			Pair p = new Pair(Stack.pop(), Stack.pop());
			Stack.push(p);
			IP++;
		});

		macro(",", (Engine E) -> {
			compile("pair!");
		});

		// unpacks elements from pair
		builtin("**", (Engine) -> {
			Pair p = (Pair) Stack.pop();
			Stack.push(p.first);
			Stack.push(p.second);
			IP++;
		});

		// matches a string with a regex pattern 
		builtin("matches", (Engine) -> {
			Pattern p = Pattern.compile((String) Stack.pop());
			Matcher m = p.matcher((String) Stack.pop());
			boolean b = m.matches();

			for(int i = 1; i <= m.groupCount(); i++) 
				Stack.push(m.group(i));

			Stack.push(b);
			IP++;
		});

	}

	// a pair class
	class Pair implements Comparable<Object> {
		public Comparable first;
		public Comparable second;

		Pair(Object b, Object a) {
			first = (Comparable) a;
			second = (Comparable) b;
		}

		// compares pairs as equal if both elements are the same
		public int compareTo(Object o) {
			if (getClass() != o.getClass()) {
				int h1 = hashCode();
				int h2 = o.hashCode();				

				if (h1 < h2) return -1;
				if (h1 > h2) return 1;
				if (h1 == h2) return 0;
			}

			Pair p = (Pair) o;

			int a = ((Comparable) first).compareTo(p.first);

			if (a < 0)
				return -1;

			if (a > 0)
				return 1;

			int b = ((Comparable) second).compareTo(p.second);

			if (b < 0)
				return -1;

			if (b > 0)
				return 1;

			return 0;
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;

			if (o == null)
				return true;

			if (o.getClass() != getClass()) 
				return false;

			Pair p = (Pair) o;

			return first.equals(p.first) && second.equals(p.second);
		}

		public int hashCode() {
			return Objects.hash(first, second);
		}

		public String toString() {
			return "(" + first.toString() + ", " + second.toString() + ")";
		}
	}

	// compiles a word into the dictionary that jumps to the first instruction of a word
	public void enter(int loc) {
		Dict.push((Consumer<Engine>) (Engine) -> {
			Trail[++iTrail] = IP + 1;
			IP = loc;
		});
	}

	// compiles a jump instruction that jumps to a set location
	Consumer<Engine> compile_jump(int loc) {
		return (Consumer<Engine>) (Engine) -> {
			IP = loc;
		};
	}

	// Compiles a word in the dictionary
	// First push the name of word to the dictionary
	// Then push the type of word to the dictionary
	// 	type 1 == builtin
	// 	type 2 == macro
	// 	type 3 == variable
	// 	type 4 == user defined
	// 	type
	// Push the index of the previous word
	// Push the Consumer object that implements the word
	// Adjust pointer 'top' to point at this word
	// TODO: add help string
	public void compile(String s, Consumer<Engine> c, int type) {
		Dict.add(s);
		Dict.add(type);   
		Dict.add(top);
		Dict.add(c);
		top = Dict.size() - 4;
	}

	// Compile a literal (string or number)
	public void literal(Object o) {
		Dict.push((Consumer<Engine>) (Engine) -> {
			Stack.push(o);
			IP++;
		});
	}

	// compiles a constant with name n and value val
	public void constant(String n, Object val) {
		macro(n, (Engine) -> {
			literal(val);
		});
	};

	// compile a builtin word
	public void builtin(String s, Consumer<Engine> c) {
		compile(s, c, 1);
	}

	// compile a macro word 
	public void macro(String s, Consumer<Engine> c) {
		compile(s, c, 2);
	}

	// find a word with the name of s
	// return location otherwise -1 if not found
	public int find(String s) {
		int cur = top;
		while (cur != -1) {
			if (((String) Dict.get(cur)).equals(s)) 
				return cur;

			cur = (int) Dict.get(cur + 2);
		}
		return -1;
	}

	// compiles word that pushes object unto stack which is at location 'l' in the dictionary	
	public void putvar(int l) {
		Dict.push((Consumer<Engine>) (Engine) -> {
			Stack.push(Dict.get(l + 3));
			IP++;
		});
	}

	// compile word named `name`
	public void compile(String name) {
		int loc  = find(name);
		if (loc == -1)
			throw new RuntimeException("Forth::compile(String name): loc == -1");

		Dict.push(Dict.get(loc + 3));
	}

	// compile all definitions in the input buffer and runs all the top level words
	public void eval() {
		preparse();

		// this points to a word that throws an exception
		Trail[++iTrail] = 0;

		// start executing here when done compiling
		int start = Dict.size();

		// compile all words
		while (Words.size() > 0) {
			parse();
			String s = (String) Stack.pop();
			int loc  = find(s);

			// find word 
			if (loc >= 0) {
				// word found; do something depending on type
				Integer t = (Integer) Dict.get(loc + 1);

				// words is a builtin				
				if (t == 1) {
					Dict.push(Dict.get(loc + 3));
					continue;
				}

				// word is a macro; don't compile but run the word				
				if (t == 2) {
					((Consumer<Engine>) Dict.get(loc + 3)).accept(this);
					continue;
				}

				// word is a named variable; push value unto stack				
				if (t == 3) {
					putvar(loc);
					continue;
				}

				// word is user defined name; compile an enter instruction
				if (t == 4) {
					enter(loc + 3);
					continue;
				}
			}

			// word not in dictionary
			try {
				// try to parse as integer
				Integer i = Integer.parseInt(s);
				literal(i.doubleValue());
				continue;
			} catch (NumberFormatException err) {}

			try {
				// try to parse as float
				Double d = Double.parseDouble(s);
				literal(d);
				continue;
			} catch (NumberFormatException err2) {}
			
			// compile as literal string
			literal(s);
		}

		// after input buffer is parsed and all the words are compiled
		// run the top-level words in the buffer 
		
		// return to previous address; eval can be called from Forth itself
		leave();

		// execute from the place where compiling started and run until exception thrown
		IP = start;

		run();
	}

	// compiles a leave instruction; returns back to calling word
	public void leave() {
		Dict.push((Consumer<Engine>) (Engine E) -> {
			IP = Trail[iTrail--];
		});
	}

	// run main loop
	public void run() {
		while (true) 
			((Consumer<Engine>) Dict.get(IP)).accept(this);
	}

	// shifts word from input buffer and puts it on the stack
	public void parse() {
		if (Words.size() == 0) 
			throw new RuntimeException();

		Stack.push(Words.removeFirst());
	}

	// split input buffer into seperate words 
	// space is the used as a elimiter except when preceded by a backlash
	// replace \s, \<space> and \n escape sequences
	public void preparse() {
		for(String l : ((String) Stack.pop()).split("\\n")) {
			l = l.split("^#|\\s#")[0]; // delete 
			for(String s : l.split("(?<!\\\\)\\s+")) {
				s = s.replace("\\ ", " ");
				s = s.replace("\\n", " ");
				s = s.trim();

				if (s.length() > 0) 
					Words.addLast(s);
			}
		}
	}

	// read a line from stdin
	public String readLine() throws IOException {
		Scanner s = new Scanner(System.in);
		return s.nextLine();
	}
}


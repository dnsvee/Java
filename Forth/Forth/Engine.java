package Forth;

import java.util.*;
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
	// running stack
	public Stack<Object>       Stack;

	// second stack
	public Stack<Object>       Second;

	// all compiled words and literals and builtins
	public Stack<Object>       Dict;

	// IP of current instruction running
	public int                 IP;

	// used for returning from called word
	public int[]               Trail   = new int[128];
	public int                 iTrail  = -1;

	// util. stack
	public Stack<Object>       Stuff;

	// input buffer split into words
	public ArrayDeque<String>  Words;

	// top of last compiled words
	public int                 top;

	// constructor
	public Engine() {
		Dict  = new Stack();
		Stack = new Stack();
		Second= new Stack();
		Stuff = new Stack();
		Words = new ArrayDeque<String>();
		IP    = 0;

		top   = -1;

		// TOS == top of stack
		//
		// this word is run when input buffer is empty and no more instructions 
		Dict.push((Consumer<Engine>) (Engine e) -> {
			throw new RuntimeException();
		});

		// ( a -- ) outputs element a to standard output
		builtin("puts", (Engine E) -> { 
			System.out.printf("%s", Stack.pop());
			IP++;
		});

		// ( a -- ) outputs element a followed by a newline to standard output
		builtin("putnl", (Engine E) -> { 
			System.out.printf("%s\n", Stack.pop());
			IP++;
		});

		// formats stack elements and outputs them to stdout
		macro("putf", (Engine E) -> { 
			compile("fmt");
			compile("puts");
		});


		macro("\"", (Engine E) -> { 
			literal(Words.pop());
		});

		// display a newline
		builtin("nl", (Engine E) -> { 
			System.out.println();
			IP++;
		});

		// displays a space
		builtin("space", (Engine E) -> { 
			System.out.printf(" ");
			IP++;
		});

		// all constants
		constant("true",  true);
		constant("false", false);
		constant("PI",    Math.PI);
		constant("e",     Math.E);
		constant("null",  null);

		// ( a b -- c )    c is a + b
		builtin("+",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() + b.doubleValue());
			IP++;
		});

		// ( a b -- c )    c is a - b
		builtin("-",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() - b.doubleValue());
			IP++;
		});

		// ( a b -- c )	c is a * b
		builtin("*",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() * b.doubleValue());
			IP++;
		});

		// ( a b -- c )	c is a / b
		builtin("/",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() / b.doubleValue());
			IP++;
		});

		// ( a b -- c ) c is max(a, b)
		builtin("max",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.max(a.doubleValue(), b.doubleValue()));
			IP++;
		});

		// ( a b -- c ) c is min(a, b)
		builtin("min",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.min(a.doubleValue(), b.doubleValue()));
			IP++;
		});

		// ( a b -- c ) c is pow(a, b)
		builtin("**",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.pow(a.doubleValue(), b.doubleValue()));
			IP++;
		});

		// ( a b -- c) c is a > b
		builtin(">",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() > b.doubleValue());
			IP++;
		});

		// ( a b -- c) c is a >= b
		builtin(">=",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() >= b.doubleValue());
			IP++;
		});

		// ( a b -- c) c is a < b
		builtin("<",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() < b.doubleValue());
			IP++;
		});

		// ( a b -- c) c is a <= b
		builtin("<=",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() <= b.doubleValue());
			IP++;
		});

		// ( a b -- c) c is a.compareTo(b)
		builtin("cmp",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b));
			IP++;
		});

		// ( a b -- c) c is a == b
		builtin("==",    (Engine E) -> {
			Object b = Stack.pop();
			Object a = Stack.pop();
			Stack.add(a.equals(b));
			IP++;
		});

		// ( a -- b ) b is sqrt(a)
		builtin("sqrt",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sqrt(a.doubleValue()));
			IP++;
		});

		// ( a -- b ) b is abs(a)
		builtin("abs",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.abs(a.doubleValue()));
			IP++;
		});

		// ( a b -- c )	c is cos(a)
		builtin("cos",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.cos(a.doubleValue()));
			IP++;
		});

		// ( a b -- c )	c is sin(a)
		builtin("sin",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sin(a.doubleValue()));
			IP++;
		});

		// ( a b -- c )	c is tan(a)
		builtin("tan",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.tan(a.doubleValue()));
			IP++;
		});

		// ( a -- b ) b is log(a)
		builtin("log",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.log(a.doubleValue()));
			IP++;
		});

		// ( a -- b ) b is floor(a)
		builtin("floor",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.floor(a.doubleValue()));
			IP++;
		});

		// ( ... a b -- c ) ... is a list of elements of size a:number and b is a format string
		// 		    c is the result of formatting using String.format 
		builtin("fmt",    (Engine E) -> {
			String a = (String) Stack.pop();
			int sz = ((Number) Stack.pop()).intValue();
			Object[] arr = Stack.subList(Stack.size() - sz, Stack.size()).toArray();

			for(int i = 0; i < sz; i++)
				Stack.pop();

			Stack.add(String.format(a, arr));
			IP++;
		});

		// sets named variable with value from stack
		// example: 11 ! number
		macro("!", (Engine E) -> {
			int i = find(E.Words.pop());

			if (i == -1)
				throw new RuntimeException("!: var not found");

			Dict.push((Consumer<Engine>) (Engine) -> {
				Dict.set(i + 3, Stack.pop());
				IP++;
			});
		});

		// creates variable and initialize it with number from top of stack
		// example: 42 var meaning
		macro("var", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine e) -> {
				IP = IP + 5;
			});

			int n = Dict.size();
			Dict.add(Words.pop());
			Dict.add(3);   // type 
			Dict.add(E.top);
			int v = Dict.size();
			Dict.add(999);
			E.top = n;

			Dict.push((Consumer<Engine>) (Engine E0) -> {
				E0.Dict.set(v, E0.Stack.pop());
				IP++;
			});
		});

		macro("", (Engine E) -> {
		});

		// calls lambda for each element in set
		macro("@", (Engine E) -> {
		});

		// calls a lambda
		builtin("call", (Engine E) -> {
			Trail[++iTrail] = IP + 1;
			IP = (Integer) Second.pop();
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

		// displays all known words
		builtin("words", (Engine E) -> {
			int cur = top;
			while (cur != -1) {
				System.out.println((String) Dict.get(cur));
				cur = (int) Dict.get(cur + 2);
			}
			IP++;
		});

		// duplicates TOS element
		builtin("dup",    (Engine E) -> {
			Stack.push(Stack.peek());
			IP++;
		});

		// duplicates element under TOS
		builtin("over",    (Engine E) -> {
			Stack.push(Stack.get(Stack.size() - 2));
			IP++;
		});

		// drop element TOS
		builtin("drop",    (Engine E) -> {
			Stack.pop();
			IP++;
		});

		// remove element under TOS
		builtin("nip",    (Engine E) -> {
			Stack.remove(Stack.size() - 2);
			IP++;
		});

		// move to second stack
		builtin("raise",    (Engine E) -> {
			Second.push(Stack.pop());
			IP++;
		});

		// bring back from second stack
		builtin("lower",    (Engine E) -> {
			Stack.push(Second.pop());
			IP++;
		});

		// rotate top of both stacks
		builtin("twirl",    (Engine) -> {
			Object o = Stack.pop();
			Stack.push(Second.pop());
			Second.push(o);
			IP++;
		});

		// swap top two elements
		builtin("swap",    (Engine E) -> {
			Object a = Stack.pop();
			Object b = Stack.pop();
			Stack.push(a);
			Stack.push(b);
			IP++;
		});

		// map constructor
		builtin("map!",    (Engine E) -> {
			Stack.push(new HashMap<Object, Object>());
			IP++;
		});

		// list constructor
		builtin("list!",    (Engine E) -> {
			Stack.push(new Vector<Object>());
			IP++;
		});

		// set constructor
		builtin("set!",    (Engine E) -> {
			Stack.push(new HashSet<Object>());
			IP++;
		});

		// map::get
		builtin("get",    (Engine E) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			Stack.push(o.get(Stack.pop()));
			IP++;
		});

		// arraylist::at
		builtin("at",    (Engine E) -> {
			Vector<Object> o = (Vector<Object>) Stack.pop();
			Stack.push(o.get((Integer) Stack.pop()));
			IP++;
		});

		// arraylist::set
		builtin("set",    (Engine E) -> {
			Vector<Object> o = (Vector<Object>) Stack.pop();
			o.set((Integer) Stack.pop(), Stack.pop() );
			IP++;
		});

		// collection:add
		builtin("add",    (Engine E) -> {
			Collection c = (Collection) Stack.pop();
			c.add(Stack.pop());
			IP++;
		});

		// collection::has (contains)
		builtin("has",    (Engine E) -> {
			Collection c = (Collection) Stack.pop();
			Stack.push(c.contains(Stack.pop()));
			IP++;
		});

		// map::put
		builtin("put",    (Engine E) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			o.put(Stack.pop(), Stack.pop());
			IP++;
		});

		// arraylist::remove index
		builtin("cut",    (Engine E) -> {
			// remove from and to
			Vector<Object> o = (Vector<Object>) Stack.pop();
			o.remove((int) Stack.pop());
			IP++;
		});

		// collection:remove
		builtin("del",    (Engine E) -> {
			Collection o = (Collection) Stack.pop();
			o.remove(Stack.pop());
			IP++;
		});

		// ( a b -- a ) adds b:collection to a:collection
		builtin("concat",    (Engine E) -> {
			((Collection) Stack.peek()).addAll((Collection) Stack.pop());
		        IP++;	
		});

		// ( a -- ) clears a:collection
		builtin("clear",    (Engine E) -> {
			Collection c = (Collection) Stack.pop();
			c.clear();
		        IP++;	
		});

		class Marker {}

		// set literal
		macro("(+",    (Engine E) -> {
			literal(new Marker());
		});

		// build set
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

		// list literal
		macro("(",    (Engine E) -> {
			literal(new Marker());
		});

		// list literal; reverses list when done
		builtin(")",    (Engine E) -> {
			Vector lst = new Vector();
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

		// push to list
		builtin("push",    (Engine E) -> {
			((Vector) Stack.pop()).add(Stack.pop());
			IP++;
		});

		// pop from list
		builtin("pop",    (Engine E) -> {
			Vector c = (Vector) Stack.pop();
			Stack.push(c.remove(c.size() - 1));
			IP++;
		});

		// size of collection
		builtin("size",    (Engine E) -> {
			Stack.push(((Collection) Stack.pop()).size());
			IP++;
		});

		// creates an iterator from a collection
		builtin("iter!",    (Engine E) -> {
			Collection c = (Collection) Stack.pop();
			Stack.push(c.iterator());
			IP++;
		});

		// replaces element on top of stack with next element from iterator if exists
		builtin("next",    (Engine E) -> {
			Iterator i = (Iterator) Stack.pop();

			if (i.hasNext()) {
				Stack.pop();
				Stack.push(i.hasNext());
			} 

			IP++;
		});

		// ( a -- b ) b is the class name of a:object
		builtin("class?", (Engine E) -> { 
			Stack.push(Stack.pop().getClass());
			IP++;
		});
	}

	// compile a builtin word
	// fix: add help string
	public void compile(String s, Consumer<Engine> c, int type) {
		Dict.add(s);
		Dict.add(type);   // 1 bultin, 2 macro, 3 variable
		Dict.add(top);
		Dict.add(c);
		top = Dict.size() - 4;
	}

	// compile a literal word unto the dictionary
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

	// compile a builtin word
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

	public void putvar(int l) {
		Dict.push((Consumer<Engine>) (Engine) -> {
			Stack.push(Dict.get(l + 3));
			IP++;
		});
	}


	public void compile(String name) {
		int loc  = find(name);
		if (loc == -1)
			throw new RuntimeException("Forth::compile(String name): loc == -1");

		Dict.push(Dict.get(loc + 3));
	}

	// compile all words and then run the program
	public void eval() {
		preparse();

		Trail[++iTrail] = 0;
		// start executing here
		int start = Dict.size();

		// compile all words
		while (Words.size() > 0) {
			String s = Words.pop();
			int loc  = find(s);
			if (loc >= 0) {
				// word found; compile
				Integer t = (Integer) Dict.get(loc + 1);
				if (t == 1) {
					Dict.push(Dict.get(loc + 3));
					continue;
				}

				// word found: run
				if (t == 2) {
					((Consumer<Engine>) Dict.get(loc + 3)).accept(this);
					continue;
				}

				// var found: put value on stack
				if (t == 3) {
					putvar(loc);
					continue;
				}
			}
			// if macro goto run loop

			try {
				// try to parse as integer
				Integer i = Integer.parseInt(s);
				literal(i);
				continue;
			} catch (NumberFormatException err) {}

			try {
				// try to parse as float
				Float f = Float.parseFloat(s);
				literal(f);
				continue;
			} catch (NumberFormatException err2) {}
			
			literal(s);
		}

		leave();

		// execute from the place where compiling started and run until exception thrown
		IP = start;
		run();
	}

	// compiles a leave instruction
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
	// space is the used delimiter except when preceded by a backlash
	// replace \s, \<space> and \n escape sequences
	public void preparse() {
		for(String s : ((String) Stack.pop()).split("(?<!\\\\)\\s+")) {
			s = s.replace("\\ ", " ");
			s = s.replace("\\s", " ");
			s = s.replace("\\n", " ");
			Words.add(s);
		}
	}

	// read a line from stdin
	public String readLine() throws IOException {
		Scanner s = new Scanner(System.in);
		return s.nextLine();
	}
}


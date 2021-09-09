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
		SimpleEntry<Integer, String> entry = new SimpleEntry<>(1, "abc");
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

		// displays TOS as string
		builtin("puts", (Engine E) -> { 
			System.out.printf("%s\n", Stack.pop());
			IP++;
		});

		// displays TOS as string
		builtin("class?", (Engine E) -> { 
			Stack.push(Stack.pop().getClass());
			IP++;
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

		// true constant
		constant("true",  true);
		constant("false", false);
		constant("PI",    Math.PI);
		constant("e",     Math.E);

		// add two numbers
		// a b + => <a + b>
		builtin("+",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() + b.doubleValue());
			IP++;
		});

		// a b  - => <a - b>
		builtin("-",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() - b.doubleValue());
			IP++;
		});

		// a b * => <a * b>
		builtin("*",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() * b.doubleValue());
			IP++;
		});

		// a b / => <a / b>
		builtin("/",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() / b.doubleValue());
			IP++;
		});

		// a b max => <max(a, b)>
		builtin("max",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.max(a.doubleValue(), b.doubleValue()));
			IP++;
		});

		// a b min => <min(a, b)>
		builtin("min",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.min(a.doubleValue(), b.doubleValue()));
			IP++;
		});

		// a b ** => <pow(a, b)>
		builtin("**",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.pow(a.doubleValue(), b.doubleValue()));
			IP++;
		});

		// works only on numbers
		// a b > => <a > b:boolean>
		builtin(">",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() > b.doubleValue());
			IP++;
		});

		// works only on numbers
		// a b < => <a < b:boolean>
		builtin("<",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() < b.doubleValue());
			IP++;
		});

		// a b cmp => <a.compareTo(b)>
		// compares anything that implements Comparable
		builtin("cmp",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b));
			IP++;
		});

		// a b == => <a.equals(b)>
		// compares anything that implements Comparable
		builtin("==",    (Engine E) -> {
			Object b = Stack.pop();
			Object a = Stack.pop();
			Stack.add(a.equals(b));
			IP++;
		});

		// a sqrt => <sqrt(a)>
		builtin("sqrt",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sqrt(a.doubleValue()));
			IP++;
		});

		// a abs => <abs(a)>
		builtin("abs",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.abs(a.doubleValue()));
			IP++;
		});

		// a cos => <cos(a)>
		builtin("cos",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.cos(a.doubleValue()));
			IP++;
		});

		// a sin => <sin(a)>
		builtin("sin",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sin(a.doubleValue()));
			IP++;
		});

		// a tan => <tan(a)>
		builtin("tan",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.tan(a.doubleValue()));
			IP++;
		});

		// a log => <log(a)>
		builtin("log",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.log(a.doubleValue()));
			IP++;
		});

		// a floor => <floor(a)>
		builtin("floor",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.floor(a.doubleValue()));
			IP++;
		});

		// formats stack elements as a string
		// Objects... <nubmer of objects> <format string> fmt
		// example: 42 hello 2 %d:%s fmt
		builtin("fmt",    (Engine E) -> {
			String a = (String) Stack.pop();
			Number b = (Number) Stack.pop();
			int i = b.intValue();

			Object[] o = Stack.subList(Stack.size() - i, Stack.size()).toArray();
			Stack.add(String.format(a, o));
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

		// support for lambas
		// all commands between [ and ] are part of a callable function that can
		// be called with call so [ 1 2 + ] call will put 3 on the stack
		// the lambda address will be pu ton the second stack and popped when call is called
		macro("[", (Engine E) -> {
			Stuff.push(Dict.size());
			Dict.push(null);
		});

		macro("]", (Engine E) -> {
			leave();
			int i = Dict.size();
			int j = (Integer) Stuff.pop();
			Dict.set(j, (Consumer<Engine>) (Engine e) -> {
				IP = i;
			});
			Dict.push((Consumer<Engine>) (Engine) -> {
				Second.push(j + 1);
				IP++;
			});
		});

		// calls a lambda
		builtin("call", (Engine E) -> {
			Trail[++iTrail] = IP + 1;
			IP = (Integer) Second.pop();
		});

		// loops while a condition is held
		// [ ... ] [ ... ] while
		// the first lambda is called provides the boolean that when true
		// call the second lambda
		macro("while", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine) -> {
				Trail[++iTrail] = IP + 1;
				IP = (Integer) Second.get(Second.size() - 2);
			});

			Dict.push((Consumer<Engine>) (Engine) -> {
				if ((Boolean) Stack.pop()) {
					Trail[++iTrail] = IP - 1;
					IP = (Integer) Second.get(Second.size() - 1);
				} else {
					Second.pop();
					Second.pop();
					IP++;
				}
			});
		});

		// the if then else control struct
		// [ ... ] [ ...] test
		// consume the boolean on the top of the stack and calls the first it is
		// true; otherwise it calls the second
		builtin("test", (Engine E) -> {
			Object f = Second.pop();
			Object t = Second.pop();

			Trail[++iTrail] = IP + 1;

			if ((Boolean) Stack.pop()) 
				IP = (Integer) t;
			else
				IP = (Integer) f;
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

		// map! 
		builtin("map!",    (Engine E) -> {
			Stack.push(new HashMap<Object, Object>());
			IP++;
		});

		builtin("put",    (Engine) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			Object k = Stack.pop();
			Object v = Stack.pop();
			o.put(k, v);
			IP++;
		});

		builtin("get",    (Engine E) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			Stack.push(o.get(Stack.pop()));
			IP++;
		});

		builtin("del",    (Engine E) -> {
			HashMap<Object, Object> o = (HashMap<Object, Object>) Stack.pop();
			o.remove(Stack.pop());
			IP++;
		});

		class ListEnd {
		}

		// list data structure
		//
		// asks as a terminator for the list constuctor
		macro("(",    (Engine E) -> {
			literal(new ListEnd());
		});

		// list constructor
		// pops values from stack and adds them to list until terminator is encountered
		builtin(")",    (Engine E) -> {
			ArrayDeque deq = new ArrayDeque();
			Object o = Stack.pop();
			while (!(o instanceof ListEnd)) {
				deq.addFirst(o);
				o = Stack.pop();
			}
			Stack.push(deq);
			IP++;
		});

		// push to list
		builtin("push",    (Engine E) -> {
			((ArrayDeque) Stack.pop()).addLast(Stack.pop());
			IP++;
		});

		// pop from list
		builtin("pop",    (Engine E) -> {
			Stack.push(((ArrayDeque) Stack.pop()).removeLast());
			IP++;
		});

		// size of collection
		builtin("size",    (Engine E) -> {
			Stack.push(((Collection) Stack.pop()).size());
			IP++;
		});
	}

	// compile a builtin word
	// fix: add help string
	public void compile(String s, Consumer<Engine> c, int type) {
		Dict.add(s);
		Dict.add(type);   // builtin
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


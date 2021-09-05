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


public class Engine {
	// running stack
	public Stack<Object>       Stack;

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
		Stuff = new Stack();
		Words = new ArrayDeque<String>();
		IP    = 0;

		top   = -1;

		// TOS == top of stack
		//
		//
		// this word is run when input buffer is empty and no more instructions 
		Dict.push((Consumer<Engine>) (Engine e) -> {
			throw new RuntimeException();
		});

		// displays TOS as string
		builtin("puts", (Engine E) -> System.out.println(Stack.pop()));

		// true constant
		constant("true",  true);
		constant("false", false);
		constant("PI", Math.PI);
		constant("e", Math.E);

		// add two numbers
		// a b + => <a + b>
		builtin("+",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() + b.doubleValue());
		});

		// a b  - => <a - b>
		builtin("-",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() - b.doubleValue());
		});

		// a b * => <a * b>
		builtin("*",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() * b.doubleValue());
		});

		// a b / => <a / b>
		builtin("/",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() / b.doubleValue());
		});

		// a b max => <max(a, b)>
		builtin("max",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.max(a.doubleValue(), b.doubleValue()));
		});

		// a b min => <min(a, b)>
		builtin("min",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.min(a.doubleValue(), b.doubleValue()));
		});

		// a b ** => <pow(a, b)>
		builtin("**",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.pow(a.doubleValue(), b.doubleValue()));
		});

		// works only on numbers
		// a b > => <a > b:boolean>
		builtin(">",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() > b.doubleValue());
		});

		// works only on numbers
		// a b < => <a < b:boolean>
		builtin("<",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() < b.doubleValue());
		});

		// a b cmp => <a.compareTo(b)>
		// compares anything that implements Comparable
		builtin("cmp",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b));
		});

		// a b == => <a.equals(b)>
		// compares anything that implements Comparable
		builtin("==",    (Engine E) -> {
			Object b = Stack.pop();
			Object a = Stack.pop();
			Stack.add(a.equals(b));
		});

		// a sqrt => <sqrt(a)>
		builtin("sqrt",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sqrt(a.doubleValue()));
		});

		// a abs => <abs(a)>
		builtin("abs",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.abs(a.doubleValue()));
		});

		// a cos => <cos(a)>
		builtin("cos",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.cos(a.doubleValue()));
		});

		// a sin => <sin(a)>
		builtin("sin",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sin(a.doubleValue()));
		});

		// a tan => <tan(a)>
		builtin("tan",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.tan(a.doubleValue()));
		});

		// a log => <log(a)>
		builtin("log",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.log(a.doubleValue()));
		});

		// a floor => <floor(a)>
		builtin("floor",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.floor(a.doubleValue()));
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
		});

		// displays debug information
		macro("!!", (Engine E) -> {
			System.out.printf("Dict.size() == %d\n", E.Dict.size());
		});

		// sets named variable with value from stack
		// example: 11 ! number
		macro("!", (Engine E) -> {
			int i = find(E.Words.pop());

			if (i == -1)
				throw new RuntimeException("!: var not found");

			Dict.push((Consumer<Engine>) (Engine E0) -> {
				E0.Dict.set(i + 3, E0.Stack.pop());
			});
		});

		// creates variable and initialize it with number from top of stack
		// example: 42 var meaning
		macro("var", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine e) -> {
				IP = IP + 4;
			});

			int n = E.Dict.size();
			Dict.add(E.Words.pop());
			Dict.add(3);   // variable location
			Dict.add(E.top);
			int v = Dict.size();
			Dict.add(99);
			E.top = n;

			Dict.push((Consumer<Engine>) (Engine E0) -> {
				E0.Dict.set(v, E0.Stack.pop());
			});
		});

		// if else then
		// if consumes boolean
		macro("if", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine E0) -> {
				Boolean b = (Boolean) Stack.pop();
				if (b) 
					IP += 1;
			});
			Stuff.add(Dict.size());
			Dict.add(null);
		});

		macro("else", (Engine E) -> {
			int i = (Integer) Stuff.pop();
			Stuff.add(Dict.size()); 
			Dict.add(null);

			int sz = Dict.size();
			Dict.set(i, (Consumer<Engine>) (Engine E1) -> {
				IP = sz - 1;
			});
		});

		macro("then", (Engine E) -> {
			int sz = Dict.size();
			Dict.set((Integer) Stuff.pop(), (Consumer<Engine>) (Engine E1) -> {
				IP = sz - 1;
			});
		});

		// do while loop 
		// while consumes a boolean
		// do <run on each iteration> while <runs when condition true> loop
		macro("do", (Engine E) -> {
			Stuff.add(Dict.size()); 
		});

		macro("while", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine E0) -> {
				if ((Boolean) Stack.pop()) 
					IP += 1;
			});
			Stuff.add(Dict.size()); 
			Dict.add(null); 
		});

		macro("loop", (Engine E) -> {
			int sz = Dict.size();

			Dict.set((Integer) Stuff.pop(), (Consumer<Engine>) (Engine E1) -> {
				IP = sz;
			});

			Integer i = (Integer) Stuff.pop();

			Dict.push((Consumer<Engine>) (Engine E0) -> {
				IP = i - 1;
			});
		});

		// displays all known words
		builtin("words", (Engine E) -> {
			int cur = top;
			while (cur != -1) {
				System.out.println((String) Dict.get(cur));
				cur = (int) Dict.get(cur + 2);
			}
		});

		// duplicates TOS element
		builtin("dup",    (Engine E) -> {
			Stack.push(Stack.peek());
		});

		// duplicates element under TOS
		builtin("over",    (Engine E) -> {
			Stack.push(Stack.get(Stack.size() - 2));
		});

		// drop element TOS
		builtin("drop",    (Engine E) -> {
			Stack.pop();
		});

		// remove element under TOS
		builtin("nip",    (Engine E) -> {
			Stack.remove(Stack.size() - 2);
		});

		// swap top two elements
		builtin("swap",    (Engine E) -> {
			Object a = Stack.pop();
			Object b = Stack.pop();
			Stack.push(a);
			Stack.push(b);
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

	// compiles a constant with name n and value val
	public void constant(String n, Object val) {
		macro(n, (Engine E) -> {
			makeLiteral(val);
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

	// compile a literal word unto the dictionary
	public void makeLiteral(Object o) {
		Consumer<Engine> c = (Engine) -> Stack.push(o);
		Dict.push(c);
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
				if ((Integer) Dict.get(loc + 1) == 1) {
					Dict.push(Dict.get(loc + 3));
					continue;
				}

				// word found: run
				if ((Integer) Dict.get(loc + 1) == 2) {
					((Consumer<Engine>) Dict.get(loc + 3)).accept(this);
					continue;
				}

				// var found: put value on stack
				if ((Integer) Dict.get(loc + 1) == 3) {
					Stack.push(Dict.get(loc + 3));
					continue;
				}
			}
			// if macro goto run loop

			try {
				// try to parse as integer
				Integer i = Integer.parseInt(s);
				makeLiteral(i);
				continue;
			} catch (NumberFormatException err) {}

			try {
				// try to parse as float
				Float f = Float.parseFloat(s);
				makeLiteral(f);
				continue;
			} catch (NumberFormatException err2) {}
			
			makeLiteral(s);
		}

		leave();

		// execute from the place where compiling started and run until exception thrown
		IP = start;
		run();
	}

	// compiles a leave instruction
	public void leave() {
		Dict.push((Consumer<Engine>) (Engine E) -> {
			IP = Trail[iTrail--] - 1;
		});
	}

	// run main loop
	public void run() {
		while (true) {
			((Consumer<Engine>) Dict.get(IP)).accept(this);
			IP++;
		}
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
		for(String s : ((String) Stack.pop()).split("(?<!\\\\)\\s")) {
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


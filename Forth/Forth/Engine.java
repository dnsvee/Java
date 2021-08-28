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

		// add builtins
		// TOS == top of stack
		// words alwyas cast optimistically towards the type it expects; if casting fails will throw an exception that cant be caught
		//
		Dict.push((Consumer<Engine>) (Engine e) -> {
			throw new RuntimeException();
		});

		// displays TOS as string
		builtin("puts", (Engine E) -> System.out.println(Stack.pop()));

		// add two numbers
		// a b + == a + b
		builtin("+",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() + b.doubleValue());
		});

		// a b == a - b
		builtin("-",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() - b.doubleValue());
		});

		// a b * == a * b
		builtin("*",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() * b.doubleValue());
		});

		// a b / == a / b
		builtin("/",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() / b.doubleValue());
		});

		// a b max == max(a, b)
		builtin("max",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.max(a.doubleValue(), b.doubleValue()));
		});

		// a b min == min(a, b)
		builtin("min",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.min(a.doubleValue(), b.doubleValue()));
		});

		// a b ** == a raised to the power of b
		builtin("**",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(Math.pow(a.doubleValue(), b.doubleValue()));
		});

		// a b > == a > b; works only on numbers
		builtin(">",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() > b.doubleValue());
		});

		// a b < == a < b; works only on numbers
		builtin("<",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(a.doubleValue() < b.doubleValue());
		});

		// a b cmp; compares anything that implements Comparable
		builtin("cmp",    (Engine E) -> {
			Comparable b = (Comparable) Stack.pop();
			Comparable a = (Comparable) Stack.pop();
			Stack.add(a.compareTo(b));
		});

		// a b ==; compares anything that implements equals
		builtin("==",    (Engine E) -> {
			Object b = Stack.pop();
			Object a = Stack.pop();
			Stack.add(a.equals(b));
		});

		// a sqrt == sqrt(a)
		builtin("sqrt",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sqrt(a.doubleValue()));
		});

		// a abs == abs(a)
		builtin("abs",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.abs(a.doubleValue()));
		});

		// PI constant
		builtin("PI",    (Engine E) -> {
			Stack.add(Math.PI);
		});

		// e constant
		builtin("e",    (Engine E) -> {
			Stack.add(Math.E);
		});

		// a cos = cos(a)	
		builtin("cos",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.cos(a.doubleValue()));
		});

		// a sin == sin(a)
		builtin("sin",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.sin(a.doubleValue()));
		});

		// a tan == tan(a)
		builtin("tan",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.tan(a.doubleValue()));
		});

		// log of number
		// ex.: 100 log
		builtin("log",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.log(a.doubleValue()));
		});

		// floor of number
		// 3.4 floor
		builtin("floor",    (Engine E) -> {
			Number a = (Number) Stack.pop();
			Stack.add(Math.floor(a.doubleValue()));
		});

		// formats stack elements as a string; first value is format string; second value is count of objects to format
		// 42 hello 2 %d:%s fmt
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
		// ex.: 11 ! number
		macro("!", (Engine E) -> {
			int i = find(E.Words.pop());

			if (i == -1)
				throw new RuntimeException("!: var not found");

			Dict.push((Consumer<Engine>) (Engine E0) -> {
				E0.Dict.set(i + 3, E0.Stack.pop());
			});
		});

		// creates variable and initialize it with number from top of stack
		// 42 var meaning
		macro("var", (Engine E) -> {
			Dict.push((Consumer<Engine>) (Engine e) -> {
				IP = IP + 4;
			});

			int n = E.Dict.size();
			Dict.add(E.Words.pop());
			Dict.add(3);   // var
			Dict.add(E.top);
			int v = Dict.size();
			Dict.add(99);
			E.top = n;

			Dict.push((Consumer<Engine>) (Engine E0) -> {
				E0.Dict.set(v, E0.Stack.pop());
				//E0.Dict.set(v, E0.Stack.pop());
			});
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
				// compile word found
				if ((Integer) Dict.get(loc + 1) == 1) {
					Dict.push(Dict.get(loc + 3));
					continue;
				}

				// macro
				if ((Integer) Dict.get(loc + 1) == 2) {
					((Consumer<Engine>) Dict.get(loc + 3)).accept(this);
					continue;
				}

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
			} catch (NumberFormatException err) {
				try {
					// try to parse as float
					Float f = Float.parseFloat(s);
					makeLiteral(f);
				} catch (NumberFormatException err2) {
					// puts string in dict as literal
					makeLiteral(s);
				}
			}
		}

		// last word compiled will throw an excpetion to break from the run loop
		//Dict.push((Consumer<Engine>) (Engine e) -> {
//			throw new RuntimeException();
//		});
		leave();

		// execute from the place where compiling started and run until exception thrown
		IP = start;
		run();
	}

	public void leave() {
		Dict.push((Consumer<Engine>) (Engine E) -> {
			IP = Trail[iTrail--] - 1;
		});
	}

	// run loop
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
	// example of words:
	// 123 
	// def
	// hello\ world
	// input\sbuffer
	// Hi!\n
	public void preparse() {
		for(String s : ((String) Stack.pop()).split("(?<!\\\\)\\s")) {
			s = s.replace("\\ ", " ");
			s = s.replace("\\s", " ");
			s = s.replace("\\n", " ");
			Words.add(s);
		}
	}

	public String readLine() throws IOException {
		Scanner s = new Scanner(System.in);
		return s.nextLine();
	}
}


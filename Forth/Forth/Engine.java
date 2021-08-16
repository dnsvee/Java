package Forth;

import java.util.*;
import java.nio.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.lang.*;
import java.util.function.*;
import java.util.regex.*;

public class Engine {
	// running stack
	public Stack<Object>       Stack;

	// all compiled words and literals and builtins
	public Stack<Object>       Dict;

	// IP of current instruction running
	public int                 IP;

	// used for returning from called word
	public int[]               Trail  = new int[128];

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
		builtin("say",  (Engine E) -> System.out.println("hello"));
		builtin("puts", (Engine E) -> System.out.println(Stack.pop()));
		builtin("+",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(b.doubleValue() + a.doubleValue());
		});
		builtin("-",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(b.doubleValue() - a.doubleValue());
		});
		builtin("*",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(b.doubleValue() * a.doubleValue());
		});
		builtin("/",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			Stack.add(b.doubleValue() / a.doubleValue());
		});
	}

	// compile a builtin word
	public void builtin(String s, Consumer<Engine> c) {
		Dict.add(s);
		Dict.add(1);   // builtin
		Dict.add(top);
		Dict.add(c);
		top = Dict.size() - 4;
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

		int start = Dict.size();
		while (Words.size() > 0) {
			String s = Words.pop();
			int loc  = find(s);
			if (loc >= 0) {
				Dict.push(Dict.get(loc + 3));
				continue;
			}

			try {
				Integer i = Integer.parseInt(s);
				makeLiteral(i);
			} catch (NumberFormatException err) {
				try {
					Float f = Float.parseFloat(s);
					makeLiteral(f);
				} catch (NumberFormatException err2) {
					makeLiteral(s);
				}
			}
		}

		Consumer<Engine> c = (Engine e) -> {
			throw new RuntimeException();
		};
		Dict.push(c);

		IP = start;
		while (true) {
			((Consumer<Engine>) Dict.get(IP)).accept(this);
			IP++;
		}
	}

	// parse a word and put it on the stack
	public void parse() {
		if (Words.size() == 0) 
			throw new RuntimeException();

		Stack.push(Words.removeFirst());
	}

	// split script to load into seperate words
	public void preparse() {
		Matcher m = Pattern.compile("(\\S+)").matcher((String) Stack.pop());

		while (m.find()) 
			Words.add(m.group(1));
		// fix strign thing
	}
}

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
	public Stack<Object>       Stack;
	public Stack<Object>       Dict;
	public int                 IP;
	public int[]               Trail  = new int[128];
	public Stack<Object>       Stuff;
	public ArrayDeque<String>  Words;
	public int                 top;

	Consumer<Object> literal(Object o, Object o2) {
		return (Object o3) -> o.toString();
	}

	public Engine() {
		Dict  = new Stack();
		Stack = new Stack();
		Stuff = new Stack();
		Words = new ArrayDeque<String>();
		IP    = 0;


		top   = -1;

		Consumer<Engine> c = (Engine) -> {
		};

		builtin("say",  (Engine E) -> System.out.println("hello"));
		builtin("puts", (Engine E) -> System.out.println(Stack.pop()));
		builtin("+",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			if (b instanceof Float) {
				Stack.push(a.floatValue() + b.floatValue());
				return;
			}

			if (Stack.peek() instanceof Integer) {
				Stack.push(a.intValue() + b.intValue());
				return;
			}
		});
		builtin("-",    (Engine E) -> {
		});
		builtin("*",    (Engine E) -> {
			Number b = (Number) Stack.pop();
			Number a = (Number) Stack.pop();
			if (b instanceof Float) {
				Stack.push(a.floatValue() * b.floatValue());
				return;
			}

			if (Stack.peek() instanceof Integer) {
				Stack.push(a.intValue() * b.intValue());
				return;
			}
		});
		builtin("/",    (Engine E) -> {
		});
	}

	public void builtin(String s, Consumer<Engine> c) {
		Dict.add(s);
		Dict.add(1);   // builtin
		Dict.add(top);
		Dict.add(c);
		top = Dict.size() - 4;
	}

	public int find(String s) {
		int cur = top;
		while (cur != -1) {
			if (((String) Dict.get(cur)).equals(s)) 
				return cur;

			cur = (int) Dict.get(cur + 2);
		}
		return -1;
	}

	public void makeLiteral(Object o) {
		Consumer<Engine> c = (Engine) -> {
			Stack.push(o);
		};
		Dict.push(c);
	}

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

		for(int i = start; i < Dict.size(); i++) {
			((Consumer<Engine>) Dict.get(i)).accept(this);
		}
	}

	// return empty string when done
	public void parse() {
		if (Words.size() == 0) 
			throw new RuntimeException();

		Stack.push(Words.removeFirst());
	}

	public void preparse() {
		Matcher m = Pattern.compile("(\\S+)").matcher((String) Stack.pop());

		while (m.find()) 
			Words.add(m.group(1));
		// fix strign thing
	}
}

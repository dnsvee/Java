import Forth.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.lang.*;

// this provides a readline for the Forth engine
public class Tester {
	public static void main(String arg[]) {
		Engine E = new Engine();

		PrintStream p = System.out;

		if (arg.length == 0) 
			return;

		File f = new File(arg[0]);

		String toread;

		// try to read
		LinkedList<String> strs;
		try {
			strs = new LinkedList<String>(Files.readAllLines(f.toPath(), StandardCharsets.UTF_8));
		} catch (IOException exp) {
			throw new RuntimeException("Problem loading file: " + arg[0]);
		}

		// read each line from the file named by arg[0]
		// eval the line and check the stack for results
		// if all element on the stack are booleans it's considered a passing test
		// if not it's a failing test and output the line to stdout
		//
		while (true) {
			String s = strs.pollFirst();

			// done
			if (s == null) 
				return;

			// eval each line
			E.Stack.add(s);
			try {
				E.eval();
			} catch (RuntimeException exp) {}

			while (E.Stack.size() != 0) {
				// check if each value on the stack is 'true'
				if (E.Stack.peek() instanceof Boolean) {
					boolean b = (boolean) E.Stack.pop();
					if (!b)
						System.out.printf("%s\n", s);
					continue;
				} 

				// value is not a boolean
				E.Stack.pop();
				System.out.printf("!booleans: %s\n", s);
			}
		}
	} // main
}

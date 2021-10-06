import Forth.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.lang.*;

// this provides a readline for the Forth engine
public class ForthTest {
	public static void main(String arg[]) {
		Engine E = new Engine();

		PrintStream p = System.out;

		if (arg.length > 0) {
			File f;

			if (arg.length == 2) 
				f = new File(arg[1]);
			else
				f = new File(arg[0]);

			String toread;

			// try to read
			LinkedList<String> strs;
			try {
				strs = new LinkedList<String>(Files.readAllLines(f.toPath(), StandardCharsets.UTF_8));
			} catch (IOException exp) {
				throw new RuntimeException("Exception!");
			}

			try {
				// if in test mode
				if (arg.length == 2 && arg[0].equals("-t")) {
					while (true) {
						String s = strs.pollFirst();
						if (s == null) 
							return;

						E.Stack.add(s);
						try {
							E.eval();
						} catch (RuntimeException exp) {
							while (E.Stack.size() != 0) {
								if (E.Stack.peek() instanceof Boolean) {
									boolean b = (boolean) E.Stack.pop();
									if (!b)
										System.out.printf("%s\n", s);
								} else {
									E.Stack.pop();
									System.out.printf("non-booleans found %s\n", s);
								}
							}
						}
					}
				} else {
					StringBuilder sb = new StringBuilder();
					while (strs.size() > 0) {
						sb.append(strs.remove(0));
						sb.append("\n");
					}

					E.Stack.add(sb.toString());
					E.eval();
				}
			} catch (RuntimeException exp) {
				System.out.printf("An exception has occurred: %s\n", exp.toString());
			} finally {
				return;
			}
		}

		try {
			int sz = 0;
			while (true) {
				// loop while input
				System.out.printf("%3d/%+3d: ", E.Stack.size(), E.Stack.size() - sz);
				String s = E.readLine();

				// no input so done reading
				if (s.length() == 0) 
					break;

				sz = E.Stack.size();

				try {
					/// put read string on stack and call eval on the Forth engine
					E.Stack.add(s);
					E.eval();
				} catch (RuntimeException exp) {
					// displays a status line
					// outpu size of stack and change since last evaluation
				}
			}
		} catch (IOException exp) {
			System.out.println("Exception");
		} // try
	} // main
}

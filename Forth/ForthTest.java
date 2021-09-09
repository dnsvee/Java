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

		if (arg.length == 1) {
			File f = new File(arg[0]);

			String toread;

		// try to read
			List<String> strs;
			try {
				strs = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
			} catch (IOException exp) {
				throw new RuntimeException("Exception!");
			}

			StringBuilder sb = new StringBuilder();

			while (strs.size() > 0) {
				sb.append(strs.remove(0));
				sb.append("\n");
			}

			E.Stack.add(sb.toString());
			E.eval();

			return;
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
					System.out.printf("Exception: %s\n", exp.toString());

					// displays a status line
					// outpu size of stack and change since last evaluation
				}
			}
		} catch (IOException exp) {
			System.out.println("Exception");
		}


	}

}

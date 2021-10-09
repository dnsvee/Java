import Forth.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.lang.*;

// this provides a readline for the Forth engine
public class Interp {
	public static void main(String arg[]) {
		Engine E = new Engine();

		PrintStream p = System.out;

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
					/// put input on stack and eval through the Forth engine
					E.Stack.add(s);
					E.eval();
				} catch (RuntimeException exp) {
					// displays a status line
				}
			}
		} catch (Exception exp) {
			System.out.println("Exception");
		} // try
	} // main
}

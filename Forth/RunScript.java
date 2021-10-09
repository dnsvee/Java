import Forth.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.lang.*;

// this provides a readline for the Forth engine
public class RunScript {
	public static void main(String arg[]) {
		Engine E = new Engine();

		PrintStream p = System.out;

		if (arg.length != 1) 
			return;

		File f = new File(arg[0]);

		String toread;

		// try to read
		LinkedList<String> strs;
		try {
			strs = new LinkedList<String>(Files.readAllLines(f.toPath(), StandardCharsets.UTF_8));
		} catch (IOException exp) {
			throw new RuntimeException("Exception!");
		}

		StringBuilder sb = new StringBuilder();
		while (strs.size() > 0) {
			sb.append(strs.remove(0));
			sb.append("\n");
		}

		try {
			E.Stack.add(sb.toString());
			E.eval();
		} catch (RuntimeException exp) {
		}
	} // main
}

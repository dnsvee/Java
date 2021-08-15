import Forth.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.lang.*;

public class ForthTest {
	public static void main(String arg[]) {
		Engine E = new Engine();

		PrintStream p = System.out;

		File f = new File("init.fth");

		String toread;
		try {
			List<String> strs;
			strs = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
			StringBuilder sb = new StringBuilder();
			while (strs.size() > 0) {
				sb.append(strs.remove(0));
				sb.append("\n");
			}
			toread = sb.toString();
		} catch (IOException e) {
			p.printf("IOException caught.\n");
			toread = "";
		}

		//E.Stack.add("say 123 def puts puts");
		E.Stack.add(toread);
		E.eval();


	}

}

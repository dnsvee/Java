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

		try {
			while (true) {
				System.out.printf(">>> ");
				String s = E.readLine();
				if (s.length() == 0) 
					break;

				int sz = E.Stack.size();
				try {
					E.Stack.add(s);
					E.eval();
				} catch (RuntimeException exp) {
					System.out.printf("[%2d|%+2d]", E.Stack.size(), E.Stack.size() - sz);
					for(int i = 0; i < Math.min(E.Stack.size(), 3); i++) {
						System.out.printf("(");
						System.out.printf("%12s", E.Stack.get(E.Stack.size() - 1 - i));
						System.out.printf(")");
						if (i == Math.min(E.Stack.size(), 3) - 1) 
							System.out.printf("\n");
					}
				}
			}
		} catch (IOException exp) {
			System.out.println("Erro");
		}


	}

}

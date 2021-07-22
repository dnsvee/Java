package RosettaCode;
import java.util.Scanner;
import java.io.*;
import java.util.Set;
import java.util.HashSet;


public class CountWords {
	public CountWords() {
		StringBuilder sb = new StringBuilder();

		String s = "";
		try (Scanner sc = new Scanner(new File("miser.txt"))) {
			sc.useDelimiter("\\Z");
			s = sc.next();
		} catch (FileNotFoundException fnf) {
			System.out.println("file not found");

		}

		String[] ss = s.split("\\s+");

		System.out.println(ss.length);

		Set<String> uq = new HashSet<>();

		for(int i = 0; i < ss.length; i++) {
			uq.add(ss[i].toLowerCase());
		}

		System.out.printf("Unique words in 'Les Miserables is %d\n", uq.size());


	}
};

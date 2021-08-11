package RosettaCode;
import java.util.Scanner;
import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// impl. RosettaCode task of Counting Words in the ebook 'Les Miserables'
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

		Matcher m = Pattern.compile("(\\w+)").matcher(s);

		Set<String> uq = new HashSet<>();
		while (m.find()) {
			uq.add(m.group(1).toLowerCase());
		}

		System.out.printf("Unique words in 'Les Miserables is %d\n", uq.size());
	}
};

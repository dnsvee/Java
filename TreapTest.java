import Treap.Treap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

// Tester class for Treap
public class TreapTest {
	// time things
	static class Clock {
		long t0;

		public Clock() {
			t0 = 0;
		}

		public void start() {
			t0 = System.currentTimeMillis();
		}

		public long end() {
			long l = System.currentTimeMillis() - t0;
			return System.currentTimeMillis() - t0;
		}
	}

	public static void assertTrue(boolean b, String msg) {
		if (!b) throw new RuntimeException(msg);
	}

	public static void assertFalse(boolean b, String msg) {
		if (b) throw new RuntimeException(msg);
	}

	public static void test1() {
		Treap<Integer> t = new Treap<>();
		System.out.println("Treap.addAll(1, 3, 2, 5, 4)");
		System.out.printf("Treap.toString() == %s\n", t.toString());
		t.addAll(Arrays.asList(1, 3, 2, 5, 4));

		Integer[] arr = {1, 2, 3, 4, 5};
		assertTrue(Arrays.equals(arr, t.toArray()), "Treap.toArray() != [1, 2, 3, 4, 5]");

		assertTrue(t.contains(1, 2, 3, 4, 5), "assertTrue: t.contains(1, 2, 3, 4, 5) != true");

		t.remove(1);
		assertFalse(t.contains(1), "assertFalse: t.contains(1) != false");

		t.remove(3);
		assertFalse(t.contains(3), "assertFalse: t.contains(3) != false");

		t.remove(5);
		assertFalse(t.contains(5), "assertFalse: t.contains(5) != false");

		t.remove(2);
		assertFalse(t.contains(2), "assertFalse: t.contains(2) != false");

		t.remove(4);
		assertFalse(t.contains(4), "assertFalse: t.contains(4) != false");
	}

	// sorting test
	public static void test2() {
		ArrayList<Integer> nums = new ArrayList<>();
		Treap<Integer>     ti   = new Treap<>();
		TreeSet<Integer>   tsi  = new TreeSet<Integer>();

		for(int i = 0; i < 1024 * 1024 * 2; i++)
			nums.add(i);

		Collections.shuffle(nums);

		Clock c = new Clock();

		c.start();
		tsi.addAll(nums);

		System.out.printf("Insert 2Mb numbers in TreeSet in %dms\n", c.end());
		tsi.clear();

		c.start();
		ti.addAll(nums);

		System.out.printf("Insert 2Mb numbers in Treap in %dms\n", c.end());
		if (!ti.isValid()) throw new RuntimeException("Invalid Treap after adding numbers");
		ti.clear();

		c.start();
		Collections.sort(nums);
		System.out.printf("Sorting 2Mb numbers using Collections.sort in %dms\n", c.end());
	}

	public static void test3() {
		Treap<String>     t  = new Treap<>();
		TreeSet<String>   ts = new TreeSet<>();
		ArrayList<String> ws = new ArrayList<>();

		try{
			Scanner sc = new Scanner(new File("words"));
			while (sc.hasNextLine()) 
				ws.add(sc.nextLine());

			Collections.shuffle(ws);

			List<String> sub = ws.subList(0, 1000);

			Clock c = new Clock();

			c.start();
			ts.addAll(ws);

			for (String w : sub) 
				if (ts.contains(w)) 
					ts.remove(w);
			
			System.out.printf("TreeSet: add wordlist and remove 1000 random words from TreeSet in %dms\n", c.end());

			c.start();
			t.addAll(ws);

			for (String w : sub) 
				if (t.contains(w)) 
					t.remove(w);

			if (!t.isValid()) throw new RuntimeException("Invalid Treap after inserting wordlist");
			
			System.out.printf("Treap: add wordlist and remove 1000 random words from TreeSet in %dms\n", c.end());

		} catch(FileNotFoundException ex) {
			System.out.println("words.txt not found");
		}
	}

	static public void main(String[] args) {
		test1();
		test2();
		test3();
	}
}

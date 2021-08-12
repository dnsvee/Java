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
		System.out.println("Doing some tests on the Treap.java class");

		t.addAll(Arrays.asList(1, 3, 2, 5, 4));

		System.out.printf("%s\n", t.toString());
		// toString test
		assertTrue(t.toString().equals("(1, 2, 3, 4, 5)"), "toString() != (1, 2, 3, 4, 5)");

		// toArray tests
		Integer[] arr  = {1, 2, 3, 4, 5};
		Integer[] arr0 = { };

		assertTrue( Arrays.equals(arr,  t.toArray()), "Treap.toArray() != [1, 2, 3, 4, 5]");
		assertTrue( t.size() == 5, "Treap.size() != 5");
		assertTrue(!Arrays.equals(arr0, t.toArray()), "Treap.toArray() != [1, 2, 3, 4, 5]");

		assertTrue(t.contains(1, 2, 3, 4, 5), "assertTrue: t.contains(1, 2, 3, 4, 5) != true");

		// remove all elements 
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

		assertTrue( t.size() == 0, "Treap.size() != 5");
		assertTrue(t.isValid(), "!Treap.isValid()");

		t.addAll(1, 3, 2, 5, 4);
		assertTrue(t.toString().equals("(1, 2, 3, 4, 5)"), "toString() != (1, 2, 3, 4, 5)");

		Treap<Integer> t2 = new Treap<>();
		for(Integer i : t) 
			t2.add(i);
		t2.add(6);

		// test equals
		assertFalse(t.equals(t2), "assertFalse: t == t2");
		t2.remove(6);
		assertTrue(t.equals(t2),  "assertTrue:  t != t2");
		t2.remove(1);
		assertFalse(t.equals(t2), "assertFalse: t == t2");
		assertFalse(t.equals(new TreeSet<Integer>()), "assertFalse: t == TreeSet()");
		assertTrue(t.equals(t),   "assertTrue:  t != t");
		assertTrue((new Treap<Integer>()).equals(new Treap<Integer>()),   "assertTrue: empty treap == empty treap");

	}

	// sorting test 
	// add a bunch of random numbers to a Treap and a TreeSet which sorts them
	// compare with both with Collections.sort
	public static void test2() {
		ArrayList<Integer> nums = new ArrayList<>();
		Treap<Integer>     ti   = new Treap<>();
		TreeSet<Integer>   tsi  = new TreeSet<Integer>();

		for(int i = 0; i < 1024 * 1024; i++)
			nums.add(i);

		Collections.shuffle(nums);

		Clock c = new Clock();

		c.start();
		tsi.addAll(nums);

		System.out.printf("Insert 1Mb numbers in TreeSet in %dms\n", c.end());
		tsi.clear();

		c.start();
		ti.addAll(nums);

		System.out.printf("Insert 1Mb numbers in Treap in %dms\n", c.end());
		assertTrue(ti.isValid(), "!Treap.isValid()");
		ti.clear();

		c.start();
		Collections.sort(nums);
		System.out.printf("Sorting 1Mb numbers using Collections.sort in %dms\n", c.end());
	}

	// add and remove a bunch of words in a Treap and TreeSet and compare
	public static void test3() {
		Treap<String>     t  = new Treap<>();
		TreeSet<String>   ts = new TreeSet<>();
		ArrayList<String> ws = new ArrayList<>();

		try{
			Scanner sc = new Scanner(new File("words"));
			while (sc.hasNextLine()) 
				ws.add(sc.nextLine());

			Collections.shuffle(ws);

			// random select a 1000 words to remove
			List<String> sub = ws.subList(0, 1000);

			Clock c = new Clock();

			// add nd remove from TreeSet
			c.start();
			ts.addAll(ws);

			for (String w : sub) 
				ts.remove(w);

			System.out.printf("TreeSet: add wordlist and remove 1000 random words from TreeSet in %dms\n", c.end());

			// same with Treap
			c.start();
			t.addAll(ws);

			for (String w : sub) 
				t.remove(w);

			assertTrue(t.isValid(), "Invalid Treap after inserting wordlist");

			System.out.printf("Treap: add wordlist and remove 1000 random words from Treap  in %dms\n", c.end());

		} catch(FileNotFoundException ex) {
			System.out.println("words.txt not found");
		}
	}

	static public void main(String[] args) {
		// execute a bunch of tests
		test1();
		test2();
		test3();
	}
}

import Treap.Treap;

public class TreapTest {
	public static class Test<K extends Comparable<K>> {
		Treap<K> t;

		public Test(Treap<K> treap) {
			t = treap;
		}

		public void exist(K... args) {
			for (K k : args) {
				if (t.contains(k)) 
					throw new RuntimeException("exist failed: Element " + k.toString() + " not found");
			}
		}

		public void notexist(K... args) {
			for (K k : args) {
				if (!t.contains(k)) 
					throw new RuntimeException("not exist failed: Element " + k.toString() + " found");
			}
		}
	}

	static public void main(String[] args) {
		Treap<Integer> t = new Treap<>();
		Test<Integer> tt = new Test<>(t);
		t.add(1);
		t.add(3);
		t.add(2);
		t.add(5);
		t.add(4);
		tt.exist(1, 3, 2, 4, 5);
		tt.notexist(0, 6);

		t.forEach((Integer i) -> System.out.printf("%d\n", i));
		System.out.printf("%s\n", t.toString());

		t.remove(3);
		tt.exist(1, 2, 4, 5);
		tt.notexist(3);

		t.remove(1);
		tt.exist(2, 4, 5);
		tt.notexist(1, 3);

		t.remove(5);
		tt.exist(2, 4);
		tt.notexist(1, 3, 5);

		t.remove(2);
		tt.exist(4);
		tt.notexist(1, 2, 3, 5);

		t.remove(4);
		tt.notexist(1, 2, 3, 4, 5);

		if (!t.testIfTreapValid()) 
			System.out.println("Not a valid Treap");
		else
			System.out.println("Valid Treap");
	}
}

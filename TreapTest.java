import Treap.Treap;

public class TreapTest {
	static public void main(String[] args) {
		Treap<Integer> t = new Treap<>();
		t.insert(1);
		t.insert(3);
		t.insert(2);
		t.insert(5);
		t.insert(4);
		t.findt(1);
		t.findt(3);
		t.findt(2);
		t.findt(5);
		t.findt(4);

		t.findf(0);
		t.findf(6);

		System.out.printf("%s\n", t.toString());

		t.remove(1);
		System.out.printf("%s\n", t.toString());

		t.remove(3);
		System.out.printf("%s\n", t.toString());

		t.remove(5);
		System.out.printf("%s\n", t.toString());

		t.remove(2);
		System.out.printf("%s\n", t.toString());

		t.remove(4);
		System.out.printf("%s\n", t.toString());


		if (!t.testIfTreapValid()) 
			System.out.println("Not a valid Treap");
		else
			System.out.println("Valid Treap");

		System.out.printf("%s\n", t.toString());

	}
}

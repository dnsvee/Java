import RosettaCode.Starter;
import tuples.Pair;

public class Main {
	public static void main(String[] args) {
		Starter ac = new Starter();

		Pair<String, Integer> p = Pair.with("hello", 42);
		String s = p.first();
		int    i = p.second();

	}
}




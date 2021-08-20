
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Function;


// Implements the solution the the RosettaCode problem of 100 prisoners
//

public class Prisoners100 {
	public ArrayList<Integer> random100() {
		ArrayList<Integer> al = new ArrayList<>();

		for(int i = 0; i < 100; i++) 
			al.add(i);

		Collections.shuffle(al);

		return al;
	}

	public Prisoners100() {
		System.out.printf("*** 100 prisoners problem ***\n");

		PrintStream p = System.out;

		// naive method: pick a 50 numbers at random and see if 
		Supplier<Integer> naive = () -> {
			ArrayList<Integer> tries = random100();

			int t = 0;

			for(int i = 0; i < 100; i++) {
				Collections.shuffle(tries);

				while (tries.size() > 0) {
					if (i == tries.remove(tries.size() - 1)) {
						t++;
						break;
					}
				}
			}

			return t;
		};

		// smart method; 

		Supplier<Integer> smart = () -> {
			int t = 0;

			ArrayList<Integer> drawers = random100();

			for(int i = 0; i < 100; i++) {
				int num = drawers.get(i);

				int j = 0;

				do {
					// number found
					if (num == i) {
						t++;
						break;
					}

					// get the number and follow it
					num = drawers.get(num);

					j++;

				} while (j < 50);
			}

			return t;
		};

		Function<Supplier<Integer>, Integer> test = (Supplier<Integer> s) -> {
			int tot = 0;

			for(int i = 0; i < 1000; i++)
				if (s.get() == 100) 
					tot++;

			return tot;
		};

		p.printf("Naivve method: Percentage of runs where all prisoners found their number = %.2f%%\n", test.apply(naive) / 10.0);
		p.printf("Naivve method: Percentage of runs where all prisoners found their number = %.2f%%\n", test.apply(smart) / 10.0);
	}

	public static void main(String[] args) {
		new Prisoners100();
	}
}


package RosettaCode;

import  java.util.ArrayList;
import  java.util.Collections;
import java.util.Random;
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.function.Consumer;

public class Prisoners100 {
	public ArrayList<Integer> random100() {
		ArrayList<Integer> al = new ArrayList<>();

		for(int i = 0; i < 100; i++) {
			al.add(i);
		}

		Collections.shuffle(al);

		return al;
	}

	public Prisoners100() {
		System.out.printf("*** 100 prisoners problem ***\n");

		PrintStream p = System.out;

		Supplier<Integer> naive = () -> {
			ArrayList<Integer> tries = random100();

			int t = 0;
			for(int i = 0; i < 100; i++) {
				Collections.shuffle(tries);
				for(int j = 0; j < 50; j++) {
					if (i == tries.get(j)) {
						t++;
						break;
					}
				}
			}
			return t;
		};

		Supplier<Integer> smart = () -> {
			int t = 0;

			ArrayList<Integer> drawers = random100();

			for(int i = 0; i < 100; i++) {
				int num = drawers.get(i);
				int j = 0;
				do {
					if (num == i) {
						t++;
						break;
					}
					num = drawers.get(num);
					j++;
				} while (j < 50);
			}
			return t;
		};

		Consumer<Supplier<Integer>> test = (Supplier<Integer> s) -> {
			int tot = 0;
			for(int i = 0; i < 1000; i++) {
				if (s.get() == 100) tot++;
			}
			p.printf("Percentage of runs where all prisoners found their number = %.2f%%\n", tot / 10.0);
		};

		p.println("Naive method");
		test.accept(naive);

		p.println("Smart");
		test.accept(smart);

	}
}


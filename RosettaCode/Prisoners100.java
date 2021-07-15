package RosettaCode;

import  java.util.ArrayList;
import  java.util.Collections;
import java.util.Random;
import java.io.PrintStream;

public class Prisoners100 {

	public ArrayList<Integer> random100() {
		ArrayList<Integer> al = new ArrayList<>();

		for(int i = 0; i < 100; i++) {
			al.add(i);
		}

		Collections.shuffle(al);

		return al;
	}

	public int naiveMethod() {
		ArrayList<Integer> tries = random100();

		int tot = 0;
		for(int i = 0; i < 100; i++) {
			Collections.shuffle(tries);
			for(int j = 0; j < 50; j++) {
				if (i == tries.get(j)) {
					tot++;
					break;
				}
			}
		}
		return tot;
	}

	public int smartMethod() {
		int tot = 0;

		ArrayList<Integer> drawers = random100();

		for(int i = 0; i < 100; i++) {
			int num = drawers.get(i);
			int j = 0;
			do {
				if (num == i) {
					tot++;
					break;
				}
				num = drawers.get(num);
				j++;
			} while (j < 50);
		}
		return tot;
	}

	public Prisoners100() {
		System.out.printf("*** 100 prisoners problem ***\n");

		PrintStream p = System.out;

		int tot;

		tot = 0;
		for(int i = 0; i < 1000; i++) {
			if (naiveMethod() == 100) tot++;
		}

		p.printf("total of prisoners finding their number in the drawers (naive): %.2f%%\n",tot / 10.0);
		
		tot = 0;
		for(int i = 0; i < 1000; i++) {
			if (smartMethod() == 100) tot++;
		}

		p.printf("total of prisoners finding their number in the drawers (smart): %.2f%%\n",tot / 10.0);

	}
}


// implemnets RosettaCode task 100 doors

public class Doors100 {
	public Doors100() {
		boolean[] doors = new boolean[101];
		
		int s = 1;
		while (s <= 99) {
			for(int i = s; i <= 100; i += s) {
				doors[i] = !doors[i];
			}
			s += 1;
		}

		// print 100 doors
		for(int i = 1; i <= 100; i++) {
			System.out.printf("%d : %b\n", i, doors[i]);
		}
	}

	public static void main(String[] args) {
		new Doors100();
	}
}

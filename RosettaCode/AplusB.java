import java.util.Scanner;

public class AplusB {
	public AplusB() {
		System.out.println("Hello");
		//Scanner s = new Scanner(System.in);
		Scanner s = new Scanner("12 34");
		int a = s.nextInt();
		int b = s.nextInt();
		System.out.printf("%d", a + b);
	}

	public static void main(String[] args) {
		AplusB ab = new AplusB();
	}
}

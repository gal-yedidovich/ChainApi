import java.util.Random;

public class Tester {
	public static void main(String[] args) {

		Chain.startUI(() -> {
			if (new Random().nextBoolean()) throw new RuntimeException("Bubu");
			else return "Bubu";
		}).then(str -> {
			System.out.println("\nSecond block: Background");
			System.out.println("String length:" + str.length());
			return str.length();
		}).thenUI(num -> {
			System.out.println("\nThird block: UI");
			return num / 0;
		}).end(n -> {
			System.out.println("\nEnd: Background");
			System.out.println("length / 2: " + n);
		}).execute();
	}
}

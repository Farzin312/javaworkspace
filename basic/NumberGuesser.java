
import java.util.Random;

/* Plan for random number generator
    1) Create a private utility function that finds a random number
    2) use that function to inside of main to apss on the argument from cli
 */
public class NumberGuesser {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a number guess as a command-line argument.");
            return;
        }
        try {
            int userGuess = Integer.parseInt(args[0]);
            int randomNumber = random();

            System.out.println("You guessed: " + userGuess);
            System.out.println("Random number was: " + randomNumber);

            if (userGuess == randomNumber) {
                System.out.println("Correct guess!");
            } else {
                System.out.println("Wrong guess. Try again!");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        }
    }

    private static int random() {
        Random rand = new Random();
        int random_number = rand.nextInt(1000);
        return random_number;
    }
;
}

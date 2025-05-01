
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/* Creating a to do list:
    1) Create an array that adds and remove items
    2) Create a to function that adds to the to list
    3) Create a function to remove from list
    4) Create a function to view the list
 */
public class Todo {

    // List is interface representing a collection of Strings as we declare
    static List<String> list = new ArrayList<>();
    static final String FILE_PATH = "data/todo.txt";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a method and list of what to add or remove");
            return;
        }
        try {
            loadListFromFile();
            String action = args[0];
            switch (action) {
                case "add":
                    for (int i = 1; i < args.length; i++) {
                        add(args[i]);
                    }
                    break;
                case "remove":
                    for (int i = 1; i < args.length; i++) {
                        if (list.contains(args[i])) {
                            remove(args[i]);
                        } else {
                            System.out.println("The item does not exist within the list.");
                        }
                    }
                    break;
                case "get":
                    get();
                    break;
                default:
                    System.out.println("Invalid action");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e);
        }

    }

    private static void get() {
        System.out.println("There are currently " + list.size() + " todos in the list.");
        System.out.println("The list consists of: " + list);
    }

    private static void add(String item) {
        list.add(item);
        saveListToFile();
    }

    private static void remove(String item) {
        list.remove(item);
        saveListToFile();
    }

    private static void loadListFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void saveListToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String item : list) {
                writer.write(item);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to save the list.");
        }
    }
}

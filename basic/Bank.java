import java.io.BufferedReader;          // For reading text from a file
import java.io.BufferedWriter;          // For writing text to a file
import java.io.IOException;             // For handling I/O errors
import java.nio.file.FileAlreadyExistsException; // To catch “file exists” on create
import java.nio.file.Files;             // Utility class for file operations
import java.nio.file.Path;              // Represents file paths
import java.nio.file.Paths;             // To build Path instances
import java.util.HashMap;               // In-memory map implementation
import java.util.Map;                   // Map interface
import java.util.Scanner;               // For reading console input

public class Bank {
    // ────────────────────────────────────────────────────────────────────────────
    // 1) Constants & In-Memory Storage
    // ────────────────────────────────────────────────────────────────────────────

    private static final Path DB_PATH =    // Path to our flat-file DB
        Paths.get("data", "users.txt");

    private final Map<Integer,User> usersByAccount = new HashMap<>();  
    // Keyed by accountNumber → User

    private final Map<String,User> usersByName = new HashMap<>();      
    // Keyed by username → same User

    private int nextAccountNumber = 1001;  
    // Tracks the next unique account number

    // ────────────────────────────────────────────────────────────────────────────
    // 2) main() → Entry Point
    // ────────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        new Bank().run();   // Instantiate & hand off control
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 3) run() → Loads data, shows menu, handles exit
    // ────────────────────────────────────────────────────────────────────────────

    private void run() {
        ensureDbFileExists();  // Make sure data/ directory & file exist
        loadUsers();           // Read the file once into memory

        Scanner input = new Scanner(System.in);
        String choice;

        do {
            // Main menu display
            System.out.println("\n---- Bank Menu ----");
            System.out.println("1) Create an account");
            System.out.println("2) Access your account");
            System.out.println("3) Exit");
            System.out.print("Enter choice: ");

            choice = input.nextLine().trim();  // Read menu choice

            switch (choice) {
                case "1": createAccount(input);   break;
                case "2": accessAccount(input);   break;
                case "3": System.out.println("Goodbye!"); break;
                default:  System.out.println("Invalid selection.");
            }
        } while (!choice.equals("3"));

        input.close();  // Clean up Scanner
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 4) ensureDbFileExists() → Prepares data/users.txt
    // ────────────────────────────────────────────────────────────────────────────

    private void ensureDbFileExists() {
        try {
            Files.createDirectories(DB_PATH.getParent()); // Create “data/” folder
            Files.createFile(DB_PATH);                    // Create “users.txt”
        } catch (FileAlreadyExistsException ignored) {
            // If it already exists, that’s fine
        } catch (IOException e) {
            System.out.println("Error initializing database file: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 5) loadUsers() → Reads every line into User objects & maps
    // ────────────────────────────────────────────────────────────────────────────

    private void loadUsers() {
        try (BufferedReader reader = Files.newBufferedReader(DB_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");    // CSV: acc,name,pass,balance
                if (parts.length == 4) {
                    int accNum    = Integer.parseInt(parts[0]);
                    String name  = parts[1];
                    String pass  = parts[2];
                    double bal   = Double.parseDouble(parts[3]);

                    User u = new User(accNum, name, pass, bal);
                    usersByAccount.put(accNum, u);
                    usersByName.put(name, u);

                    // Keep nextAccountNumber strictly greater
                    if (accNum >= nextAccountNumber) {
                        nextAccountNumber = accNum + 1;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 6) saveUsers() → Overwrites the file with current in-memory state
    // ────────────────────────────────────────────────────────────────────────────

    private void saveUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(DB_PATH)) {
            for (User u : usersByAccount.values()) {
                // Write each User as: account,name,password,balance
                writer.write(u.accountNumber + ","
                           + u.name + ","
                           + u.password + ","
                           + u.balance);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 7) createAccount() → Prompts user, builds new User, persists it
    // ────────────────────────────────────────────────────────────────────────────

    private void createAccount(Scanner input) {
        System.out.print("Enter your name: ");
        String name = input.nextLine().trim();

        // Prevent duplicate usernames or blank names
        while (name.isEmpty() || usersByName.containsKey(name)) {
            if (name.isEmpty()) {
                System.out.print("Name cannot be blank. Enter your name: ");
            } else {
                System.out.print("That name is taken. Enter a different name: ");
            }
            name = input.nextLine().trim();
        }

        System.out.print("Create a password: ");
        String pass = input.nextLine().trim();

        // Assign next unique account number
        int acc = nextAccountNumber++;
        User u = new User(acc, name, pass, 0.0);

        // Store in both maps
        usersByAccount.put(acc, u);
        usersByName.put(name, u);

        saveUsers();  // Persist immediately

        System.out.println("Account created! Your account number: " + acc);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 8) accessAccount() → Login flow and sub-menu for account ops
    // ────────────────────────────────────────────────────────────────────────────

    private void accessAccount(Scanner input) {
        System.out.print("Enter your name: ");
        String name = input.nextLine().trim();
        System.out.print("Enter your password: ");
        String pass = input.nextLine().trim();

        User current = usersByName.get(name);  // O(1) lookup by name
        if (current == null || !current.password.equals(pass)) {
            System.out.println("Login failed. Check your credentials.");
            return;  // Back to main menu
        }

        System.out.println("Login successful! Account #: " + current.accountNumber);

        String opt;
        do {
            // Sub-menu display
            System.out.println("\n1) View Balance   2) Deposit   3) Withdraw   4) Logout");
            System.out.print("Choose an option: ");
            opt = input.nextLine().trim();

            switch (opt) {
                case "1":
                    System.out.println("Balance: $" + current.balance);
                    break;

                case "2":
                    System.out.print("Amount to deposit: ");
                    double dep = readDouble(input);
                    current.balance += dep;  // Update in memory
                    saveUsers();            // Persist change
                    System.out.println("Deposited. New balance: $" + current.balance);
                    break;

                case "3":
                    System.out.print("Amount to withdraw: ");
                    double w = readDouble(input);
                    if (w <= current.balance) {
                        current.balance -= w;
                        saveUsers();        // Persist change
                        System.out.println("Withdrawn. New balance: $" + current.balance);
                    } else {
                        System.out.println("Insufficient funds.");
                    }
                    break;

                case "4":
                    System.out.println("Logging out.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        } while (!opt.equals("4"));
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 9) Helper to safely parse a double from input
    // ────────────────────────────────────────────────────────────────────────────

    private double readDouble(Scanner input) {
        while (true) {
            try {
                String line = input.nextLine().trim();
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 10) User class → holds one account’s data in memory
    // ────────────────────────────────────────────────────────────────────────────

    private static class User {
        int    accountNumber;
        String name;
        String password;
        double balance;

        User(int accountNumber, String name, String password, double balance) {
            this.accountNumber = accountNumber;
            this.name          = name;
            this.password      = password;
            this.balance       = balance;
        }
    }
}

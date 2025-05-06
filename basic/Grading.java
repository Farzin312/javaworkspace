import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Grading {
    // — DATA FILES —
    private static final Path TEACHERS_PATH = Paths.get("data", "teachers.txt");
    private static final Path STUDENTS_PATH = Paths.get("data", "students.txt");

    // — USER INPUT & ID SEED —
    private final Scanner input = new Scanner(System.in);
    private int nextID = 1000;

    // — PRIMARY LOGIN & LOOKUP MAPS —
    private static final Map<String, Teacher> teacherByUsername = new HashMap<>();
    private static final Map<String, Student> studentByUsername = new HashMap<>();
    private static final Map<String, Subject> subjectByName   = new HashMap<>();

    private static final Map<Integer, Teacher> teacherByID    = new HashMap<>();
    private static final Map<Integer, Student> studentByID    = new HashMap<>();
    private static final Map<Integer, Subject> subjectByID    = new HashMap<>();

    // — SECONDARY NAME LOOKUPS —
    private static final Map<String, List<Teacher>> teachersByName = new HashMap<>();
    private static final Map<String, List<Student>> studentsByName = new HashMap<>();

    public static void main(String[] args) {
        new Grading().run();
    }

    private void run() {
        // initialize
        createDataFilesIfNeeded();
        loadTeachers();
        loadStudents();

        // main loop
        boolean exit = false;
        while (!exit) {
            System.out.println("\n---- Main Menu ----");
            System.out.println("1) Register as Teacher");
            System.out.println("2) Register as Student");
            System.out.println("3) Login as Teacher");
            System.out.println("4) Login as Student");
            System.out.println("5) Exit");
            System.out.print("Choice: ");
            String choice = input.nextLine().trim();
            switch (choice) {
                case "1" -> registerTeacher();
                case "2" -> registerStudent();
                case "3" -> loginTeacher();
                case "4" -> loginStudent();
                case "5" -> {
                    exit = true;
                    saveTeachers();
                    saveStudents();
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // — REGISTRATION —

    private void registerTeacher() {
        System.out.print("Choose a username: ");
        String u = input.nextLine().trim();
        if (teacherByUsername.containsKey(u) || studentByUsername.containsKey(u)) {
            System.out.println("Username already taken.");
            return;
        }
        System.out.print("Choose a password: ");
        String p = input.nextLine().trim();
        System.out.print("Enter your full name: ");
        String n = input.nextLine().trim();

        int id = nextID++;
        Teacher t = new Teacher(id, u, p, n);
        teacherByID.put(id, t);
        teacherByUsername.put(u, t);
        teachersByName.computeIfAbsent(n, k -> new ArrayList<>()).add(t);

        System.out.println("Registered teacher '" + n + "' with ID " + id + ".");
    }

    private void registerStudent() {
        System.out.print("Choose a username: ");
        String u = input.nextLine().trim();
        if (studentByUsername.containsKey(u) || teacherByUsername.containsKey(u)) {
            System.out.println("Username already taken.");
            return;
        }
        System.out.print("Choose a password: ");
        String p = input.nextLine().trim();
        System.out.print("Enter your full name: ");
        String n = input.nextLine().trim();

        int id = nextID++;
        Student s = new Student(id, u, p, n);
        studentByID.put(id, s);
        studentByUsername.put(u, s);
        studentsByName.computeIfAbsent(n, k -> new ArrayList<>()).add(s);

        System.out.println("Registered student '" + n + "' with ID " + id + ".");
    }

    // — LOGIN HANDLERS —

    private void loginTeacher() {
        System.out.print("Teacher username: ");
        String u = input.nextLine().trim();
        System.out.print("Password: ");
        String p = input.nextLine().trim();
        Teacher t = teacherByUsername.get(u);
        if (t != null && t.password.equals(p)) {
            System.out.println("Welcome, " + t.name + "!");
            teacherMenu(t);
        } else {
            System.out.println("Login failed.");
        }
    }

    private void loginStudent() {
        System.out.print("Student username: ");
        String u = input.nextLine().trim();
        System.out.print("Password: ");
        String p = input.nextLine().trim();
        Student s = studentByUsername.get(u);
        if (s != null && s.password.equals(p)) {
            System.out.println("Welcome, " + s.name + "!");
            studentMenu(s);
        } else {
            System.out.println("Login failed.");
        }
    }

    // — TEACHER MENU & ACTIONS —

    private void teacherMenu(Teacher t) {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Teacher Menu --");
            System.out.println("1) Create Subject");
            System.out.println("2) Assign Grade");
            System.out.println("3) View Subject Grades");
            System.out.println("4) View My Rating");
            System.out.println("5) Logout");
            System.out.print("Choice: ");
            switch (input.nextLine().trim()) {
                case "1" -> createSubject(t);
                case "2" -> assignGrade(t);
                case "3" -> viewSubjectGrades(t);
                case "4" -> System.out.printf("Your overall rating: %.2f%n", t.overallRating);
                case "5" -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void createSubject(Teacher t) {
        System.out.print("New subject name: ");
        String name = input.nextLine().trim();
        if (subjectByName.containsKey(name)) {
            System.out.println("Subject already exists.");
            return;
        }
        int id = nextID++;
        Subject s = new Subject(id, name, t);
        subjectByID.put(id, s);
        subjectByName.put(name, s);
        t.subjects.add(s);
        System.out.println("Subject '" + name + "' created with ID " + id + ".");
    }

    private void assignGrade(Teacher t) {
        System.out.print("Subject name: ");
        String subn = input.nextLine().trim();
        Subject s = subjectByName.get(subn);
        if (s == null || s.teacher != t) {
            System.out.println("You do not teach that subject.");
            return;
        }
        if (s.students.isEmpty()) {
            System.out.println("No students enrolled.");
            return;
        }
        System.out.println("Enrolled students:");
        s.students.forEach(st -> System.out.println(" - " + st.studentID + ": " + st.name));
        System.out.print("Student ID to grade: ");
        int sid = Integer.parseInt(input.nextLine().trim());
        Student st = studentByID.get(sid);
        if (st == null || !s.students.contains(st)) {
            System.out.println("Invalid student.");
            return;
        }
        System.out.print("Grade (0-100): ");
        int grade = Integer.parseInt(input.nextLine().trim());
        s.studentGrades.put(sid, grade);
        st.grades.put(s.subjectID, grade);
        s.classAverage = s.studentGrades.values().stream()
                                  .mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.println("Assigned grade " + grade + " to " + st.name + ".");
    }

    private void viewSubjectGrades(Teacher t) {
        System.out.print("Subject name: ");
        String subn = input.nextLine().trim();
        Subject s = subjectByName.get(subn);
        if (s == null || s.teacher != t) {
            System.out.println("You do not teach that subject.");
            return;
        }
        System.out.println("Grades for '" + subn + "':");
        if (s.studentGrades.isEmpty()) {
            System.out.println("  No grades assigned yet.");
        } else {
            s.studentGrades.forEach((sid, g) -> {
                Student st = studentByID.get(sid);
                System.out.printf("  %s (%d): %d%n", st.name, sid, g);
            });
            System.out.printf("Class average: %.2f%n", s.classAverage);
        }
    }

    // — STUDENT MENU & ACTIONS —

    private void studentMenu(Student s) {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Student Menu --");
            System.out.println("1) Enroll in Subject");
            System.out.println("2) View My Subjects");
            System.out.println("3) View My Grades");
            System.out.println("4) Rate a Teacher");
            System.out.println("5) View Teacher Info");
            System.out.println("6) Logout");
            System.out.print("Choice: ");
            switch (input.nextLine().trim()) {
                case "1" -> enrollSubject(s);
                case "2" -> viewMySubjects(s);
                case "3" -> viewMyGrades(s);
                case "4" -> rateTeacher(s);
                case "5" -> viewTeacherInfo();
                case "6" -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void enrollSubject(Student s) {
        System.out.print("Subject name to enroll: ");
        String subn = input.nextLine().trim();
        Subject subj = subjectByName.get(subn);
        if (subj == null) {
            System.out.println("No such subject.");
        } else if (subj.students.contains(s)) {
            System.out.println("Already enrolled.");
        } else {
            subj.students.add(s);
            s.enrolledSubjects.add(subj);
            System.out.println("Enrolled in '" + subn + "'.");
        }
    }

    private void viewMySubjects(Student s) {
        if (s.enrolledSubjects.isEmpty()) {
            System.out.println("Not enrolled in any subjects.");
        } else {
            System.out.println("Your subjects:");
            s.enrolledSubjects.forEach(subj ->
                System.out.println(" - " + subj.subjectName + " (ID " + subj.subjectID + ")"));
        }
    }

    private void viewMyGrades(Student s) {
        if (s.grades.isEmpty()) {
            System.out.println("No grades yet.");
        } else {
            System.out.println("Your grades:");
            s.grades.forEach((sid, g) -> {
                Subject subj = subjectByID.get(sid);
                System.out.printf("  %s: %d%n", subj.subjectName, g);
            });
        }
    }

    private void rateTeacher(Student s) {
        System.out.print("Teacher ID to rate: ");
        int tid = Integer.parseInt(input.nextLine().trim());
        Teacher t = teacherByID.get(tid);
        if (t == null) {
            System.out.println("No such teacher.");
            return;
        }
        System.out.print("Rating (1-5): ");
        int r = Integer.parseInt(input.nextLine().trim());
        s.teacherRatings.put(tid, r);
        t.studentRatings.put(s.studentID, r);
        t.overallRating = t.studentRatings.values().stream()
                                .mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.println("You rated " + t.name + " " + r + "/5.");
    }

    private void viewTeacherInfo() {
        System.out.print("Teacher ID to view: ");
        int tid = Integer.parseInt(input.nextLine().trim());
        Teacher t = teacherByID.get(tid);
        if (t == null) {
            System.out.println("No such teacher.");
            return;
        }
        System.out.println("Name: " + t.name);
        System.out.println("Username: " + t.username);
        System.out.printf("Overall rating: %.2f%n", t.overallRating);
        System.out.println("Subjects taught:");
        t.subjects.forEach(subj -> System.out.println(" - " + subj.subjectName));
    }

    // — FILE I/O —

    private void createDataFilesIfNeeded() {
        try {
            Files.createDirectories(TEACHERS_PATH.getParent());
            if (!Files.exists(TEACHERS_PATH)) Files.createFile(TEACHERS_PATH);
            if (!Files.exists(STUDENTS_PATH)) Files.createFile(STUDENTS_PATH);
        } catch (IOException e) {
            System.out.println("Error initializing data files: " + e.getMessage());
        }
    }

    private void loadTeachers() {
        try (BufferedReader reader = Files.newBufferedReader(TEACHERS_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) continue;
                int id = Integer.parseInt(p[0]);
                Teacher t = new Teacher(id, p[1], p[2], p[3]);
                teacherByID.put(id, t);
                teacherByUsername.put(p[1], t);
                teachersByName.computeIfAbsent(t.name, k -> new ArrayList<>()).add(t);
                nextID = Math.max(nextID, id + 1);
            }
        } catch (IOException e) {
            System.out.println("Error loading teachers: " + e.getMessage());
        }
    }

    private void loadStudents() {
        try (BufferedReader reader = Files.newBufferedReader(STUDENTS_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) continue;
                int id = Integer.parseInt(p[0]);
                Student s = new Student(id, p[1], p[2], p[3]);
                studentByID.put(id, s);
                studentByUsername.put(p[1], s);
                studentsByName.computeIfAbsent(s.name, k -> new ArrayList<>()).add(s);
                nextID = Math.max(nextID, id + 1);
            }
        } catch (IOException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }

    private void saveTeachers() {
        try (BufferedWriter writer = Files.newBufferedWriter(TEACHERS_PATH)) {
            for (Teacher t : teacherByUsername.values()) {
                writer.write(t.teacherID + "," + t.username + "," + t.password + "," + t.name);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving teachers: " + e.getMessage());
        }
    }

    private void saveStudents() {
        try (BufferedWriter writer = Files.newBufferedWriter(STUDENTS_PATH)) {
            for (Student s : studentByUsername.values()) {
                writer.write(s.studentID + "," + s.username + "," + s.password + "," + s.name);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving students: " + e.getMessage());
        }
    }

    // — DATA CLASSES —

    private static class Teacher {
        int teacherID;
        String username, password, name;
        List<Subject> subjects = new ArrayList<>();
        Map<Integer,Integer> studentRatings = new HashMap<>();
        double overallRating;

        Teacher(int teacherID, String username, String password, String name) {
            this.teacherID = teacherID;
            this.username  = username;
            this.password  = password;
            this.name      = name;
            this.overallRating = 0.0;
        }
    }

    private static class Subject {
        int subjectID;
        String subjectName;
        Teacher teacher;
        List<Student> students = new ArrayList<>();
        Map<Integer,Integer> studentGrades = new HashMap<>();
        double classAverage, classRating;

        Subject(int subjectID, String subjectName, Teacher teacher) {
            this.subjectID   = subjectID;
            this.subjectName = subjectName;
            this.teacher     = teacher;
            this.classAverage = 0.0;
            this.classRating  = 0.0;
        }
    }

    private static class Student {
        int studentID;
        String username, password, name;
        List<Subject> enrolledSubjects = new ArrayList<>();
        Map<Integer,Integer> teacherRatings = new HashMap<>();
        Map<Integer,Integer> grades = new HashMap<>();

        Student(int studentID, String username, String password, String name) {
            this.studentID = studentID;
            this.username  = username;
            this.password  = password;
            this.name      = name;
        }
    }
}

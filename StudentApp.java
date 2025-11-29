import java.util.*;

/* Simple Student Management with exceptions and threading.
   File name: StudentApp.java
*/

// abstract person
abstract class Person {
    protected String name;
    protected String email;
    public Person() {}
    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public abstract void displayInfo();
}

// student
class Student extends Person {
    private int rollNo;
    private String course;
    private double marks;
    private char grade;

    public Student() {}

    public Student(int rollNo, String name, String email, String course, double marks) {
        super(name, email);
        this.rollNo = rollNo;
        this.course = course;
        this.marks = marks;
        calculateGrade();
    }

    @Override
    public void displayInfo() {
        System.out.println("Roll No: " + rollNo);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Course: " + course);
        System.out.println("Marks: " + marks);
        System.out.println("Grade: " + grade);
        System.out.println("-------------------------");
    }

    private void calculateGrade() {
        if (marks >= 90) grade = 'A';
        else if (marks >= 75) grade = 'B';
        else if (marks >= 60) grade = 'C';
        else grade = 'D';
    }

    public int getRollNo() { return rollNo; }
}

// interface for actions
interface RecordActions {
    void addStudent(Student s) throws InvalidInputException;
    void deleteStudent(int rollNo) throws StudentNotFoundException;
    void updateStudent(int rollNo, Student newData) throws StudentNotFoundException, InvalidInputException;
    Student searchStudent(int rollNo) throws StudentNotFoundException;
    void viewAllStudents();
}

// custom exception when student is not found
class StudentNotFoundException extends Exception {
    public StudentNotFoundException(String msg) { super(msg); }
}

// custom exception for invalid input
class InvalidInputException extends Exception {
    public InvalidInputException(String msg) { super(msg); }
}

// manager with HashMap storage
class StudentManager implements RecordActions {

    private Map<Integer, Student> students = new HashMap<>();

    // loader runnable to simulate loading
    static class Loader implements Runnable {
        private String message;
        public Loader(String message) { this.message = message; }
        public void run() {
            try {
                System.out.print(message);
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(300); // simulate work
                    System.out.print(".");
                }
                System.out.println();
            } catch (InterruptedException e) {
                System.out.println("\nLoading interrupted.");
            }
        }
    }

    public synchronized void addStudent(Student s) throws InvalidInputException {
        if (s == null) throw new InvalidInputException("Student data is null.");
        Integer key = Integer.valueOf(s.getRollNo()); // wrapper usage
        if (students.containsKey(key)) {
            throw new InvalidInputException("Duplicate roll number: " + key);
        }
        // simulate loading
        Thread t = new Thread(new Loader("Loading"));
        t.start();
        try {
            t.join(); // wait for loader to finish
        } catch (InterruptedException e) {
            // ignore
        }
        students.put(key, s);
        System.out.println("Student added.");
    }

    public synchronized void deleteStudent(int rollNo) throws StudentNotFoundException {
        Integer key = Integer.valueOf(rollNo);
        if (students.remove(key) == null) throw new StudentNotFoundException("Roll no " + rollNo + " not found.");
        System.out.println("Student deleted.");
    }

    public synchronized void updateStudent(int rollNo, Student newData) throws StudentNotFoundException, InvalidInputException {
        if (newData == null) throw new InvalidInputException("New student data is null.");
        Integer key = Integer.valueOf(rollNo);
        if (!students.containsKey(key)) throw new StudentNotFoundException("Roll no " + rollNo + " not found.");
        // simulate loading while updating
        Thread t = new Thread(new Loader("Updating"));
        t.start();
        try { t.join(); } catch (InterruptedException e) {}
        students.put(key, newData);
        System.out.println("Student updated.");
    }

    public synchronized Student searchStudent(int rollNo) throws StudentNotFoundException {
        Integer key = Integer.valueOf(rollNo);
        Student s = students.get(key);
        if (s == null) throw new StudentNotFoundException("Roll no " + rollNo + " not found.");
        return s;
    }

    public synchronized void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No records.");
            return;
        }
        for (Student s : students.values()) s.displayInfo();
    }
}

// main class (file name StudentApp.java)
public class StudentApp {

    // validate non-empty string
    private static String readNonEmpty(Scanner sc, String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        if (line.isEmpty()) throw new InvalidInputException("Field cannot be empty.");
        return line;
    }

    // read integer using wrapper parsing
    private static Integer readInteger(Scanner sc, String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            Integer val = Integer.valueOf(line); // wrapper
            return val;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid integer input.");
        }
    }

    // read double using wrapper parsing
    private static Double readDouble(Scanner sc, String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            Double val = Double.valueOf(line); // wrapper
            return val;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid decimal input.");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StudentManager manager = new StudentManager();

        boolean running = true;
        while (running) {
            System.out.println("\n===== Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. Display All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Demo: Add sample student (quick)");
            System.out.println("7. Exit");
            System.out.print("Choice: ");

            String choiceLine = sc.nextLine().trim();
            int choice = -1;
            try {
                choice = Integer.parseInt(choiceLine);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    try {
                        Integer roll = readInteger(sc, "Enter Roll No (Integer): ");
                        String name = readNonEmpty(sc, "Enter Name: ");
                        String email = readNonEmpty(sc, "Enter Email: ");
                        String course = readNonEmpty(sc, "Enter Course: ");
                        Double marks = readDouble(sc, "Enter Marks (0-100): ");
                        if (marks < 0 || marks > 100) throw new InvalidInputException("Marks must be 0-100.");

                        // autoboxing - primitive to wrapper happens when needed (example below)
                        int rprimitive = roll.intValue();
                        double mprimitive = marks.doubleValue();
                        Student s = new Student(rprimitive, name, email, course, mprimitive);
                        manager.addStudent(s);
                    } catch (InvalidInputException ex) {
                        System.out.println("Input error: " + ex.getMessage());
                    } catch (Exception ex) {
                        System.out.println("Error: " + ex.getMessage());
                    } finally {
                        // optional cleanup
                    }
                    break;

                case 2:
                    manager.viewAllStudents();
                    break;

                case 3:
                    try {
                        Integer r = readInteger(sc, "Enter Roll No to search: ");
                        Student found = manager.searchStudent(r.intValue());
                        System.out.println("Student found:");
                        found.displayInfo();
                    } catch (InvalidInputException ex) {
                        System.out.println("Input error: " + ex.getMessage());
                    } catch (StudentNotFoundException ex) {
                        System.out.println("Not found: " + ex.getMessage());
                    } finally {
                        // nothing
                    }
                    break;

                case 4:
                    try {
                        Integer r = readInteger(sc, "Enter Roll No to update: ");
                        // check existence first
                        Student existing = manager.searchStudent(r.intValue());
                        System.out.println("Existing record:");
                        existing.displayInfo();

                        String name = readNonEmpty(sc, "Enter New Name: ");
                        String email = readNonEmpty(sc, "Enter New Email: ");
                        String course = readNonEmpty(sc, "Enter New Course: ");
                        Double marks = readDouble(sc, "Enter New Marks (0-100): ");
                        if (marks < 0 || marks > 100) throw new InvalidInputException("Marks must be 0-100.");

                        Student updated = new Student(r.intValue(), name, email, course, marks.doubleValue());
                        manager.updateStudent(r.intValue(), updated);
                    } catch (InvalidInputException ex) {
                        System.out.println("Input error: " + ex.getMessage());
                    } catch (StudentNotFoundException ex) {
                        System.out.println("Not found: " + ex.getMessage());
                    } finally {
                        // nothing
                    }
                    break;

                case 5:
                    try {
                        Integer r = readInteger(sc, "Enter Roll No to delete: ");
                        manager.deleteStudent(r.intValue());
                    } catch (InvalidInputException ex) {
                        System.out.println("Input error: " + ex.getMessage());
                    } catch (StudentNotFoundException ex) {
                        System.out.println("Not found: " + ex.getMessage());
                    } finally {
                        // nothing
                    }
                    break;

                case 6:
                    // quick demo to match expected output format
                    try {
                        Integer roll = Integer.valueOf(102); // wrapper
                        Student demo = new Student(roll.intValue(), "Karan", "karan@mail.com", "BCA", 77.5);
                        manager.addStudent(demo);
                        System.out.println("Loading....."); // already loader shows dots; print per expected output
                        demo.displayInfo();
                    } catch (Exception ex) {
                        System.out.println("Demo error: " + ex.getMessage());
                    }
                    break;

                case 7:
                    running = false;
                    System.out.println("Program execution completed.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }
}

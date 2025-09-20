import java.io.*;
import java.util.*;

class User implements Serializable {
    String username;
    String password;
    double income;

    User(String username, String password) {
        this.username = username;
        this.password = password;
        this.income = 0;
    }
}

class Category implements Serializable {
    String name;

    Category(String name) { this.name = name; }
    public String toString() { return name; }
}

class Expense implements Serializable {
    String category;
    double amount;
    String note;

    Expense(String category, double amount, String note) {
        this.category = category;
        this.amount = amount;
        this.note = note;
    }

    public String toString() { return category + " - " + amount + " (" + note + ")"; }
}

class Bill implements Serializable {
    String name;
    double amount;
    Date dueDate;

    Bill(String name, double amount, Date dueDate) {
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    public String toString() {
        return name + " - " + amount + " (Due: " + dueDate + ")";
    }
}

public class SmartBudgetApp {
    private static final String USER_FILE = "users.dat";
    private static User currentUser;
    private static ArrayList<Category> categories = new ArrayList<>();
    private static ArrayList<Expense> expenses = new ArrayList<>();
    private static ArrayList<Bill> bills = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===== Welcome to Smart Budget & Bill Reminder =====");
        loadDefaultCategories();
        loginMenu();
        askIncomeFirst();
        mainMenu();
    }

    // ---------- LOGIN / SIGNUP ----------
    private static void loginMenu() {
        while (true) {
            System.out.println("\n1. Signup");
            System.out.println("2. Login");
            System.out.print("Choose: ");
            int choice = sc.nextInt(); sc.nextLine();

            if (choice == 1) signup();
            else if (choice == 2) {
                if (login()) break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void signup() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        currentUser = new User(user, pass);
        saveUser(currentUser);
        System.out.println("Signup successful! Please login.");
    }

    private static boolean login() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        User loaded = loadUser(user);
        if (loaded != null && loaded.password.equals(pass)) {
            currentUser = loaded;
            System.out.println("Login successful! Welcome " + currentUser.username);
            return true;
        }
        System.out.println("Invalid credentials.");
        return false;
    }

    private static void saveUser(User user) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(user);
        } catch (IOException e) {
            System.out.println("Error saving user.");
        }
    }

    private static User loadUser(String username) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            return (User) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- INCOME ----------
    private static void askIncomeFirst() {
        if (currentUser.income <= 0) {
            System.out.print("Enter your total income: ");
            currentUser.income = sc.nextDouble(); sc.nextLine();
            saveUser(currentUser);
        }
    }

    // ---------- MAIN MENU ----------
    private static void mainMenu() {
        while (true) {
            System.out.println("\n===== Main Menu =====");
            System.out.println("1. Add Category");
            System.out.println("2. Show Categories");
            System.out.println("3. Add Income");
            System.out.println("4. Show Total Income");
            System.out.println("5. Add Expense");
            System.out.println("6. Show Expenses");
            System.out.println("7. Add Bill");
            System.out.println("8. Show Bills");
            System.out.println("9. Report & Statistics");
            System.out.println("10. Search Expense");
            System.out.println("11. Export Data");
            System.out.println("12. Logout");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt(); sc.nextLine();
            switch (choice) {
                case 1: addCategory(); break;
                case 2: showCategories(); break;
                case 3: addIncome(); break;
                case 4: showIncome(); break;
                case 5: addExpense(); break;
                case 6: showExpenses(); break;
                case 7: addBill(); break;
                case 8: showBills(); break;
                case 9: showReportAndStats(); break;
                case 10: searchExpense(); break;
                case 11: exportData(); break;
                case 12: logout(); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ---------- CATEGORIES ----------
    private static void loadDefaultCategories() {
        categories.add(new Category("Food"));
        categories.add(new Category("Rent"));
        categories.add(new Category("Travel"));
        categories.add(new Category("Shopping"));
        categories.add(new Category("Entertainment"));
        categories.add(new Category("Bills"));
    }

    private static void addCategory() {
        System.out.print("Enter new category name: ");
        String name = sc.nextLine();
        categories.add(new Category(name));
        System.out.println("Category added.");
    }

    private static void showCategories() {
        System.out.println("Categories:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }
    }

    // ---------- INCOME ----------
    private static void addIncome() {
        System.out.print("Enter amount to add to income: ");
        double amt = sc.nextDouble(); sc.nextLine();
        currentUser.income += amt;
        saveUser(currentUser);
        System.out.println("Income updated.");
    }

    private static void showIncome() {
        System.out.println("Total income: " + currentUser.income);
    }

    // ---------- EXPENSE ----------
    private static void addExpense() {
        showCategories();
        System.out.print("Choose category number: ");
        int c = sc.nextInt(); sc.nextLine();
        if (c < 1 || c > categories.size()) {
            System.out.println("Invalid category.");
            return;
        }
        System.out.print("Enter amount: ");
        double amt = sc.nextDouble(); sc.nextLine();
        System.out.print("Enter note: ");
        String note = sc.nextLine();

        expenses.add(new Expense(categories.get(c - 1).name, amt, note));
        checkBudgetAlert();
    }

    private static void showExpenses() {
        System.out.println("Expenses:");
        for (Expense e : expenses) System.out.println(e);
    }

    private static void checkBudgetAlert() {
        double totalExp = expenses.stream().mapToDouble(e -> e.amount).sum();
        if (totalExp > currentUser.income) {
            System.out.println("⚠ ALERT: Expenses exceeded your income!");
        } else if (totalExp > 0.8 * currentUser.income) {
            System.out.println("⚠ WARNING: Expenses crossed 80% of income.");
        }
    }

    // ---------- BILLS ----------
    private static void addBill() {
        System.out.print("Enter bill name: ");
        String name = sc.nextLine();
        System.out.print("Enter bill amount: ");
        double amt = sc.nextDouble(); sc.nextLine();
        System.out.print("Enter due date (dd/mm/yyyy): ");
        String dateStr = sc.nextLine();
        try {
            String[] parts = dateStr.split("/");
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
            bills.add(new Bill(name, amt, cal.getTime()));
        } catch (Exception e) {
            System.out.println("Invalid date format.");
        }
    }

    private static void showBills() {
        System.out.println("Bills:");
        for (Bill b : bills) System.out.println(b);
    }

    // ---------- REPORT & STATISTICS ----------
    private static void showReportAndStats() {
        double totalExp = expenses.stream().mapToDouble(e -> e.amount).sum();
        System.out.println("\n--- REPORT ---");
        System.out.println("Total Income: " + currentUser.income);
        System.out.println("Total Expenses: " + totalExp);
        System.out.println("Remaining Balance: " + (currentUser.income - totalExp));

        System.out.println("\nCategory-wise Expenses:");
        Map<String, Double> catMap = new HashMap<>();
        for (Expense e : expenses) {
            catMap.put(e.category, catMap.getOrDefault(e.category, 0.0) + e.amount);
        }
        for (String c : catMap.keySet()) {
            System.out.println(c + ": " + catMap.get(c));
        }

        if (!expenses.isEmpty()) {
            Expense maxExp = expenses.stream().max(Comparator.comparingDouble(e -> e.amount)).get();
            System.out.println("\nHighest Expense: " + maxExp);
        }

        System.out.println("Number of Expenses: " + expenses.size());
        long pendingBills = bills.stream().filter(b -> b.dueDate.after(new Date())).count();
        System.out.println("Number of Bills Pending: " + pendingBills);
        System.out.printf("Percentage of Income Spent: %.2f%%\n", (totalExp / currentUser.income) * 100);
    }

    // ---------- SEARCH ----------
    private static void searchExpense() {
        System.out.print("Enter keyword to search (category or note): ");
        String key = sc.nextLine().toLowerCase();
        System.out.println("\nSearch Results:");
        for (Expense e : expenses) {
            if (e.category.toLowerCase().contains(key) || e.note.toLowerCase().contains(key)) {
                System.out.println(e);
            }
        }
    }

    // ---------- EXPORT ----------
    private static void exportData() {
        try (PrintWriter pw = new PrintWriter("report.txt")) {
            pw.println("--- Income & Expenses Report ---");
            pw.println("Total Income: " + currentUser.income);
            pw.println("\nExpenses:");
            for (Expense e : expenses) pw.println(e);
            pw.println("\nBills:");
            for (Bill b : bills) pw.println(b);
            System.out.println("Data exported to report.txt");
        } catch (Exception e) {
            System.out.println("Error exporting.");
        }
    }

    // ---------- LOGOUT ----------
    private static void logout() {
        System.out.println("Goodbye " + currentUser.username + "!");
    }
}

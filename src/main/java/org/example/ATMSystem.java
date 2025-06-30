package org.example;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

// ATMSystem Class
public class ATMSystem {
    public static double atmCashBalance;
    private Bank bank;
    public static int InkQuantityUsed;
    public static int PaperQuantityUsed;
    private static final int InkQuantity_LIMIT = 50;
    static final int PaperQuantity_LIMIT = 50;
    public static boolean requiresInkMaintenance;
    public static boolean requiresPaperMaintenance;
    private PostgreSQLJDBC db;
    public Connection conn;
    private Customer customer;
    private Account account;
    private ATMTechnician technician;

    public Account getAccount() {
        return account;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setTechnician(ATMTechnician technician) {
        this.technician = technician;
    }

    public ATMTechnician getTechnician()
    {
        return technician;
    }


    public ATMSystem() {
        atmCashBalance = 1000000.0;
        this.bank = new Bank("MyBank");
        PaperQuantityUsed = 0;
        InkQuantityUsed = 0;
        requiresInkMaintenance = false;
        requiresPaperMaintenance = false;
        db = new PostgreSQLJDBC();
        db.initializeTables();
        conn = db.getConnection();
        initializePaperAndInkAndCashUsed();
    }

    public void initializePaperAndInkAndCashUsed()
    {
        String query = "SELECT ink_amount_used, paper_amount_used, atmCashBalance, last_updated FROM atm_maintenance WHERE id = 1;";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                InkQuantityUsed = rs.getInt("ink_amount_used");
                PaperQuantityUsed = rs.getInt("paper_amount_used");
                atmCashBalance = rs.getDouble("atmCashBalance");
            } else {
                System.out.println("No maintenance record found.");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching maintenance usage: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Initialize ATM system, Bank, Customer, and ATM Technician
        ATMSystem atmSystem = new ATMSystem();
        atmSystem.showWelcomeScreen(scanner);
        scanner.close();
    }

    public double getAtmCashBalance() {
        return atmCashBalance;
    }

    public boolean isrequiresInkMaintenance() {
        return requiresInkMaintenance;
    }

    public boolean isrequiresPaperMaintenance() {
        return requiresPaperMaintenance;
    }

    public void showWelcomeScreen(Scanner scanner) {
        boolean exit = false;
        while (!exit) {
            System.out.println("Welcome to the ATM System of " + bank.getBankName());
            System.out.println("Please Choose an option to continue\n 1) Create New Account \n 2) Access Existing Account \n 3) Exit");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    createAccount(scanner);
                    break;
                case 2:
                    showPinEntryScreen(scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");

            }
        }
    }

    public void showPinEntryScreen(Scanner scanner) {
        scanner.nextLine();
        System.out.println("Please Enter your User name");
        String userNo = scanner.nextLine();
        System.out.print("Enter PIN: ");
        int enteredPin = scanner.nextInt();
        String role = "", name = "";
        int id = 0, userID = 0;
        double balance = 0.0;

        role = getDataFromDB(enteredPin, userNo, role);


        if (role.equals("technician")) {
            System.out.println("Pin validated successfully as ATM Technician!");
            showTechnicianMenu(scanner);
        } else if (role.equals("customer")) {
            System.out.println("Pin validated successfully as Customer!");
            if (requiresInkMaintenance) {
                System.out.println("Maintenance required! Technician must refill ink. Please Try again later!");
//                atmSystem.showTechnicianMenu(atmTechnician, scanner);
            } else if (requiresPaperMaintenance) {
                System.out.println("Maintenance required! Technician must refill paper. Please Try again later!");
//                atmSystem.showTechnicianMenu(atmTechnician, scanner);
            } else {
                showCustomerMenu(scanner);
            }

        }
    }

    private String getDataFromDB(int enteredPin, String userNo, String role) {
        int id;
        int userID;
        String name;
        double balance;
        if (conn != null) {
            String sql = "SELECT * FROM \"user\" WHERE pin = ? AND name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, enteredPin);
                stmt.setString(2, userNo);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    id = rs.getInt("id");
                    name = rs.getString("name");
                    role = rs.getString("role");
                    if (role.equals("customer")) {
                        customer = new Customer(id, name, role, enteredPin);
                        String AccountQuery = "SELECT * FROM account WHERE userid = ?";
                        try (PreparedStatement accountStmt = conn.prepareStatement(AccountQuery)) {
                            accountStmt.setInt(1, id);
                            ResultSet acc = accountStmt.executeQuery();

                            if (acc.next()) {
                                id = acc.getInt("account_id");
                                userID = acc.getInt("userid");
                                balance = acc.getDouble("balance");
                                account = new Account(id, userID, balance);
                            } else {
                                System.out.println("No account found for user ID: " + id);
                            }
                        }
                    } else {
                        technician = new ATMTechnician(id, name, role, enteredPin);
                    }
                } else {
                    System.out.println("No user found with that PIN.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return role;
    }

    public void createAccount(Scanner scanner) {
        System.out.println("Please Choose an option to continue\n 1) Create Account as Customer \n 2) Technician");
        int choice = scanner.nextInt();
        boolean exit = false;
        while (!exit) {
            switch (choice) {
                case 1:
                    createUser(scanner, "customer");
                    exit = true;
                    break;
                case 2:
                    createUser(scanner, "technician");
                    exit = true;
                    break;
                case 3:
                    System.out.println("Exiting...");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");

            }
        }

    }

    private void createUser(Scanner scanner, String role) {
        String name;
        int pin;
        System.out.println("Please Enter name");
        scanner.nextLine();
        name = scanner.nextLine();
        System.out.print("Please Enter 4-digit PIN: ");
        while (true) {
            pin = scanner.nextInt();
            if (pin >= 1000 && pin <= 9999) {
                break;
            } else {
                System.out.println("Invalid PIN. Please enter exactly 4 digits.");
            }
        }
        insertInDB(role, name, pin);


    }

    private void insertInDB(String role, String name, int pin) {
        if (conn != null) {
            String userSql = "INSERT INTO \"user\" (name, role, pin) VALUES (?, ?, ?)";
            String accountSql = "INSERT INTO account (userid, balance) VALUES (?, ?)";

            try (
                    PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement accountStmt = conn.prepareStatement(accountSql)
            ) {
                // Insert into user
                userStmt.setString(1, name);
                userStmt.setString(2, role);
                userStmt.setInt(3, pin);
                userStmt.executeUpdate();

                if (role.equals("customer")) {
                    ResultSet rs = userStmt.getGeneratedKeys();
                    if (rs.next()) {
                        int userId = rs.getInt(1);

                        // Insert into account with default balance
                        accountStmt.setInt(1, userId);
                        accountStmt.setBigDecimal(2, new BigDecimal("0.00"));

                        accountStmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void showCustomerMenu(Scanner scanner) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\nCustomer Menu:");
            System.out.println("1. Check Balance");
            System.out.println("2. Withdraw Cash");
            System.out.println("3. Deposit Cash");
            System.out.println("4. Exit");

            System.out.print("Select an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    showBalanceScreen(customer.getId());
                    break;
                case 2:
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    showWithdrawScreen(withdrawAmount);
                    break;
                case 3:
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    showDepositScreen(depositAmount);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }

            // Check if maintenance is required
            if (InkQuantityUsed >= InkQuantity_LIMIT) {
                requiresInkMaintenance = true;
                System.out.println("Maintenance required! Technician must refill Ink before further transactions.");
                break;
            }
            if (PaperQuantityUsed >= PaperQuantity_LIMIT) {
                requiresPaperMaintenance = true;
                System.out.println("Maintenance required! Technician must refill Paper before further transactions.");
                break;
            }
        }
    }

    public void showTechnicianMenu(Scanner scanner) {
        boolean technicianExit = false;
        while (!technicianExit) {
            System.out.println("\nATM Technician Menu");
            System.out.println("1. Refill Ink");
            System.out.println("2. Refill Paper");
            System.out.println("3. Upgrade System");
            System.out.println("4. Perform Diagnostics");
            System.out.println("5. Replenish Cash");
            System.out.println("6. Exit");

            System.out.print("Select an action: ");
            int technicianChoice = scanner.nextInt();

            switch (technicianChoice) {
                case 1:
                    technician.refillInk(conn);
                    requiresInkMaintenance = false;
                    InkQuantityUsed = 0;
                    break;
                case 2:
                    technician.refillPaper(conn);
                    requiresPaperMaintenance = false;
                    PaperQuantityUsed = 0;
                    break;
                case 3:
                    technician.upgradeSystem();
                    break;
                case 4:
                    technician.performDiagnostics();
                    break;
                case 5:
                    System.out.print("Enter amount to replenish ATM with: ");
                    double replenishAmount = scanner.nextDouble();
                    technician.replenishCash(replenishAmount,conn);
                    break;
                case 6:
                    System.out.println("Exiting maintenance mode.");
                    technicianExit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public void showBalanceScreen(int userId) {
        String sql = "SELECT balance FROM account WHERE userid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);  // replace with the actual user ID
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal("balance");
                System.out.println("Your current balance is: " + balance);
            } else {
                System.out.println("No account found for user ID: " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        System.out.println("Your current balance is: " + customer.getAccount().getBalance());
    }

    public void showWithdrawScreen(double amount) {
        if (amount > account.getBalance()) {
            System.out.println("Insufficient balance!");
        } else {
            if (account.withdraw(amount, conn)) {
                atmCashBalance -= amount;
                PaperQuantityUsed++;
                InkQuantityUsed += 5;

                System.out.println("Withdrawn: " + amount + " | ATM cash balance: " + atmCashBalance);
                System.out.println("Ink Used Per Transaction : " + 5 + " | Remaining Ink " + (InkQuantity_LIMIT - InkQuantityUsed) + " ml");
                System.out.println("Paper Used Per Transaction : " + 1 + " | Remaining Papers " + (PaperQuantity_LIMIT - PaperQuantityUsed));

                // Check if transaction limit is reached
                if (InkQuantityUsed >= InkQuantity_LIMIT) {
                    requiresInkMaintenance = true;
                    System.out.println("Maintenance required! Technician must refill Ink before further transactions.");
                }
                if (PaperQuantityUsed >= PaperQuantity_LIMIT) {
                    requiresPaperMaintenance = true;
                    System.out.println("Maintenance required! Technician must refill Paper before further transactions.");
                }
            }
        }
    }

    public void showDepositScreen(double amount) {
        if (account.deposit(amount, conn)) {
            atmCashBalance += amount;
            PaperQuantityUsed++;
            InkQuantityUsed += 5;

            System.out.println("Deposited: " + amount + " | ATM cash balance: " + atmCashBalance);
            System.out.println("Ink Used Per Transaction : " + 5 + " | Remaining Ink " + (InkQuantity_LIMIT - InkQuantityUsed) + " ml");
            System.out.println("Paper Used Per Transaction : " + 1 + " | Remaining Papers " + (PaperQuantity_LIMIT - PaperQuantityUsed));

        }
    }

    public static String readNameAndAge(Scanner scanner) {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your age: ");
        int age = scanner.nextInt();

        return "Name: " + name + ", Age: " + age;
    }

}
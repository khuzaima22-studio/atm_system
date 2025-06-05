package org.example;

import java.util.Scanner;

// ATMSystem Class
public class ATMSystem {
    private double atmCashBalance;
    private Bank bank;
    private int InkQuantityUsed;
    private int PaperQuantityUsed;
    private static final int InkQuantity_LIMIT = 20;
    private static final int PaperQuantity_LIMIT = 6;
    private boolean requiresInkMaintenance;
    private boolean requiresPaperMaintenance;

    public double getAtmCashBalance() {
        return atmCashBalance;
    }
    public boolean isrequiresInkMaintenance() {
        return requiresInkMaintenance;
    }
    public boolean isrequiresPaperMaintenance() {
        return requiresPaperMaintenance;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Initialize ATM system, Bank, Customer, and ATM Technician
        ATMSystem atmSystem = new ATMSystem();
        ATMTechnician atmTechnician = new ATMTechnician("tech123", "t9999"); // Set technician PIN
        Customer customer1 = new Customer("1234"); // Customer PIN for testing
        Customer customer2 = new Customer("7586");

        atmSystem.showWelcomeScreen();
        atmSystem.showPinEntryScreen();
        System.out.print("Enter PIN: ");
        String enteredPin = scanner.nextLine();

        // Check if the entered PIN is for ATM Technician or Customer
        if (enteredPin.equals(atmTechnician.getTechnicianPin())) {
            System.out.println("Pin validated successfully as ATM Technician!");
            atmSystem.showTechnicianMenu(atmTechnician, scanner);
        } else if (enteredPin.equals(customer1.getPin())) {
            System.out.println("Pin validated successfully as Customer 1!");
            if (atmSystem.requiresInkMaintenance) {
                System.out.println("Maintenance required! Technician must refill ink before proceeding.");
                atmSystem.showTechnicianMenu(atmTechnician, scanner);
            }
            if (atmSystem.requiresPaperMaintenance) {
                System.out.println("Maintenance required! Technician must refill paper before proceeding.");
                atmSystem.showTechnicianMenu(atmTechnician, scanner);
            }
            atmSystem.showCustomerMenu(customer1, scanner);
        } else if (enteredPin.equals(customer2.getPin())) {
            System.out.println("Pin validated successfully as Customer 2!");
            if (atmSystem.requiresInkMaintenance) {
                System.out.println("Maintenance required! Technician must refill ink before proceeding.");
                atmSystem.showTechnicianMenu(atmTechnician, scanner);
            }
            if (atmSystem.requiresPaperMaintenance) {
                System.out.println("Maintenance required! Technician must refill paper before proceeding.");
                atmSystem.showTechnicianMenu(atmTechnician, scanner);
            }
            atmSystem.showCustomerMenu(customer2, scanner);
        } else {
            System.out.println("Invalid PIN.");
        }

        scanner.close();
    }

    public ATMSystem() {
        this.atmCashBalance = 25000.0;
        this.bank = new Bank("MyBank");
        this.PaperQuantityUsed = 0;
        this.InkQuantityUsed = 0;
        this.requiresInkMaintenance = false;
        this.requiresPaperMaintenance = false;
    }

    public void showWelcomeScreen() {
        System.out.println("Welcome to the ATM System!");
    }

    public void showPinEntryScreen() {
        System.out.println("Please insert your card and enter your PIN.");
    }

    public void showCustomerMenu(Customer customer, Scanner scanner) {
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
                    showBalanceScreen(customer);
                    break;
                case 2:
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    showWithdrawScreen(customer, withdrawAmount);
                    break;
                case 3:
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    showDepositScreen(customer, depositAmount);
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

    public void showTechnicianMenu(ATMTechnician atmTechnician, Scanner scanner) {
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
                    refillInk();
                    requiresInkMaintenance = false;
                    InkQuantityUsed = 0;
                    break;
                case 2:
                    refillPaper();
                    requiresPaperMaintenance = false;
                    PaperQuantityUsed = 0;
                    break;
                case 3:
                    upgradeSystem();
                    break;
                case 4:
                    performDiagnostics();
                    break;
                case 5:
                    System.out.println("Exiting maintenance mode.");
                    technicianExit = true;
                    break;
                case 6:
                    System.out.print("Enter amount to replenish ATM with: ");
                    double replenishAmount = scanner.nextDouble();
                    replenishCash(replenishAmount);
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public void showBalanceScreen(Customer customer) {
        System.out.println("Your current balance is: " + customer.getAccount().getBalance());
    }

    public void showWithdrawScreen(Customer customer, double amount) {
        if (amount > customer.getAccount().getBalance()) {
            System.out.println("Insufficient balance!");
        } else {
            customer.getAccount().withdraw(amount);
            atmCashBalance -= amount;
            PaperQuantityUsed++;
            InkQuantityUsed+=5;

            System.out.println("Withdrawn: " + amount + " | ATM cash balance: " + atmCashBalance);
            System.out.println("Ink Used Per Transaction : " + 5 + " | Remaining Ink " + (InkQuantity_LIMIT-InkQuantityUsed) + " ml");
            System.out.println("Paper Used Per Transaction : " + 1 + " | Remaining Papers " + (PaperQuantity_LIMIT-PaperQuantityUsed));

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

    public void showDepositScreen(Customer customer, double amount) {
        customer.getAccount().deposit(amount);
        atmCashBalance += amount;
        PaperQuantityUsed++;
        InkQuantityUsed+=5;

        System.out.println("Deposited: " + amount + " | ATM cash balance: " + atmCashBalance);
        System.out.println("Ink Used Per Transaction : " + 5 + " | Remaining Ink " + (InkQuantity_LIMIT-InkQuantityUsed) + " ml");
        System.out.println("Paper Used Per Transaction : " + 1 + " | Remaining Papers " + (PaperQuantity_LIMIT-PaperQuantityUsed));

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

    public void replenishCash(double amount) {
        atmCashBalance += amount;
        System.out.println("ATM replenished with " + amount + " cash. Current ATM cash balance: " + atmCashBalance);
    }
    public void refillInk() {
        System.out.println("Refilled ATM with ink");
        requiresInkMaintenance = false; // Reset maintenance flag
        InkQuantityUsed = 0; // Reset transaction count
    }
    public void refillPaper() {
        System.out.println("Refilled ATM with printer paper.");
        requiresPaperMaintenance = false; // Reset maintenance flag
        PaperQuantityUsed = 0; // Reset transaction count
    }

    public void upgradeSystem() {
        System.out.println("Upgraded ATM system (hardware/firmware).\n");
    }

    public void performDiagnostics() {
        System.out.println("Performing diagnostics on the ATM system.");
    }
}
package org.example;

import org.junit.Test; // Import for @Test
import static org.junit.Assert.*; // Import for assertTrue
import java.util.Scanner;
 // It's an integration testing in which we test how components work together.
public class ATMSystemIntegrationTest {
    // Test no: 1 "Validation of Customer’s PIN and for checking the balance"
    @Test
    public void testCustomerPinValidationAndBalanceCheck() {
        ATMSystem atmSystem = new ATMSystem();
        Customer customer = new Customer("1234"); // Known PIN
        Scanner scanner = new Scanner("1234\n1\n4\n"); // Simulate PIN entry, balance check, and exit

        // Simulate main flow
        atmSystem.showWelcomeScreen();
        atmSystem.showPinEntryScreen();
        String enteredPin = scanner.nextLine();

        if (enteredPin.equals(customer.getPin())) {
            System.out.println("Pin validated successfully as Customer!");
            atmSystem.showCustomerMenu(customer, scanner);
        } else {
            System.out.println("Invalid PIN.");
        }

        // Verify balance is displayed
        assertTrue(customer.getAccount().getBalance() >= 0);
    }
    //Test no: 2 "Customer Withdraws Cash"
    @Test
    public void testCustomerWithdrawCash() {
        ATMSystem atmSystem = new ATMSystem();
        Customer customer = new Customer("1234"); // Known PIN

        // Verify initial balance is 15000.0
        assertEquals(15000.0, customer.getAccount().getBalance(), 0.01);

        Scanner scanner = new Scanner("1234\n2\n500\n4\n"); // Simulate PIN entry, withdraw 500, and exit

        // Simulate main flow
        atmSystem.showWelcomeScreen();
        atmSystem.showPinEntryScreen();
        String enteredPin = scanner.nextLine();

        if (enteredPin.equals(customer.getPin())) {
            System.out.println("Pin validated successfully as Customer!");
            atmSystem.showCustomerMenu(customer, scanner);
        } else {
            System.out.println("Invalid PIN.");
        }

        // Verify withdrawal
        assertEquals(14500.0, customer.getAccount().getBalance(), 0.01); // Balance should be 14500.0 after withdrawal
        assertEquals(24500.0, atmSystem.getAtmCashBalance(), 0.01); // ATM cash balance should decrease by 500
    }

    // Test no: 3 " After reaching the Transaction Limit the maintenance will be Required "
    @Test
    public void testMaintenanceRequiredAfterTransactionLimit() {
        ATMSystem atmSystem = new ATMSystem();
        Customer customer = new Customer("1234");
        customer.getAccount().deposit(1000.0); // funds will be added to the customer's account
        Scanner scanner = new Scanner("1234\n2\n100\n2\n100\n2\n100\n2\n100\n2\n100\n4\n"); // Simulate 5 withdrawals

        // Simulate main flow
        atmSystem.showWelcomeScreen();
        atmSystem.showPinEntryScreen();
        String enteredPin = scanner.nextLine();

        if (enteredPin.equals(customer.getPin())) {
            System.out.println("Pin validated successfully as Customer!");
            atmSystem.showCustomerMenu(customer, scanner);
        } else {
            System.out.println("Invalid PIN.");
        }

        //Verify maintenance flag
        assertTrue(atmSystem.isRequiresMaintenance()); // Maintenance will be required
    }
}

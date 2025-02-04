package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
   // It's a unit testing in which we are testing the individual components of ATM System
public class ATMSystemTest {
    private ATMSystem atmSystem;
    private Customer customer;

    @BeforeEach  //  Annotation for JUnit 5
    public void setUp() {
        atmSystem = new ATMSystem();
        customer = new Customer("1234");
        customer.getAccount().deposit(1000.0); // Add funds to the customer's account
    }

    // Test no: 1 "Customer Withdraws Cash Successfully"
    @Test
    public void testCustomerWithdrawCashSuccessfully() {
        // Arrange
        double initialCustomerBalance = customer.getAccount().getBalance();
        double initialATMBalance = atmSystem.getAtmCashBalance();
        double withdrawAmount = 4500.0;

        // Act
        atmSystem.showWithdrawScreen(customer, withdrawAmount);

        // Assert
        assertEquals(initialCustomerBalance - withdrawAmount, customer.getAccount().getBalance(), 0.01);
        assertEquals(initialATMBalance - withdrawAmount, atmSystem.getAtmCashBalance(), 0.01);
    }
    // Test no: 2 "ATM Requires Maintenance After Transaction Limit"
    @Test
    public void testAtmRequiresMaintenanceAfterTransactionLimitCustomer2() {
        ATMSystem atmSystem = new ATMSystem();
        Customer customer2 = new Customer("7586");

        // Perform 5 transactions for Customer 2 to reach the limit
        atmSystem.showWithdrawScreen(customer2, 1500);
        atmSystem.showWithdrawScreen(customer2, 1200);
        atmSystem.showWithdrawScreen(customer2, 2000);
        atmSystem.showWithdrawScreen(customer2, 2500);
        atmSystem.showWithdrawScreen(customer2, 5500);

        // Assert that the ATM requires maintenance
        assertTrue(atmSystem.isRequiresMaintenance(), "ATM should require maintenance after 5 transactions for Customer 2");
    }

    // Test no: 3 "Test Customer Deposit Cash with Balance Verification"
    @Test
    public void testCustomerDepositCashWithBalanceCheck() {
        // Arrange
        Customer customer1 = new Customer("1234"); // Use customer1
        double initialCustomerBalance = customer1.getAccount().getBalance();
        double initialAtmBalance = atmSystem.getAtmCashBalance();
        double depositAmount = 5500.0;

        // Act - Customer deposits money
        atmSystem.showDepositScreen(customer1, depositAmount);

        // Assert - Customer's account balance should increase by the deposit amount
        assertEquals(initialCustomerBalance + depositAmount, customer1.getAccount().getBalance(),
                "Customer's balance should be updated correctly after depositing cash.");

        // Assert - ATM balance should increase as well
        assertEquals(initialAtmBalance + depositAmount, atmSystem.getAtmCashBalance(),
                "ATM's cash balance should be updated correctly after customer deposit.");
    }
}






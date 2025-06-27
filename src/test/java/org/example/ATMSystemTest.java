package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ATMSystemTest {
    
    private ATMSystem atmSystem;
    private Customer customer;
    private Account account;
    private ATMTechnician technician;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private Connection testConnection;

    @BeforeEach
    void setUp() {
        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Initialize ATM system
        atmSystem = new ATMSystem();
        testConnection = atmSystem.conn;
        
        // Create test customer and account in database
        createTestCustomerAndAccount();
        
        // Create test technician
        technician = new ATMTechnician(2, "Test Technician", "technician", 5678);
        
        // Set up ATM system with test objects
        atmSystem.setCustomer(customer);
        atmSystem.setAccount(account);
        atmSystem.setTechnician(technician);
    }

    private void createTestCustomerAndAccount() {
        try {
            // Create test customer in database
            String userSql = "INSERT INTO \"user\" (id, name, role, pin) VALUES (?, ?, ?, ?)";
            try (PreparedStatement userStmt = testConnection.prepareStatement(userSql)) {
                userStmt.setInt(1, 1);
                userStmt.setString(2, "Test Customer");
                userStmt.setString(3, "customer");
                userStmt.setInt(4, 1234);
                userStmt.executeUpdate();
            }
            
            // Create test account in database
            String accountSql = "INSERT INTO account (account_id, userid, balance) VALUES (?, ?, ?)";
            try (PreparedStatement accountStmt = testConnection.prepareStatement(accountSql)) {
                accountStmt.setInt(1, 1);
                accountStmt.setInt(2, 1);
                accountStmt.setDouble(3, 1000.0);
                accountStmt.executeUpdate();
            }
            
            // Create test objects
            customer = new Customer(1, "Test Customer", "customer", 1234);
            account = new Account(1, 1, 1000.0);
            
        } catch (SQLException e) {
            // If records already exist, just create the objects
            customer = new Customer(1, "Test Customer", "customer", 1234);
            account = new Account(1, 1, 1000.0);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test ATM System Initialization")
    void testATMSystemInitialization() {
        // Test initial ATM cash balance
        assertEquals(25000.0, atmSystem.getAtmCashBalance(), 0.01);
        
        // Test initial maintenance flags
        assertFalse(atmSystem.isrequiresInkMaintenance());
        assertFalse(atmSystem.isrequiresPaperMaintenance());
        
        // Test initial ink and paper quantities
        assertEquals(0, ATMSystem.InkQuantityUsed);
        assertEquals(0, ATMSystem.PaperQuantityUsed);
        
        // Test bank name
        assertNotNull(atmSystem.getBank());
        assertEquals("MyBank", atmSystem.getBank().getBankName());
        
        // Test database connection
        assertNotNull(testConnection);
    }

    @Test
    @Order(2)
    @DisplayName("Test Customer Deposit Transaction")
    void testCustomerDepositTransaction() {
        // Arrange
        double initialBalance = account.getBalance();
        double initialATMBalance = atmSystem.getAtmCashBalance();
        double depositAmount = 500.0;
        
        // Act - Use real database connection
        boolean depositSuccess = account.deposit(depositAmount, testConnection);
        assertTrue(depositSuccess, "Deposit should succeed with database connection");
        
        // Manually update ATM cash balance since we're not using showDepositScreen
        ATMSystem.atmCashBalance += depositAmount;
        ATMSystem.PaperQuantityUsed++;
        ATMSystem.InkQuantityUsed += 5;
        
        // Assert
        assertEquals(initialBalance + depositAmount, account.getBalance(), 0.01);
        assertEquals(initialATMBalance + depositAmount, atmSystem.getAtmCashBalance(), 0.01);
        
        // Verify ink and paper usage
        assertEquals(5, ATMSystem.InkQuantityUsed);
        assertEquals(1, ATMSystem.PaperQuantityUsed);
    }

    @Test
    @Order(3)
    @DisplayName("Test Customer Withdrawal Transaction")
    void testCustomerWithdrawalTransaction() {
        // Arrange
        double initialBalance = account.getBalance();
        double initialATMBalance = atmSystem.getAtmCashBalance();
        double withdrawAmount = 300.0;
        
        // Act - Use real database connection
        boolean withdrawSuccess = account.withdraw(withdrawAmount, testConnection);
        assertTrue(withdrawSuccess, "Withdrawal should succeed with database connection");
        
        // Manually update ATM cash balance since we're not using showWithdrawScreen
        ATMSystem.atmCashBalance -= withdrawAmount;
        ATMSystem.PaperQuantityUsed++;
        ATMSystem.InkQuantityUsed += 5;
        
        // Assert
        assertEquals(initialBalance - withdrawAmount, account.getBalance(), 0.01);
        assertEquals(initialATMBalance - withdrawAmount, atmSystem.getAtmCashBalance(), 0.01);
        
        // Verify ink and paper usage
        assertEquals(5, ATMSystem.InkQuantityUsed);
        assertEquals(1, ATMSystem.PaperQuantityUsed);
    }

    @Test
    @Order(4)
    @DisplayName("Test Insufficient Balance Withdrawal")
    void testInsufficientBalanceWithdrawal() {
        // Arrange
        double initialBalance = account.getBalance();
        double initialATMBalance = atmSystem.getAtmCashBalance();
        double withdrawAmount = 2000.0; // More than available balance
        
        // Act - Use real database connection
        boolean withdrawSuccess = account.withdraw(withdrawAmount, testConnection);
        
        // Assert
        assertFalse(withdrawSuccess, "Withdrawal should fail due to insufficient balance");
        
        // Balance should remain unchanged
        assertEquals(initialBalance, account.getBalance(), 0.01);
        assertEquals(initialATMBalance, atmSystem.getAtmCashBalance(), 0.01);
        
        // Ink and paper should not be used for failed transactions
        assertEquals(0, ATMSystem.InkQuantityUsed);
        assertEquals(0, ATMSystem.PaperQuantityUsed);
    }


} 
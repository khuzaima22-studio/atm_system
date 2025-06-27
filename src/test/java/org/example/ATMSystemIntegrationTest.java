package org.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ATMSystemIntegrationTest {

    private static ATMSystem atmSystem;
    private static Connection testConnection;
    private static ByteArrayOutputStream outputStream;
    private static PrintStream originalOut;

    @BeforeAll
    static void setUp() throws Exception {
        // Redirect System.out to capture output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Initialize ATM system with existing database connection
        atmSystem = new ATMSystem();
        testConnection = atmSystem.conn;
        
        // Clear any existing test data
        clearTestData();
    }

    @AfterAll
    static void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
        
        try {
            if (testConnection != null && !testConnection.isClosed()) {
                testConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void clearOutput() {
        outputStream.reset();

        // Reset static state
        atmSystem.atmCashBalance = 25000.0;
        atmSystem.InkQuantityUsed = 0;
        atmSystem.PaperQuantityUsed = 0;
        atmSystem.requiresInkMaintenance = false;
        atmSystem.requiresPaperMaintenance = false;
        
        // Clear references
        atmSystem.setCustomer(null);
        atmSystem.setAccount(null);
        atmSystem.setTechnician(null);
    }

    private static void clearTestData() throws SQLException {
        try (PreparedStatement stmt = testConnection.prepareStatement("DELETE FROM user_history")) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = testConnection.prepareStatement("DELETE FROM account")) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = testConnection.prepareStatement("DELETE FROM \"user\"")) {
            stmt.executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test Customer Account Creation and Authentication")
    void testCustomerAccountCreationAndAuthentication() {
        // Create customer account
        int customerId = createTestCustomer("John Doe", 1234);
        assertThat(customerId).isGreaterThan(0);
        
        // Verify customer exists in database
        try (PreparedStatement stmt = testConnection.prepareStatement(
                "SELECT * FROM \"user\" WHERE name = ? AND role = ?")) {
            stmt.setString(1, "John Doe");
            stmt.setString(2, "customer");
            ResultSet rs = stmt.executeQuery();
            
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("John Doe");
            assertThat(rs.getInt("pin")).isEqualTo(1234);
            
            // Verify account was created
            int userId = rs.getInt("id");
            try (PreparedStatement accountStmt = testConnection.prepareStatement(
                    "SELECT * FROM account WHERE userid = ?")) {
                accountStmt.setInt(1, userId);
                ResultSet accountRs = accountStmt.executeQuery();
                
                assertThat(accountRs.next()).isTrue();
                assertThat(accountRs.getDouble("balance")).isEqualTo(0.0);
            }
        } catch (SQLException e) {
            fail("Database error: " + e.getMessage());
        }
        
        // Test authentication
        String input = "\nJohn Doe\n1234\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        
        atmSystem.showPinEntryScreen(scanner);
        
        String output = outputStream.toString();
        assertThat(output).contains("Pin validated successfully as Customer!");
    }

    @Test
    @Order(2)
    @DisplayName("Test Customer Deposit and Withdrawal Transactions")
    void testCustomerDepositAndWithdrawalTransactions() {
        // Create customer and setup
        int customerId = createTestCustomer("Transaction Customer", 5678);
        int accountId = getAccountId(customerId);
        setupCustomerAndAccount("Transaction Customer", 5678, accountId, customerId);
        
        double initialBalance = atmSystem.getAccount().getBalance();
        double initialATMBalance = atmSystem.getAtmCashBalance();
        
        // Test deposit
        double depositAmount = 1000.0;
        atmSystem.showDepositScreen(depositAmount);
        
        assertThat(atmSystem.getAccount().getBalance()).isEqualTo(initialBalance + depositAmount);
        assertThat(atmSystem.getAtmCashBalance()).isEqualTo(initialATMBalance + depositAmount);
        
        // Test withdrawal
        double withdrawAmount = 300.0;
        atmSystem.showWithdrawScreen(withdrawAmount);
        
        assertThat(atmSystem.getAccount().getBalance()).isEqualTo(initialBalance + depositAmount - withdrawAmount);
        assertThat(atmSystem.getAtmCashBalance()).isEqualTo(initialATMBalance + depositAmount - withdrawAmount);
        
        // Verify transaction history
        verifyTransactionHistory(customerId, "deposit", depositAmount);
        verifyTransactionHistory(customerId, "withdrawal", withdrawAmount);
    }

    @Test
    @Order(3)
    @DisplayName("Test ATM Maintenance Requirements")
    void testATMMaintenanceRequirements() {
        // Create customer and setup
        int customerId = createTestCustomer("Maintenance Customer", 9999);
        int accountId = getAccountId(customerId);
        setupCustomerAndAccount("Maintenance Customer", 9999, accountId, customerId);
        
        // Deposit money first
        atmSystem.getAccount().deposit(10000.0, testConnection);
        
        // Perform transactions to reach maintenance limits
        // Ink limit: 20ml, 5ml per transaction = 4 transactions
        for (int i = 0; i < 4; i++) {
            atmSystem.showWithdrawScreen(100.0);
        }
        
        // Verify maintenance is required
        assertThat(atmSystem.isrequiresInkMaintenance()).isTrue();
        
        String output = outputStream.toString();
        assertThat(output).contains("Maintenance required! Technician must refill Ink");
    }

    @Test
    @Order(4)
    @DisplayName("Test Technician Maintenance Operations")
    void testTechnicianMaintenanceOperations() {
        // Create technician
        int technicianId = createTestTechnician("Maintenance Tech", 1111);
        atmSystem.setTechnician(new ATMTechnician(technicianId, "Maintenance Tech", "technician", 1111));
        
        // Set maintenance flags
        ATMSystem.requiresInkMaintenance = true;
        ATMSystem.requiresPaperMaintenance = true;
        ATMSystem.InkQuantityUsed = 20;
        ATMSystem.PaperQuantityUsed = 6;
        
        // Test ink refill
        atmSystem.getTechnician().refillInk();
        assertThat(atmSystem.isrequiresInkMaintenance()).isFalse();
        assertThat(ATMSystem.InkQuantityUsed).isEqualTo(0);
        
        // Test paper refill
        atmSystem.getTechnician().refillPaper();
        assertThat(atmSystem.isrequiresPaperMaintenance()).isFalse();
        assertThat(ATMSystem.PaperQuantityUsed).isEqualTo(0);
        
        // Test cash replenishment
        double initialATMBalance = atmSystem.getAtmCashBalance();
        double replenishAmount = 5000.0;
        atmSystem.getTechnician().replenishCash(replenishAmount);
        assertThat(atmSystem.getAtmCashBalance()).isEqualTo(initialATMBalance + replenishAmount);
    }

    @Test
    @Order(5)
    @DisplayName("Test Complete ATM Workflow")
    void testCompleteATMWorkflow() {
        // Create customer
        int customerId = createTestCustomer("Workflow Customer", 2222);
        int accountId = getAccountId(customerId);
        setupCustomerAndAccount("Workflow Customer", 2222, accountId, customerId);
        
        // 1. Check initial balance
        atmSystem.showBalanceScreen(customerId);
        assertThat(atmSystem.getAccount().getBalance()).isEqualTo(0.0);
        
        // 2. Deposit money
        atmSystem.showDepositScreen(2000.0);
        assertThat(atmSystem.getAccount().getBalance()).isEqualTo(2000.0);
        
        // 3. Check balance after deposit
        atmSystem.showBalanceScreen(customerId);
        
        // 4. Withdraw money
        atmSystem.showWithdrawScreen(500.0);
        assertThat(atmSystem.getAccount().getBalance()).isEqualTo(1500.0);
        
        // 5. Check final balance
        atmSystem.showBalanceScreen(customerId);
        
        // 6. Verify all transactions were recorded
        verifyTransactionHistory(customerId, "deposit", 2000.0);
        verifyTransactionHistory(customerId, "withdrawal", 500.0);
        
        // 7. Verify ATM cash balance changes
        assertThat(atmSystem.getAtmCashBalance()).isEqualTo(25000.0 + 2000.0 - 500.0);
    }

    // Helper methods
    private int createTestCustomer(String name, int pin) {
        try (PreparedStatement stmt = testConnection.prepareStatement(
                "INSERT INTO \"user\" (name, role, pin) VALUES (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, "customer");
            stmt.setInt(3, pin);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                
                // Create account
                try (PreparedStatement accountStmt = testConnection.prepareStatement(
                        "INSERT INTO account (userid, balance) VALUES (?, ?)")) {
                    accountStmt.setInt(1, userId);
                    accountStmt.setDouble(2, 0.0);
                    accountStmt.executeUpdate();
                }
                
                return userId;
            }
        } catch (SQLException e) {
            fail("Failed to create test customer: " + e.getMessage());
        }
        return -1;
    }

    private int createTestTechnician(String name, int pin) {
        try (PreparedStatement stmt = testConnection.prepareStatement(
                "INSERT INTO \"user\" (name, role, pin) VALUES (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, "technician");
            stmt.setInt(3, pin);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            fail("Failed to create test technician: " + e.getMessage());
        }
        return -1;
    }

    private int getAccountId(int userId) {
        try (PreparedStatement stmt = testConnection.prepareStatement(
                "SELECT account_id FROM account WHERE userid = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("account_id");
            }
        } catch (SQLException e) {
            fail("Failed to get account ID: " + e.getMessage());
        }
        return -1;
    }

    private void setupCustomerAndAccount(String name, int pin, int accountId, int customerId) {
        atmSystem.setCustomer(new Customer(customerId, name, "customer", pin));
        atmSystem.setAccount(new Account(accountId, customerId, 0.0));
    }

    private void verifyTransactionHistory(int userId, String transactionType, double amount) {
        try (PreparedStatement stmt = testConnection.prepareStatement(
                "SELECT * FROM user_history WHERE user_id = ? AND transaction_type = ? AND amount = ?")) {
            stmt.setInt(1, userId);
            stmt.setString(2, transactionType);
            stmt.setDouble(3, amount);
            ResultSet rs = stmt.executeQuery();
            
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("transaction_type")).isEqualTo(transactionType);
            assertThat(rs.getDouble("amount")).isEqualTo(amount);
        } catch (SQLException e) {
            fail("Failed to verify transaction history: " + e.getMessage());
        }
    }
} 
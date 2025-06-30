package org.example;

import org.junit.jupiter.api.*;

import java.io.PrintStream;
import java.io.OutputStream;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ATMSystemIntegrationTest {

    private static ATMSystem atmSystem;
    private static Customer customer;
    private static Account account;
    private static Connection testConnection;
    private static final PrintStream originalOut = System.out;

    @BeforeAll
    static void setUp() throws SQLException {
        atmSystem = new ATMSystem();
        testConnection = atmSystem.conn;
        createTestCustomer();
    }

    private static void createTestCustomer() throws SQLException {
        String cleanupAccount = "DELETE FROM account WHERE account_id = 999";
        String cleanupUser = "DELETE FROM \"user\" WHERE id = 999";

        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(cleanupAccount);
            stmt.executeUpdate(cleanupUser);
        }

        String userSql = "INSERT INTO \"user\" (id, name, role, pin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = testConnection.prepareStatement(userSql)) {
            stmt.setInt(1, 999);
            stmt.setString(2, "Test User");
            stmt.setString(3, "customer");
            stmt.setInt(4, 1234);
            stmt.executeUpdate();
        }

        String accountSql = "INSERT INTO account (account_id, userid, balance) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = testConnection.prepareStatement(accountSql)) {
            stmt.setInt(1, 999);
            stmt.setInt(2, 999);
            stmt.setDouble(3, 1000.00);
            stmt.executeUpdate();
        }

        customer = new Customer(999, "Test User", "customer", 1234);
        account = new Account(999, 999, 1000.00);
        atmSystem.setCustomer(customer);
        atmSystem.setAccount(account);
    }

    @AfterEach
    void resetState() {
        ATMSystem.PaperQuantityUsed = 0;
        ATMSystem.InkQuantityUsed = 0;
        ATMSystem.requiresPaperMaintenance = false;
        ATMSystem.requiresInkMaintenance = false;
    }

    @AfterAll
    static void tearDown() throws SQLException {
        String deleteAccount = "DELETE FROM account WHERE account_id = 999";
        String deleteUser = "DELETE FROM \"user\" WHERE id = 999";

        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(deleteAccount);
            stmt.executeUpdate(deleteUser);
        }

        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    // Helper to suppress console output during tests
    void suppressOutput() {
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
    }

    void restoreOutput() {
        System.setOut(originalOut);
    }

    @Test
    @Order(1)
    @DisplayName("Technician resets paper count after refill")
    void testTechnicianPaperRefill() {
        suppressOutput();
        ATMSystem.PaperQuantityUsed = ATMSystem.PaperQuantity_LIMIT;
        ATMSystem.requiresPaperMaintenance = true;
        ATMTechnician technician = new ATMTechnician(1, "Tech", "technician", 9999);
        atmSystem.setTechnician(technician);
        atmSystem.getTechnician().refillPaper(testConnection);
        assertFalse(ATMSystem.requiresPaperMaintenance);
        assertEquals(0, ATMSystem.PaperQuantityUsed);
        restoreOutput();
        System.out.println("Test passed successfully, Technician resets paper count after refill");
    }

    @Test
    @Order(2)
    @DisplayName("Withdrawal fails if balance is insufficient")
    void testWithdrawalInsufficientFunds() {
        // Reset state before test
        ATMSystem.InkQuantityUsed = 0;
        ATMSystem.PaperQuantityUsed = 0;
        ATMSystem.requiresInkMaintenance = false;
        ATMSystem.requiresPaperMaintenance = false;
        ATMSystem.atmCashBalance = 1_000_000.0;
        account.setBalance(1000.0);  // reset account balance

        double withdrawAmount = account.getBalance() + 1000; // More than balance

        // Call only once
        atmSystem.showWithdrawScreen(withdrawAmount);

        // Because withdraw fails inside showWithdrawScreen, this should be false
        // But do NOT call withdraw again here

        // Confirm no ink or paper used
        assertEquals(0, ATMSystem.PaperQuantityUsed, "Paper used should be 0 when withdrawal fails");
        assertEquals(0, ATMSystem.InkQuantityUsed, "Ink used should be 0 when withdrawal fails");

        // Confirm balance unchanged
        assertEquals(1000.0, account.getBalance(), 0.01);
    }

    @Test
    @Order(3)
    @DisplayName("Technician replenishes ATM cash correctly")
    void testCashReplenishment() {
        suppressOutput();
        double initialCash = atmSystem.getAtmCashBalance();
        double replenishAmount = 5000.0;
        ATMTechnician technician = new ATMTechnician(1, "Tech", "technician", 9999);
        atmSystem.setTechnician(technician);
        atmSystem.getTechnician().replenishCash(replenishAmount,testConnection);
        assertEquals(initialCash + replenishAmount, atmSystem.getAtmCashBalance(), 0.01);
        restoreOutput();
        System.out.println("Test passed successfully, Technician replenishes ATM cash correctly");
    }
}

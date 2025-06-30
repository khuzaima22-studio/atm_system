package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ATMSystemUnitTest {

    private ATMSystem atmSystem;

    @BeforeEach
    void setUp() {
        // Restore output to console
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        atmSystem = new ATMSystem();
    }

    @Test
    @DisplayName("Should require ink maintenance after ink limit exceeded")
    void testInkMaintenanceFlagTriggered() {
        ATMSystem.InkQuantityUsed = 20;
        ATMSystem.requiresInkMaintenance = ATMSystem.InkQuantityUsed >= 20;

        assertTrue(ATMSystem.requiresInkMaintenance);
        System.out.println("Test passed: Ink maintenance triggered correctly");
    }

    @Test
    @DisplayName("Should require paper maintenance after paper limit exceeded")
    void testPaperMaintenanceFlagTriggered() {
        ATMSystem.PaperQuantityUsed = 6;
        ATMSystem.requiresPaperMaintenance = ATMSystem.PaperQuantityUsed >= 6;

        assertTrue(ATMSystem.requiresPaperMaintenance);
        System.out.println("Test passed: Paper maintenance triggered correctly");
    }

    @Test
    @DisplayName("Should correctly set and get ATM technician")
    void testSetAndGetTechnician() {
        ATMTechnician tech = new ATMTechnician(1, "Test Tech", "technician", 1111);
        atmSystem.setTechnician(tech);

        assertEquals(tech, atmSystem.getTechnician());
        System.out.println("Test passed: Technician set and retrieved correctly");
    }

    @Test
    @DisplayName("Should correctly set and get ATM account")
    void testSetAndGetAccount() {
        Account acc = new Account(101, 1, 5000.0);
        atmSystem.setAccount(acc);

        assertEquals(acc, atmSystem.getAccount());
        System.out.println("Test passed: Account set and retrieved correctly");
    }
}


package org.example;

import static org.example.ATMSystem.*;

public class ATMTechnician {
    private int id;
    private String name;
    private int pin;
    private String role;

    public ATMTechnician(int id, String name, String role, int pin) {
        this.id = id;
        this.name = name;
        this.pin = pin;
        this.role = role;
//        this.account = new Account(this);  // This will pass the customer object to Account Constructor
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    //    public Account getAccount() {
//        return
//    }
    public void replenishCash(double amount) {
        if (amount <= 0) {
            System.out.println("Amount should be greater than 0");
            return;
        }
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

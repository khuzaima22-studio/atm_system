package org.example;

public class Account {
    private String accountNumber;
    private double balance;
    private Customer owner;

    public Account(Customer owner) {
        this.owner = owner;
        this.balance = 15000.0;
        this.accountNumber = "ACC123456";
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount > balance) {
            System.out.println("Insufficient balance!");
            return;  // No change to balance if insufficient funds
        }
        balance -= amount; // Update the balance only if withdrawal is successful
    }


    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getOwner() {
        return owner;
    }
}

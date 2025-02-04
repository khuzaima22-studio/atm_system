package org.example;

public class Customer {
    private String pin;
    private Account account;

    public Customer(String pin) {
        this.pin = pin;
        this.account = new Account(this);  // This will pass the customer object to Account Constructor
    }

    public String getPin() {
        return pin;
    }

    public Account getAccount() {
        return account;
    }
}

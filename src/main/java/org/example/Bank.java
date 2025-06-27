package org.example;

public class Bank {
    private String bankName;

    public Bank(String bankName) {
        this.bankName = bankName;
    }

//    public Account createAccount(Customer customer) {
//        return new Account(customer);
//    }

    public double getAccountBalance(Account account) {
        return account.getBalance();
    }

    public String getBankName() {
        return bankName;
    }


}

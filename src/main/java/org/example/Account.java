package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Account {
    //    private String accountNumber;
    private double balance;
    private int userId;
    private int pin;
    int id;

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Account(int id,int userid, double b) {
        this.userId = userid;
        this.balance = b;
        this.id=id;
//        this.accountNumber = "ACC123456";
    }
    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getPin() {
        return pin;
    }

    public boolean deposit(double amount, Connection conn) {
        if(amount<=0)
        {
            System.out.println("Amount should be greater than 0");
            return false;
        }
        
        // Update balance regardless of database connection (for unit testing)
        balance += amount;
        
        if (conn != null) {
            String updateSql = "UPDATE account SET balance = ? WHERE userid = ?";
            String historySql = "INSERT INTO user_history (user_id, transaction_type, amount, description) VALUES (?, ?, ?, ?)";

            try (
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    PreparedStatement historyStmt = conn.prepareStatement(historySql)
            ) {
                // Update account balance
                updateStmt.setDouble(1, balance);
                updateStmt.setInt(2, userId);
                int rows = updateStmt.executeUpdate();

                if (rows > 0) {
                    // Insert transaction history
                    historyStmt.setInt(1, userId);
                    historyStmt.setString(2, "deposit");
                    historyStmt.setDouble(3, amount);
                    historyStmt.setString(4, "Deposit to account");

                    int historyRows = historyStmt.executeUpdate();
                    if (historyRows > 0) {
                        System.out.println("Deposit successful and history recorded.");
                    } else {
                        System.out.println("Deposit succeeded but history not recorded.");
                    }

                    return true;
                } else {
                    System.out.println("Deposit failed. Account not found.");
                    // Revert balance change if database update failed
                    balance -= amount;
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Revert balance change if database operation failed
                balance -= amount;
                return false;
            }
        } else {
            // For unit testing without database connection
            System.out.println("Deposit successful (no database connection).");
            return true;
        }
    }


    public boolean withdraw(double amount, Connection conn) {
        if (amount > balance) {
            System.out.println("Insufficient balance!");
            return false;
        }

        // Update balance regardless of database connection (for unit testing)
        balance -= amount;

        if (conn != null) {
            String updateSql = "UPDATE account SET balance = ? WHERE userid = ?";
            String historySql = "INSERT INTO user_history (user_id, transaction_type, amount, description) VALUES (?, ?, ?, ?)";

            try (
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    PreparedStatement historyStmt = conn.prepareStatement(historySql)
            ) {
                // Update the account balance
                updateStmt.setDouble(1, balance);
                updateStmt.setInt(2, userId);
                int rows = updateStmt.executeUpdate();

                if (rows > 0) {
                    // Insert into account_history
                    historyStmt.setInt(1, userId);
                    historyStmt.setString(2, "withdrawal");
                    historyStmt.setDouble(3, amount);
                    historyStmt.setString(4, "Withdrawal from account");

                    int historyRows = historyStmt.executeUpdate();
                    if (historyRows > 0) {
                        System.out.println("Withdraw successful and history recorded.");
                    } else {
                        System.out.println("Withdraw succeeded but history not recorded.");
                    }

                    return true;
                } else {
                    System.out.println("Withdraw failed.");
                    // Revert balance change if database update failed
                    balance += amount;
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Revert balance change if database operation failed
                balance += amount;
                return false;
            }
        } else {
            // For unit testing without database connection
            System.out.println("Withdraw successful (no database connection).");
            return true;
        }
    }



    public double getBalance() {
        return balance;
    }

    public int getUserId() {
        return userId;
    }
//    public String getAccountNumber() {
//        return accountNumber;
//    }

//    public Customer getOwner() {
//        return owner;
//    }
}

package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLJDBC {
    private final String url = "jdbc:postgresql://localhost:5432/Atm_system";
    private final String user = "postgres";
    private final String password = "Mughal@19!";

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
            return null;
        }
    }
    public void initializeTables() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS \"user\" (" +
                "id SERIAL PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "role VARCHAR(50) NOT NULL," +
                "pin INT NOT NULL" +
                ");";

        String createAccountTable = "CREATE TABLE IF NOT EXISTS account (" +
                "account_id SERIAL PRIMARY KEY," +
                "userid INTEGER REFERENCES \"user\"(id) ON DELETE CASCADE," +
                "balance DOUBLE PRECISION NOT NULL" +
                ");";

        String createHistoryTable = "CREATE TABLE IF NOT EXISTS user_history (" +
                "id SERIAL PRIMARY KEY," +
                "user_id INTEGER REFERENCES \"user\"(id) ON DELETE CASCADE," +
                "transaction_type VARCHAR(20) NOT NULL," +
                "amount DOUBLE PRECISION NOT NULL," +
                "description TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = getConnection()) {
            if (conn != null) {
                conn.createStatement().executeUpdate(createUserTable);
                conn.createStatement().executeUpdate(createAccountTable);
                conn.createStatement().executeUpdate(createHistoryTable);
                //System.out.println("Database tables set up successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to create tables.");
            e.printStackTrace();
        }
    }


}

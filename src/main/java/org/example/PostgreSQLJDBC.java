package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgreSQLJDBC {
    private final String url = "jdbc:postgresql://localhost:5432/Atm_system";
    private final String user = "postgres";
    private final String password = "12345678";

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
        String createMaintenanceTable = "CREATE TABLE IF NOT EXISTS atm_maintenance (" +
                "id SERIAL PRIMARY KEY," +
                "ink_amount_used INTEGER DEFAULT 0," +
                "paper_amount_used INTEGER DEFAULT 0,"+
                "atmCashBalance DOUBLE PRECISION DEFAULT 0,"+
                "last_updated TIMESTAMP DEFAULT NOW()"+
                ");";

        try (Connection conn = getConnection()) {
            if (conn != null) {
                conn.createStatement().executeUpdate(createUserTable);
                conn.createStatement().executeUpdate(createAccountTable);
                conn.createStatement().executeUpdate(createHistoryTable);
                conn.createStatement().executeUpdate(createMaintenanceTable);
                initializeMaintenanceTable(conn);
                //System.out.println("Database tables set up successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to create tables.");
            e.printStackTrace();
        }
    }

    public void initializeMaintenanceTable(Connection conn)
    {
        String insertQuery = "INSERT INTO atm_maintenance (ink_amount_used, paper_amount_used, atmCashBalance) " +
                "SELECT 0, 0, 1000000.0 WHERE NOT EXISTS (SELECT 1 FROM atm_maintenance);";

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Initial maintenance row inserted.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting initial maintenance row: " + e.getMessage());
        }
    }

}

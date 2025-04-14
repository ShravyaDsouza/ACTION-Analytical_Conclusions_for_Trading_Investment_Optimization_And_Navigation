package utils;
/*
import services.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestDBConnection {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();

            // User actions
            insertUser(conn, "Shravya", "shravya@example.com", "9876543210", "hashed_password");
            readUsers(conn);

            // Holdings actions
            insertHolding(conn, 1, "AAPL", "Apple Inc.", 10.5, 150.0);
            readHoldings(conn, 1);

            // Watchlist actions
            insertWatchlist(conn, 1, "TSLA", "Tesla Inc.");
            readWatchlist(conn, 1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeConnection();
        }
    }

    // ---------------- USERS ----------------
    private static void insertUser(Connection conn, String name, String email, String mobile, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (name, email, mobile_no, password_hash) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, mobile);
            pstmt.setString(4, passwordHash);
            pstmt.executeUpdate();
            System.out.println("✅ User inserted: " + name);
        }
    }

    private static void readUsers(Connection conn) throws SQLException {
        String sql = "SELECT * FROM users";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("👤 Registered Users:");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("name") + " | " + rs.getString("email") + " | " + rs.getString("mobile_no"));
            }
        }
    }

    // ---------------- HOLDINGS ----------------
    private static void insertHolding(Connection conn, int userId, String symbol, String company, double shares, double avgPrice) throws SQLException {
        String sql = "INSERT INTO holdings (user_id, stock_symbol, company_name, shares_owned, average_price, equity_value) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        double equityValue = shares * avgPrice;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, symbol);
            pstmt.setString(3, company);
            pstmt.setDouble(4, shares);
            pstmt.setDouble(5, avgPrice);
            pstmt.setDouble(6, equityValue);
            pstmt.executeUpdate();
            System.out.println("📈 Holding added for user " + userId);
        }
    }

    private static void readHoldings(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM holdings WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("📊 Holdings for User ID " + userId + ":");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("stock_symbol") + ": " +
                        rs.getDouble("shares_owned") + " shares @ $" +
                        rs.getDouble("average_price") + " = $" +
                        rs.getDouble("equity_value"));
            }
        }
    }

    // ---------------- WATCHLIST ----------------
    private static void insertWatchlist(Connection conn, int userId, String symbol, String company) throws SQLException {
        String sql = "INSERT INTO watchlist (user_id, stock_symbol, company_name) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, symbol);
            pstmt.setString(3, company);
            pstmt.executeUpdate();
            System.out.println("👁️ Stock added to watchlist: " + symbol);
        }
    }

    private static void readWatchlist(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM watchlist WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("👁️ Watchlist for User ID " + userId + ":");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("stock_symbol") + " (" + rs.getString("company_name") + ")");
            }
        }
    }
}*/

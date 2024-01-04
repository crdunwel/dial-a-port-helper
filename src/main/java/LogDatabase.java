import org.sqlite.SQLiteErrorCode;

import java.sql.*;

public class LogDatabase {

    public LogDatabase() {
        createLogTable();
        createDonationsTable();
        createTipperTable();
    }

    private Connection connectDB() {
        String url = "jdbc:sqlite:dap-db.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean insertLog(String timestamp, String sender, String receiver, String message) {
        String sql = "INSERT INTO logs(timestamp, sender, receiver, message) VALUES(?,?,?,?)";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, timestamp);
            pstmt.setString(2, sender);
            pstmt.setString(3, receiver);
            pstmt.setString(4, message);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                System.out.println("Duplicate log entry, not inserted.");
            } else {
                System.out.println(e.getMessage());
            }
            return false;
        }
    }
    public boolean insertDonation(String timestamp, String sender, int pp, int gp, int sp, int cp) {
        String sql = "INSERT INTO donations(timestamp, sender, pp, gp, sp, cp) VALUES(?,?,?,?,?,?)";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, timestamp);
            pstmt.setString(2, sender);
            pstmt.setInt(3, pp);
            pstmt.setInt(4, gp);
            pstmt.setInt(5, sp);
            pstmt.setInt(6, cp);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int getTipperStatus(String userName) {
        String sql = "SELECT status FROM tippers WHERE userName = ?";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();

            // Check if the userName was found
            if (rs.next()) {
                return rs.getInt("status");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    public boolean setTipperStatus(String userName, int status) {
        String sql = "INSERT INTO tippers (userName, status) VALUES(?, ?) "
                + "ON CONFLICT(userName) DO UPDATE SET status = excluded.status;";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userName);
            pstmt.setInt(2, status);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error updating the tipper status: " + e.getMessage());
            return false;
        }
    }

    private void createTipperTable() {
        String sql = "CREATE TABLE IF NOT EXISTS tippers ("
                + " id INTEGER PRIMARY KEY," // Auto-increment implied
                + " userName TEXT NOT NULL,"
                + " status INTEGER NOT NULL DEFAULT 0,"
                + " UNIQUE(userName)"
                + ");";

        try (Connection conn = this.connectDB();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createLogTable() {
        String sql = "CREATE TABLE IF NOT EXISTS logs (\n"
                + " id integer PRIMARY KEY,\n"
                + " timestamp text NOT NULL,\n"
                + " sender text NOT NULL,\n"
                + " receiver text NOT NULL,\n"
                + " message text NOT NULL,\n"
                + " UNIQUE(timestamp, sender, receiver, message)\n"
                + ");";

        try (Connection conn = this.connectDB();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createDonationsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS donations (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " timestamp TEXT NOT NULL,\n"
                + " sender TEXT NOT NULL,\n"
                + " pp INTEGER NOT NULL,\n"
                + " gp INTEGER NOT NULL,\n"
                + " sp INTEGER NOT NULL,\n"
                + " cp INTEGER NOT NULL,\n"
                + " UNIQUE(timestamp, sender)\n"
                + ");";

        try (Connection conn = this.connectDB();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}

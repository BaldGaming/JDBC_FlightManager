package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Updates the delay status for flight {@code LX354} in the FlightDB database.
 *
 * <p>The previous implementation inserted the user's delay status directly into an SQL string
 * and executed it with a plain {@link java.sql.Statement}. Since the input was treated as part
 * of the SQL syntax, a crafted value containing quotes and SQL expressions could change the
 * meaning of the update and potentially affect rows beyond the intended flight.</p>
 *
 * <p>This implementation uses a {@link PreparedStatement} with placeholders for both values.
 * The SQL structure is prepared separately from the supplied data, and JDBC sends each bound
 * value as a parameter rather than parsing it as SQL. Thus, SQL syntax typed as a delay status
 * remains data and cannot change the statement's structure.</p>
 */
public class SimpleFlightUpdater {

    private static final String URL = "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static final String USER = "root";

    /**
     * Prompts for the database password and a new status, then securely updates flight
     * {@code LX354}.
     *
     * <p>The status and flight number are bound to the prepared SQL statement using
     * {@link PreparedStatement#setString(int, String)}. Binding keeps both values separate
     * from the SQL syntax and prevents user input from being interpreted as executable SQL.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Please enter your MySQL root password: ");
            String password = scanner.nextLine();

            System.out.print("Type a new delay status for flight 'LX354': ");
            String newStatus = scanner.nextLine();

            String sql = "UPDATE Arrivals SET delay_status = ? WHERE flight_number = ?";

            try (Connection conn = DriverManager.getConnection(URL, USER, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, "LX354");
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Number of affected rows: " + affectedRows);

            } catch (SQLException e) {
                System.err.println("Database update failed: " + e.getMessage());
            }
        }
    }
}
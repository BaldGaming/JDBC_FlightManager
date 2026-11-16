package src;


import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class AdvancedFlightDispatcher {
    private static final String URL =
            "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the database password: ");
            String password = scanner.nextLine();

            try (Connection conn = DriverManager.getConnection(URL, "root", password)) {
                updateMultipleDelays(conn, Arrays.asList(1, 3, 5), "Landed - Final");
            } catch (SQLException e) {
                System.err.println("Database operation failed: " + e.getMessage());
            }
        }
    }

    public static void updateMultipleDelays(
            Connection conn, List<Integer> arrivalIds, String newStatus) throws SQLException {
        String sql = "UPDATE Arrivals SET delay_status = ? WHERE id = ?";

        // Use try-with-resources for PreparedStatement
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // CRITICAL: Set the constant parameter ONCE outside the loop
            pstmt.setString(1, newStatus);

            for (Integer id : arrivalIds) {
                pstmt.setInt(2, id);
                pstmt.addBatch(); // Add to batch instead of executing immediately
            }

            // Execute all updates in a single batch
            int[] results = pstmt.executeBatch();
            long totalAffectedRows = 0;
            int countsNotReported = 0;

            for (int result : results) {
                if (result >= 0) {
                    totalAffectedRows += result;
                } else if (result == Statement.SUCCESS_NO_INFO) {
                    countsNotReported++;
                }
            }

            System.out.printf(
                    "Batch update complete: %d affected rows reported for %d arrivals.",
                    totalAffectedRows, results.length);
            if (countsNotReported > 0) {
                System.out.printf(" The affected-row count was not reported for %d update(s).",
                        countsNotReported);
            }
            System.out.println();
        }
    }
}
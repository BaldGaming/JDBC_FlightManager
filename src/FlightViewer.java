package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class FlightViewer {
    private static final String URL =
            "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    public static void main(String[] args) {
        String sql = "SELECT a.id, a.flight_number, r.from_city, a.scheduled, "
                + "a.delay_status, a.terminal "
                + "FROM Arrivals a "
                + "INNER JOIN FlightRoutes r ON a.flight_number = r.flight_number "
                + "WHERE a.terminal = ? "
                + "ORDER BY a.scheduled ASC";

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the MySQL root password: ");
            String password = scanner.nextLine();

            System.out.print("Enter a target terminal (2, 3, 4, or 5): ");
            String terminal = scanner.nextLine().trim();
            while (!terminal.matches("[2-5]")) {
                System.out.print("Invalid terminal. Enter 2, 3, 4, or 5: ");
                terminal = scanner.nextLine().trim();
            }

            try (Connection conn = DriverManager.getConnection(URL, "root", password);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, terminal);

                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.printf("%-5s | %-10s | %-15s | %-20s | %-20s | %-10s%n",
                            "ID", "Flight", "From", "Scheduled", "Delay Status", "Terminal");
                    System.out.println("---------------------------------------------------------------------------------------");

                    int rowCount = 0;
                    while (rs.next()) {
                        System.out.printf("%-5d | %-10s | %-15s | %-20s | %-20s | %-10s%n",
                                rs.getInt("id"),
                                rs.getString("flight_number"),
                                rs.getString("from_city"),
                                rs.getString("scheduled"),
                                rs.getString("delay_status"),
                                rs.getString("terminal"));
                        rowCount++;
                    }

                    System.out.println("---------------------------------------------------------------------------------------");
                    System.out.println("Flights displayed: " + rowCount);
                }
            } catch (SQLException e) {
                System.err.println("Unable to retrieve flight details: " + e.getMessage());
            }
        }
    }
}

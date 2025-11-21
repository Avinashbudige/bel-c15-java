import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {

    private static final String DB_URL = System.getenv("JDBC_URL");
    private static final String USER = System.getenv("JDBC_USER");
    private static final String PASS = System.getenv("JDBC_PASS");

    public static void main(String[] args) {
        if (DB_URL == null) {
            System.err.println("Please run this via the run_with_h2.sh or run_with_postgres.sh script.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected to database.");
            initializeSpots(conn);

            while (true) {
                System.out.println("\n=== Smart Parking System ===");
                System.out.println("1. Park Vehicle (Check-In)");
                System.out.println("2. Unpark Vehicle (Check-Out)");
                System.out.println("3. View Availability");
                System.out.println("4. Exit");
                System.out.print("Select option: ");

                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine();

                if ("1".equals(input)) {
                    System.out.print("Enter Vehicle Reg: ");
                    String reg = scanner.nextLine();
                    System.out.print("Enter Size (SMALL, MEDIUM, LARGE): ");
                    String size = scanner.nextLine().toUpperCase();
                    parkVehicle(conn, reg, size);
                } else if ("2".equals(input)) {
                    System.out.print("Enter Ticket ID: ");
                    try {
                        String idStr = scanner.nextLine();
                        long ticketId = Long.parseLong(idStr);
                        unparkVehicle(conn, ticketId);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                } else if ("3".equals(input)) {
                    showAvailability(conn);
                } else if ("4".equals(input)) {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Requirement: Real-Time Availability Update & Spot Allocation
    // Requirement: Concurrency Handling (synchronized via DB transaction)
    private static void parkVehicle(Connection conn, String reg, String size) throws SQLException {
        conn.setAutoCommit(false); // Start Transaction
        try {
            // 1. Find available spot (Locking it to prevent race conditions)
            // Note: H2 syntax. For Postgres use "FOR UPDATE SKIP LOCKED" for better concurrency
            String findSpotSql = "SELECT id, spot_number FROM parking_spot WHERE size = ? AND status = 'AVAILABLE' ORDER BY id LIMIT 1 FOR UPDATE";
            
            long spotId = -1;
            String spotNum = "";

            try (PreparedStatement ps = conn.prepareStatement(findSpotSql)) {
                ps.setString(1, size);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    spotId = rs.getLong("id");
                    spotNum = rs.getString("spot_number");
                } else {
                    System.out.println("No spots available for size: " + size);
                    conn.rollback();
                    return;
                }
            }

            // 2. Update Spot Status
            String updateSpotSql = "UPDATE parking_spot SET status = 'OCCUPIED' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSpotSql)) {
                ps.setLong(1, spotId);
                ps.executeUpdate();
            }

            // 3. Create Ticket (Check-In)
            String createTicketSql = "INSERT INTO parking_ticket (spot_id, vehicle_reg, vehicle_size, entry_time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(createTicketSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, spotId);
                ps.setString(2, reg);
                ps.setString(3, size);
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("Vehicle Parked! Spot: " + spotNum + ", Ticket ID: " + rs.getLong(1));
                }
            }

            conn.commit(); // Commit Transaction
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Requirement: Fee Calculation & Check-Out
    private static void unparkVehicle(Connection conn, long ticketId) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // 1. Retrieve Ticket
            String getTicketSql = "SELECT t.id, t.entry_time, t.spot_id, s.size FROM parking_ticket t JOIN parking_spot s ON t.spot_id = s.id WHERE t.id = ? AND t.exit_time IS NULL";
            long spotId = -1;
            LocalDateTime entryTime = null;
            String size = "";

            try (PreparedStatement ps = conn.prepareStatement(getTicketSql)) {
                ps.setLong(1, ticketId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    spotId = rs.getLong("spot_id");
                    entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
                    size = rs.getString("size");
                } else {
                    System.out.println("Active ticket not found.");
                    conn.rollback();
                    return;
                }
            }

            // 2. Calculate Fee
            LocalDateTime exitTime = LocalDateTime.now();
            double fee = calculateFee(entryTime, exitTime, size);

            // 3. Update Ticket
            String updateTicketSql = "UPDATE parking_ticket SET exit_time = ?, fee = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateTicketSql)) {
                ps.setTimestamp(1, Timestamp.valueOf(exitTime));
                ps.setDouble(2, fee);
                ps.setLong(3, ticketId);
                ps.executeUpdate();
            }

            // 4. Free up Spot
            String freeSpotSql = "UPDATE parking_spot SET status = 'AVAILABLE' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(freeSpotSql)) {
                ps.setLong(1, spotId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Vehicle Unparked. Duration: " + Duration.between(entryTime, exitTime).toMinutes() + " mins. Fee: $" + fee);

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static double calculateFee(LocalDateTime start, LocalDateTime end, String size) {
        long hours = Duration.between(start, end).toHours();
        if (hours == 0) hours = 1; // Minimum 1 hour
        
        double rate = 10.0;
        if ("MEDIUM".equals(size)) rate = 15.0;
        if ("LARGE".equals(size)) rate = 20.0;

        return hours * rate;
    }

    private static void showAvailability(Connection conn) throws SQLException {
        String sql = "SELECT size, COUNT(*) as count FROM parking_spot WHERE status = 'AVAILABLE' GROUP BY size";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("--- Available Spots ---");
            while (rs.next()) {
                System.out.println(rs.getString("size") + ": " + rs.getInt("count"));
            }
        }
    }

    private static void initializeSpots(Connection conn) throws SQLException {
        // Helper to seed DB if empty
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM parking_spot");
            rs.next();
            if (rs.getInt(1) == 0) {
                System.out.println("Seeding database with spots...");
                stmt.executeUpdate("INSERT INTO parking_spot (floor, spot_number, size) VALUES (1, 'A1', 'SMALL')");
                stmt.executeUpdate("INSERT INTO parking_spot (floor, spot_number, size) VALUES (1, 'A2', 'MEDIUM')");
                stmt.executeUpdate("INSERT INTO parking_spot (floor, spot_number, size) VALUES (1, 'A3', 'LARGE')");
                stmt.executeUpdate("INSERT INTO parking_spot (floor, spot_number, size) VALUES (1, 'A4', 'SMALL')");
            }
        }
    }
}

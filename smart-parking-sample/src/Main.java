import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    // Entry point
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        String useDb = System.getenv().getOrDefault("USE_DB", "false");
        Repository repo;
        if ("true".equalsIgnoreCase(useDb)) {
            String jdbcUrl = System.getenv().getOrDefault("JDBC_URL", "");
            String jdbcUser = System.getenv().getOrDefault("JDBC_USER", "");
            String jdbcPass = System.getenv().getOrDefault("JDBC_PASS", "");
            if (jdbcUrl.isEmpty()) {
                System.err.println("USE_DB=true but JDBC_URL not set. Falling back to in-memory.");
                repo = new InMemoryRepository();
            } else {
                repo = new DbRepository(jdbcUrl, jdbcUser, jdbcPass);
            }
        } else {
            repo = new InMemoryRepository();
        }
        AllocationService allocator = new AllocationService(repo);
        FeeCalculator feeCalculator = new FeeCalculator();

        server.createContext("/entry", new EntryHandler(allocator));
        server.createContext("/exit", new ExitHandler(repo, feeCalculator));
        server.createContext("/availability", new AvailabilityHandler(repo));
        server.createContext("/health", new HealthHandler(repo, "USE_DB="+useDb, System.getenv().getOrDefault("JDBC_URL","")));

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Smart parking sample running on http://localhost:" + port);
    }

    // ----- HTTP Handlers -----
    static class EntryHandler implements HttpHandler {
        private final AllocationService allocator;

        EntryHandler(AllocationService allocator) { this.allocator = allocator; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }
            Map<String,String> q = Utils.queryToMap(exchange.getRequestURI());
            String plate = q.get("plate");
            String type = q.getOrDefault("type","CAR").toUpperCase();
            String floorStr = q.get("preferredFloor");
            Integer preferredFloor = floorStr!=null?Integer.valueOf(floorStr):null;

            if (plate==null || plate.isBlank()) {
                send(exchange,400,"Missing plate parameter");
                return;
            }

            VehicleType vt;
            try { vt = VehicleType.valueOf(type); }
            catch (Exception e) { send(exchange,400,"Invalid vehicle type"); return; }

            Optional<ParkingSession> sess = allocator.allocate(plate, vt, preferredFloor);
            if (sess.isEmpty()) {
                send(exchange,200,"{\"status\":\"NO_SPOT\"}");
                return;
            }
            ParkingSession s = sess.get();
            String body = String.format("{\"sessionId\":%d,\"spotId\":%d,\"floor\":%d,\"allocatedAt\":\"%s\"}",
                    s.id, s.spotId, s.floor, s.entryTime.toString());
            send(exchange,200,body);
        }
    }

    static class ExitHandler implements HttpHandler {
        private final Repository repo;
        private final FeeCalculator feeCalculator;
        ExitHandler(Repository repo, FeeCalculator feeCalculator) { this.repo = repo; this.feeCalculator = feeCalculator; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange,405,"Method Not Allowed"); return; }
            Map<String,String> q = Utils.queryToMap(exchange.getRequestURI());
            String sid = q.get("sessionId");
            if (sid==null) { send(exchange,400,"Missing sessionId"); return; }
            long sessionId;
            try { sessionId = Long.parseLong(sid); } catch (Exception e) { send(exchange,400,"Invalid sessionId"); return; }

            Optional<ParkingSession> closed = repo.closeSession(sessionId);
            if (closed.isEmpty()) { send(exchange,404,"Session not found"); return; }
            ParkingSession s = closed.get();
            long durationMinutes = Duration.between(s.entryTime, s.exitTime).toMinutes();
            long feeCents = feeCalculator.calculateFeeMinutes(s.vehicleType, durationMinutes);
            s.feeCents = feeCents;

            String body = String.format("{\"sessionId\":%d,\"durationMinutes\":%d,\"feeCents\":%d,\"closedAt\":\"%s\"}",
                    s.id, durationMinutes, feeCents, s.exitTime.toString());
            send(exchange,200,body);
        }
    }

    static class AvailabilityHandler implements HttpHandler {
        private final Repository repo;
        AvailabilityHandler(Repository repo) { this.repo = repo; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange,405,"Method Not Allowed"); return; }
            Map<Integer, Map<SpotSize, Long>> avail = repo.getAvailabilityByFloorAndSize();
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean firstFloor=true;
            for (Map.Entry<Integer, Map<SpotSize, Long>> fe : avail.entrySet()) {
                if (!firstFloor) sb.append(','); firstFloor=false;
                sb.append('"').append(fe.getKey()).append('"').append(":" );
                sb.append('{');
                boolean first=true;
                for (Map.Entry<SpotSize, Long> e : fe.getValue().entrySet()) {
                    if (!first) sb.append(','); first=false;
                    sb.append('"').append(e.getKey()).append('"').append(':').append(e.getValue());
                }
                sb.append('}');
            }
            sb.append('}');
            send(exchange,200,sb.toString());
        }
    }

    static class HealthHandler implements HttpHandler {
        private final Repository repo;
        private final String mode;
        private final String jdbcUrl;
        HealthHandler(Repository repo, String mode, String jdbcUrl) { this.repo = repo; this.mode = mode; this.jdbcUrl = jdbcUrl; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange,405,"Method Not Allowed"); return; }
            boolean dbOk = true;
            try {
                // Try a light-weight repository operation to verify DB connectivity
                repo.getAvailabilityByFloorAndSize();
            } catch (RuntimeException e) {
                dbOk = false;
            }
            String maskedUrl = jdbcUrl==null?"":(jdbcUrl.length()>40?jdbcUrl.substring(0,40)+"...":jdbcUrl);
            String body = String.format("{\"mode\":\"%s\",\"dbConnected\":%b,\"jdbcUrl\":\"%s\"}", mode, dbOk, maskedUrl);
            send(exchange, 200, body);
        }
    }

    // ----- Simple domain & services -----
    enum VehicleType { MOTORCYCLE, CAR, BUS }
    enum SpotSize { MOTORCYCLE, COMPACT, REGULAR, LARGE }
    enum SpotStatus { AVAILABLE, OCCUPIED }

    // Repository abstraction so we can swap in DB-backed implementation
    interface Repository {
        Collection<ParkingSpot> allSpots();
        Optional<ParkingSpot> reserveSpot(long spotId);
        void releaseSpot(long spotId);
        ParkingSession createSession(String plate, VehicleType vt, ParkingSpot spot);
        Optional<ParkingSession> closeSession(long sessionId);
        Map<Integer, Map<SpotSize, Long>> getAvailabilityByFloorAndSize();
    }

    static class ParkingSpot {
        final long id; final int floor; final SpotSize size; volatile SpotStatus status;
        ParkingSpot(long id, int floor, SpotSize size) { this.id = id; this.floor = floor; this.size = size; this.status = SpotStatus.AVAILABLE; }
    }

    static class ParkingSession {
        final long id; final String plate; final VehicleType vehicleType; final long spotId; final int floor; final Instant entryTime;
        volatile Instant exitTime; volatile Long feeCents;
        ParkingSession(long id, String plate, VehicleType vehicleType, long spotId, int floor, Instant entryTime) {
            this.id = id; this.plate = plate; this.vehicleType = vehicleType; this.spotId = spotId; this.floor = floor; this.entryTime = entryTime; }
    }

    // A tiny in-memory repository holding spots and sessions (thread-safe)
    static class InMemoryRepository implements Repository {
        private final ConcurrentMap<Long, ParkingSpot> spots = new ConcurrentHashMap<>();
        private final ConcurrentMap<Long, ParkingSession> sessions = new ConcurrentHashMap<>();
        private final AtomicLong spotIdGen = new AtomicLong(1);
        private final AtomicLong sessIdGen = new AtomicLong(1);

        InMemoryRepository() {
            // initialize small layout: floors 1..3
            for (int f=1; f<=3; f++) {
                // add spots per floor: 5 motorcycle, 10 compact, 10 regular, 2 large
                addSpots(f, SpotSize.MOTORCYCLE,5);
                addSpots(f, SpotSize.COMPACT,10);
                addSpots(f, SpotSize.REGULAR,10);
                addSpots(f, SpotSize.LARGE,2);
            }
        }

        private void addSpots(int floor, SpotSize size, int count) {
            for (int i=0;i<count;i++) {
                long id = spotIdGen.getAndIncrement();
                spots.put(id, new ParkingSpot(id,floor,size));
            }
        }

        public Collection<ParkingSpot> allSpots() { return spots.values(); }

        public Optional<ParkingSpot> reserveSpot(long spotId) {
            ParkingSpot s = spots.get(spotId);
            if (s==null) return Optional.empty();
            synchronized (s) {
                if (s.status == SpotStatus.AVAILABLE) { s.status = SpotStatus.OCCUPIED; return Optional.of(s); }
                else return Optional.empty();
            }
        }

        public void releaseSpot(long spotId) {
            ParkingSpot s = spots.get(spotId);
            if (s!=null) { synchronized (s) { s.status = SpotStatus.AVAILABLE; } }
        }

        public ParkingSession createSession(String plate, VehicleType vt, ParkingSpot spot) {
            long id = sessIdGen.getAndIncrement();
            ParkingSession p = new ParkingSession(id, plate, vt, spot.id, spot.floor, Instant.now());
            sessions.put(id,p);
            return p;
        }

        public Optional<ParkingSession> closeSession(long sessionId) {
            ParkingSession s = sessions.get(sessionId);
            if (s==null) return Optional.empty();
            synchronized (s) {
                if (s.exitTime!=null) return Optional.of(s);
                s.exitTime = Instant.now();
                // free spot
                releaseSpot(s.spotId);
                return Optional.of(s);
            }
        }

        public Map<Integer, Map<SpotSize, Long>> getAvailabilityByFloorAndSize() {
            Map<Integer, Map<SpotSize, Long>> out = new TreeMap<>();
            for (ParkingSpot s: spots.values()) {
                out.computeIfAbsent(s.floor, k->new EnumMap<>(SpotSize.class));
                Map<SpotSize, Long> m = out.get(s.floor);
                m.putIfAbsent(s.size, 0L);
                if (s.status==SpotStatus.AVAILABLE) m.put(s.size, m.get(s.size)+1);
            }
            // ensure all sizes present
            for (Map<SpotSize, Long> m : out.values()) {
                for (SpotSize ss : SpotSize.values()) m.putIfAbsent(ss, 0L);
            }
            return out;
        }
    }

    // Simple JDBC-backed repository (Postgres/H2 compatible). Requires migration run.
    static class DbRepository implements Repository {
        private final String jdbcUrl;
        private final String user;
        private final String pass;

        DbRepository(String jdbcUrl, String user, String pass) {
            this.jdbcUrl = jdbcUrl; this.user = user; this.pass = pass;
        }

        private Connection conn() throws SQLException {
            if (user==null || user.isEmpty()) return DriverManager.getConnection(jdbcUrl);
            return DriverManager.getConnection(jdbcUrl, user, pass);
        }

        public Collection<ParkingSpot> allSpots() {
            List<ParkingSpot> out = new ArrayList<>();
            try (Connection c = conn(); PreparedStatement ps = c.prepareStatement("SELECT id,floor,size,status FROM parking_spot")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong(1); int floor = rs.getInt(2);
                        SpotSize size = SpotSize.valueOf(rs.getString(3));
                        SpotStatus status = SpotStatus.valueOf(rs.getString(4));
                        ParkingSpot p = new ParkingSpot(id,floor,size); p.status = status; out.add(p);
                    }
                }
            } catch (Exception e) { throw new RuntimeException(e); }
            return out;
        }

        public Optional<ParkingSpot> reserveSpot(long spotId) {
            try (Connection c = conn()) {
                c.setAutoCommit(false);
                try (PreparedStatement sel = c.prepareStatement("SELECT id,floor,size,status FROM parking_spot WHERE id = ? FOR UPDATE")) {
                    sel.setLong(1, spotId);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return Optional.empty(); }
                        String status = rs.getString("status");
                        if (!"AVAILABLE".equals(status)) { c.rollback(); return Optional.empty(); }
                            try (PreparedStatement upd = c.prepareStatement("UPDATE parking_spot SET status='OCCUPIED', updated_at = CURRENT_TIMESTAMP, version = version+1 WHERE id = ?")) {
                            upd.setLong(1, spotId); int u = upd.executeUpdate();
                            if (u==1) {
                                c.commit();
                                long id = rs.getLong("id"); int floor = rs.getInt("floor"); SpotSize size = SpotSize.valueOf(rs.getString("size"));
                                ParkingSpot p = new ParkingSpot(id,floor,size); p.status = SpotStatus.OCCUPIED; return Optional.of(p);
                            } else { c.rollback(); return Optional.empty(); }
                        }
                    }
                }
            } catch (SQLException e) { throw new RuntimeException(e); }
        }

        public void releaseSpot(long spotId) {
            try (Connection c = conn(); PreparedStatement ps = c.prepareStatement("UPDATE parking_spot SET status='AVAILABLE', updated_at=CURRENT_TIMESTAMP, version=version+1 WHERE id = ?")) {
                ps.setLong(1, spotId); ps.executeUpdate();
            } catch (SQLException e) { throw new RuntimeException(e); }
        }

        public ParkingSession createSession(String plate, VehicleType vt, ParkingSpot spot) {
            try (Connection c = conn()) {
                c.setAutoCommit(false);
                long vehicleId = -1;
                try (PreparedStatement sel = c.prepareStatement("SELECT id FROM vehicle WHERE plate_number = ?")) {
                    sel.setString(1, plate);
                    try (ResultSet rs = sel.executeQuery()) { if (rs.next()) vehicleId = rs.getLong(1); }
                }
                if (vehicleId==-1) {
                    try (PreparedStatement ins = c.prepareStatement("INSERT INTO vehicle (plate_number,type,created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS)) {
                        ins.setString(1, plate); ins.setString(2, vt.name()); ins.executeUpdate();
                        try (ResultSet rk = ins.getGeneratedKeys()) { if (rk.next()) vehicleId = rk.getLong(1); }
                    }
                }

                try (PreparedStatement ps = c.prepareStatement("INSERT INTO parking_session (vehicle_id, spot_id, entry_time, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'ACTIVE')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, vehicleId); ps.setLong(2, spot.id); ps.executeUpdate();
                    long sessId = -1; try (ResultSet rk = ps.getGeneratedKeys()) { if (rk.next()) sessId = rk.getLong(1); }
                    c.commit(); return new ParkingSession(sessId, plate, vt, spot.id, spot.floor, Instant.now());
                }
            } catch (SQLException e) { throw new RuntimeException(e); }
        }

        public Optional<ParkingSession> closeSession(long sessionId) {
            try (Connection c = conn()) {
                c.setAutoCommit(false);
                try (PreparedStatement sel = c.prepareStatement("SELECT id, vehicle_id, spot_id, entry_time, exit_time, status FROM parking_session WHERE id = ? FOR UPDATE")) {
                    sel.setLong(1, sessionId);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return Optional.empty(); }
                        Timestamp entryTs = rs.getTimestamp("entry_time");
                        Timestamp exitTs = rs.getTimestamp("exit_time");
                        long spotId = rs.getLong("spot_id");
                        if (exitTs!=null) { c.rollback(); ParkingSession s = new ParkingSession(sessionId, "", VehicleType.CAR, spotId, 0, entryTs.toInstant()); s.exitTime = exitTs.toInstant(); return Optional.of(s); }
                        try (PreparedStatement upd = c.prepareStatement("UPDATE parking_session SET exit_time = CURRENT_TIMESTAMP, status='CLOSED' WHERE id = ?")) {
                            upd.setLong(1, sessionId); upd.executeUpdate();
                        }
                        try (PreparedStatement rel = c.prepareStatement("UPDATE parking_spot SET status='AVAILABLE', updated_at = CURRENT_TIMESTAMP, version = version+1 WHERE id = ?")) { rel.setLong(1, spotId); rel.executeUpdate(); }
                        c.commit(); ParkingSession s = new ParkingSession(sessionId, "", VehicleType.CAR, spotId, 0, entryTs.toInstant()); s.exitTime = Instant.now(); return Optional.of(s);
                    }
                }
            } catch (SQLException e) { throw new RuntimeException(e); }
        }

        public Map<Integer, Map<SpotSize, Long>> getAvailabilityByFloorAndSize() {
            try {
                Map<Integer, Map<SpotSize, Long>> out = new TreeMap<>();
                String sql = "SELECT floor, size, count(*) FROM parking_spot WHERE status = 'AVAILABLE' GROUP BY floor, size ORDER BY floor";
                try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int floor = rs.getInt(1); SpotSize size = SpotSize.valueOf(rs.getString(2)); long cnt = rs.getLong(3);
                            out.computeIfAbsent(floor, k->new EnumMap<>(SpotSize.class)); out.get(floor).put(size, cnt);
                        }
                    }
                }
                for (Map<SpotSize, Long> m : out.values()) for (SpotSize ss : SpotSize.values()) m.putIfAbsent(ss, 0L);
                return out;
            } catch (SQLException e) { throw new RuntimeException(e); }
        }
    }

    static class AllocationService {
        private final Repository repo;

        AllocationService(Repository repo) { this.repo = repo; }

        // Returns an allocated session if successful
        Optional<ParkingSession> allocate(String plate, VehicleType vt, Integer preferredFloor) {
            List<Integer> floors = new ArrayList<>();
            if (preferredFloor!=null) { floors.add(preferredFloor); }
            // then other floors
            for (int f=1; f<=3; f++) if (preferredFloor==null || f!=preferredFloor) floors.add(f);

            List<SpotSize> eligible = eligibleSizesFor(vt);

            for (int floor: floors) {
                for (SpotSize sz : eligible) {
                    Optional<ParkingSpot> cand = findAndReserve(sz, floor);
                    if (cand.isPresent()) {
                        ParkingSession sess = repo.createSession(plate, vt, cand.get());
                        return Optional.of(sess);
                    }
                }
            }
            return Optional.empty();
        }

        private Optional<ParkingSpot> findAndReserve(SpotSize size, int floor) {
            // naive scan; in production use Redis priority sets + DB locking
            for (ParkingSpot s: repo.allSpots()) {
                if (s.floor==floor && s.size==size && s.status==SpotStatus.AVAILABLE) {
                    if (repo.reserveSpot(s.id).isPresent()) return Optional.of(s);
                }
            }
            return Optional.empty();
        }

        private List<SpotSize> eligibleSizesFor(VehicleType vt) {
            switch (vt) {
                case MOTORCYCLE: return Arrays.asList(SpotSize.MOTORCYCLE, SpotSize.COMPACT, SpotSize.REGULAR, SpotSize.LARGE);
                case CAR: return Arrays.asList(SpotSize.COMPACT, SpotSize.REGULAR, SpotSize.LARGE);
                case BUS: return Arrays.asList(SpotSize.LARGE);
                default: return Arrays.asList(SpotSize.REGULAR);
            }
        }
    }

    static class FeeCalculator {
        // Simple policy: grace 15 min, per-hour rounding up
        private final Map<VehicleType, Long> ratePerHourCents = new EnumMap<>(VehicleType.class);
        private final long graceMinutes = 15;

        FeeCalculator() {
            ratePerHourCents.put(VehicleType.MOTORCYCLE, 100L); // $1.00/hr
            ratePerHourCents.put(VehicleType.CAR, 300L); // $3.00/hr
            ratePerHourCents.put(VehicleType.BUS, 500L); // $5.00/hr
        }

        long calculateFeeMinutes(VehicleType vt, long durationMinutes) {
            if (durationMinutes <= graceMinutes) return 0L;
            long hours = (durationMinutes + 59) / 60; // ceil
            return hours * ratePerHourCents.getOrDefault(vt, 300L);
        }
    }

    // ----- Utilities -----
    static class Utils {
        static Map<String,String> queryToMap(URI uri) {
            Map<String,String> map = new HashMap<>();
            String q = uri.getRawQuery();
            if (q==null) return map;
            for (String pair : q.split("&")) {
                int idx = pair.indexOf('=');
                try {
                    if (idx>0) {
                        String k = java.net.URLDecoder.decode(pair.substring(0,idx), "UTF-8");
                        String v = java.net.URLDecoder.decode(pair.substring(idx+1), "UTF-8");
                        map.put(k,v);
                    } else {
                        map.put(java.net.URLDecoder.decode(pair, "UTF-8"), "");
                    }
                } catch (Exception ignored) {}
            }
            return map;
        }
    }

    static void send(HttpExchange exchange, int code, String body) throws IOException {
        byte[] b = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, b.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(b); }
    }
}

// CAR RENTAL & REPAIR MANAGEMENT SYSTEM
// All Java code is in this ONE file. Java 17+ recommended.
// Search for "CONCEPT" to find each syllabus topic quickly during your viva.
// Frontend: index.html + style.css. Database: HSQLDB using JDBC (jar included in lib/).

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CarRentalRepairSystem {
    // CONCEPT 01: final keyword + static variable/constant
    private static final int PORT = Integer.getInteger("port", 8080);
    public static final String PROJECT_NAME = "Car Rental & Repair System";
    private static final Database DB = new Database();

    // CONCEPT 02: multithreading (HTTP requests use a pool of worker threads)
    private static final ExecutorService WORKERS = Executors.newFixedThreadPool(4);
    private static final ScheduledExecutorService BACKGROUND = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws Exception {
        DB.initialize();
        System.out.println(PROJECT_NAME);

        // CONCEPT 03: objects + different inheritance examples in action
        User admin = new Admin(1, "Demo Admin", "0000");
        Mechanic mechanic = new Mechanic(2, "Demo Mechanic", "0000", "Engine");
        System.out.println("Roles: " + admin.getRole() + " / " + mechanic.getRole());
        System.out.println("Using final method: " + admin.identity());
        System.out.println("Hybrid inheritance demo: " + mechanic.description());

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/", CarRentalRepairSystem::handleRequest);
        server.setExecutor(WORKERS); // Every request can be handled by a different thread.
        server.start();

        // CONCEPT 04: multithreading (a second thread runs periodically)
        BACKGROUND.scheduleAtFixedRate(() -> {
            try {
                FileLogger.write("Background check: " + DB.count("cars") + " cars in database");
            } catch (Exception error) {
                System.err.println("Background check: " + error.getMessage());
            }
        }, 30, 60, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BACKGROUND.shutdownNow();
            WORKERS.shutdownNow();
            DB.shutdown(); // Flush the file-based JDBC database when the server closes.
            System.out.println("\nServer stopped. Data saved.");
        }));

        System.out.println("Open http://localhost:" + PORT + " in your browser");
        System.out.println("Press Ctrl+C here to stop the application.");
        FileLogger.write("Server started");
    }

    // CONCEPT 05: REST API using Java's built-in HTTP server (GET + POST)
    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
            } else if (path.equals("/") || path.equals("/index.html")) {
                sendFile(exchange, "index.html", "text/html; charset=utf-8");
            } else if (path.equals("/style.css")) {
                sendFile(exchange, "style.css", "text/css; charset=utf-8");
            } else {
                send(exchange, 404, "text/plain; charset=utf-8", "Page not found");
            }
        // CONCEPT 06: exception handling (try, catch, custom exception)
        } catch (InputException error) {
            send(exchange, 400, "application/json; charset=utf-8", "{\"error\":" + quote(error.getMessage()) + "}");
        } catch (Exception error) {
            System.err.println("Request error: " + error);
            send(exchange, 500, "application/json; charset=utf-8", "{\"error\":\"Server or database error. Check terminal.\"}");
        }
    }

    private static void handleApi(HttpExchange exchange, String path) throws Exception {
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            String result;
            switch (path) {
                case "/api/dashboard": result = DB.dashboardJson(); break;
                case "/api/cars": result = DB.carsJson(); break;
                case "/api/customers": result = DB.customersJson(); break;
                case "/api/rentals": result = DB.rentalsJson(); break;
                case "/api/repairs": result = DB.repairsJson(); break;
                default: send(exchange, 404, "application/json; charset=utf-8", "{\"error\":\"Unknown endpoint\"}"); return;
            }
            send(exchange, 200, "application/json; charset=utf-8", result);
            return;
        }

        if (!"POST".equals(method)) {
            send(exchange, 405, "application/json; charset=utf-8", "{\"error\":\"Use GET or POST\"}");
            return;
        }

        // HTML forms send application/x-www-form-urlencoded: no JSON library needed.
        Map<String, String> form = parseForm(exchange);
        String message;
        switch (path) {
            case "/api/customers":
                DB.addCustomer(required(form, "name"), required(form, "phone"));
                message = "Customer added";
                break;
            case "/api/cars":
                DB.addCar(required(form, "registration"), required(form, "brand"),
                        required(form, "model"), positiveMoney(form, "price"));
                message = "Car added";
                break;
            case "/api/rentals":
                DB.rentCar(positiveId(form, "customerId"), positiveId(form, "carId"),
                        positiveId(form, "days"));
                message = "Car rented successfully";
                break;
            case "/api/rentals/return":
                DB.returnCar(positiveId(form, "rentalId"));
                message = "Car returned successfully";
                break;
            case "/api/repairs":
                DB.addRepair(positiveId(form, "carId"), required(form, "problem"),
                        required(form, "mechanic"), positiveMoney(form, "cost"));
                message = "Repair job created";
                break;
            case "/api/repairs/complete":
                DB.completeRepair(positiveId(form, "repairId"));
                message = "Repair marked complete";
                break;
            default:
                send(exchange, 404, "application/json; charset=utf-8", "{\"error\":\"Unknown endpoint\"}");
                return;
        }
        FileLogger.write(message);
        send(exchange, 200, "application/json; charset=utf-8", "{\"message\":" + quote(message) + "}");
    }

    private static void sendFile(HttpExchange exchange, String filename, String contentType) throws IOException {
        // CONCEPT 07: file handling - read HTML/CSS from local files
        String content = Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        send(exchange, 200, contentType, content);
    }

    private static void send(HttpExchange exchange, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(code, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readNBytes(8193);
        if (body.length > 8192) throw new InputException("Form is too large");
        // CONCEPT 08: Java Collections - HashMap stores key/value pairs from a form.
        Map<String, String> fields = new HashMap<>();
        for (String item : new String(body, StandardCharsets.UTF_8).split("&")) {
            if (item.isEmpty()) continue;
            String[] pair = item.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String val = URLDecoder.decode(pair.length > 1 ? pair[1] : "", StandardCharsets.UTF_8);
            fields.put(key, val);
        }
        return fields;
    }

    private static String required(Map<String, String> fields, String key) {
        // CONCEPT 09: String functions - trim(), isEmpty(), length()
        String value = fields.getOrDefault(key, "").trim();
        if (value.isEmpty() || value.length() > 100) throw new InputException("Enter a valid " + key + " (max 100 characters)");
        return value;
    }

    private static int positiveId(Map<String, String> fields, String key) {
        try {
            int value = Integer.parseInt(required(fields, key));
            if (value <= 0 || value > 3650) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new InputException("Enter a valid positive number for " + key + " (1 to 3650)");
        }
    }

    private static double positiveMoney(Map<String, String> fields, String key) {
        try {
            double value = Double.parseDouble(required(fields, key));
            if (!Double.isFinite(value) || value < 0 || value > 10000000) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new InputException("Enter a valid price/cost for " + key);
        }
    }

    // Simple JSON escaping without a third-party library.
    static String quote(String text) {
        if (text == null) return "null";
        StringBuilder result = new StringBuilder("\""); // CONCEPT: StringBuilder
        for (char ch : text.toCharArray()) {
            if (ch == '"' || ch == '\\') result.append('\\').append(ch);
            else if (ch == '\n') result.append("\\n");
            else if (ch == '\r') result.append("\\r");
            else if (ch == '\t') result.append("\\t");
            else if (ch < 32) result.append(' ');
            else result.append(ch);
        }
        return result.append('"').toString();
    }

    static String money(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
}

// CONCEPT 10: abstract class + abstract method
abstract class User {
    private final int userId;   // CONCEPT 11: private (encapsulation)
    private final String name;
    private final String phone;

    protected User(int userId, String name, String phone) { // constructor
        this.userId = userId;
        this.name = name;
        this.phone = phone;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public abstract String getRole(); // ABSTRACT METHOD; subclasses must implement it
    public final String identity() { return getRole() + " #" + userId; } // FINAL METHOD: cannot override
}

// CONCEPT 12: single inheritance (Customer extends User)
class Customer extends User {
    public Customer(int id, String name, String phone) {
        super(id, name, phone); // CONCEPT 13: super keyword calls parent constructor
    }

    @Override // CONCEPT 14: method overriding
    public String getRole() { return "CUSTOMER"; }

    public String toJson() {
        return "{\"id\":" + getUserId() + ",\"name\":" + CarRentalRepairSystem.quote(getName()) +
                ",\"phone\":" + CarRentalRepairSystem.quote(getPhone()) + "}";
    }
}

class Staff extends User {
    public Staff(int id, String name, String phone) { super(id, name, phone); }
    @Override public String getRole() { return "STAFF"; }
}

// CONCEPT 15: multilevel inheritance (User -> Staff -> Mechanic)
// CONCEPT 16a: hybrid-style inheritance: parent class + interface
class Mechanic extends Staff implements Describable {
    private final String specialization;
    public Mechanic(int id, String name, String phone, String specialization) {
        super(id, name, phone); // super calls Staff constructor
        this.specialization = specialization;
    }
    @Override public String getRole() { return "MECHANIC (" + specialization + ")"; }
    @Override public String description() { return "Specializes in " + specialization + " repairs"; }
}

// CONCEPT 16: hierarchical inheritance (Staff -> Admin AND Staff -> Mechanic)
class Admin extends Staff {
    public Admin(int id, String name, String phone) { super(id, name, phone); }
    @Override public String getRole() { return "ADMIN"; }
}

// CONCEPT 17: interface - Rental and Repair both promise calculateBill().
interface Billable {
    double calculateBill();
}

// Second interface: Java supports implementing more than one interface.
interface Describable {
    String description();
}

class Car {
    // CONCEPT 18: static variable shared by all objects
    public static int objectsCreated = 0;
    private final int id;
    private final String registration;
    private final String brand;
    private final String model;
    private final double rate;
    private final String status;

    public Car(int id, String registration, String brand, String model, double rate, String status) {
        this.id = id; this.registration = registration; this.brand = brand;
        this.model = model; this.rate = rate; this.status = status;
        objectsCreated++;
    }

    public String toJson() {
        return "{\"id\":" + id + ",\"registration\":" + CarRentalRepairSystem.quote(registration)
                + ",\"brand\":" + CarRentalRepairSystem.quote(brand)
                + ",\"model\":" + CarRentalRepairSystem.quote(model)
                + ",\"rate\":" + CarRentalRepairSystem.money(rate)
                + ",\"status\":" + CarRentalRepairSystem.quote(status) + "}";
    }
}

// CONCEPT 19: method overloading - same method name, different parameters.
class PriceCalculator {
    public static double total(double pricePerDay, int days) {
        return pricePerDay * days;
    }
    public static double total(double pricePerDay, int days, double discountPercent) {
        return pricePerDay * days * (1 - discountPercent / 100.0);
    }
}

class Rental implements Billable, Describable { // CONCEPT 20: two interfaces (multiple-interface inheritance)
    private final double dailyPrice;
    private final int days;
    public Rental(double dailyPrice, int days) { this.dailyPrice = dailyPrice; this.days = days; }
    @Override public String description() { return days + "-day car rental"; }

    @Override // interface method overriding / polymorphism
    public double calculateBill() {
        // Both versions of the overloaded method are called in real application flow.
        if (days >= 7) return PriceCalculator.total(dailyPrice, days, 10); // 10% weekly discount
        return PriceCalculator.total(dailyPrice, days);
    }
}

class Repair implements Billable { // Another implementation of the same interface
    private final double estimatedCost;
    public Repair(double estimatedCost) { this.estimatedCost = estimatedCost; }
    @Override public double calculateBill() { return estimatedCost; }
}

// CONCEPT 21: custom exception + exception handling
class InputException extends RuntimeException {
    public InputException(String message) { super(message); }
}

class FileLogger {
    // CONCEPT 22: file handling - write/append lines to activity.log
    public static synchronized void write(String message) {
        try {
            String safeMessage = message.replace('\r', ' ').replace('\n', ' ');
            Files.writeString(Path.of("activity.log"), java.time.LocalDateTime.now() + " - "
                    + safeMessage + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException error) {
            System.err.println("Log file error: " + error.getMessage());
        }
    }
}

// CONCEPT 23: JDBC - Connection, Statement, PreparedStatement, ResultSet.
// HSQLDB stores a local database in data/ ; no internet or DB server is needed.
class Database {
    private static final String URL = "jdbc:hsqldb:file:data/car_rental";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    public void initialize() throws Exception {
        Files.createDirectories(Path.of("data"));
        Class.forName("org.hsqldb.jdbcDriver"); // Load the supplied JDBC driver.
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            createIfMissing(connection, statement, "CUSTOMERS", "CREATE TABLE customers (" +
                    "id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, phone VARCHAR(30) NOT NULL)");
            createIfMissing(connection, statement, "CARS", "CREATE TABLE cars (" +
                    "id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, " +
                    "registration VARCHAR(40) NOT NULL, brand VARCHAR(100) NOT NULL, " +
                    "model VARCHAR(100) NOT NULL, rate DOUBLE NOT NULL, status VARCHAR(20) NOT NULL, CONSTRAINT car_reg_unique UNIQUE(registration))");
            createIfMissing(connection, statement, "RENTALS", "CREATE TABLE rentals (" +
                    "id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, customer_id INTEGER NOT NULL, " +
                    "car_id INTEGER NOT NULL, days INTEGER NOT NULL, total DOUBLE NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, FOREIGN KEY (customer_id) REFERENCES customers(id), " +
                    "FOREIGN KEY (car_id) REFERENCES cars(id))");
            createIfMissing(connection, statement, "REPAIRS", "CREATE TABLE repairs (" +
                    "id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, car_id INTEGER NOT NULL, " +
                    "problem VARCHAR(100) NOT NULL, mechanic VARCHAR(100) NOT NULL, " +
                    "cost DOUBLE NOT NULL, status VARCHAR(20) NOT NULL, " +
                    "FOREIGN KEY (car_id) REFERENCES cars(id))");
        }
        if (count("cars") == 0) {
            addCar("MH02AB1234", "Maruti Suzuki", "Swift", 1500);
            addCar("MH01CD5678", "Hyundai", "i20", 1800);
            addCar("MH12EF9012", "Tata", "Nexon", 2300);
        }
        if (count("customers") == 0) {
            addCustomer("Demo Customer", "9999900000");
        }
        // Build Java Car objects to demonstrate classes, objects, and static variable.
        carsJson();
        System.out.println("Static Car.objectsCreated = " + Car.objectsCreated);
    }

    private void createIfMissing(Connection connection, Statement statement, String table, String ddl) throws SQLException {
        try (ResultSet tables = connection.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            if (!tables.next()) statement.executeUpdate(ddl);
        }
    }

    public int count(String table) throws SQLException {
        // Table names here are ONLY hard-coded program constants, never user-provided.
        if (!List.of("cars", "customers", "rentals", "repairs").contains(table)) throw new SQLException("Invalid table");
        try (Connection connection = connect(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void addCustomer(String name, String phone) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO customers (name, phone) VALUES (?, ?)") ) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.executeUpdate();
        }
    }

    public void addCar(String registration, String brand, String model, double rate) throws SQLException {
        // CONCEPT: more String functions (replace(), toUpperCase()).
        String cleanReg = registration.replace(" ", "").toUpperCase(Locale.ROOT);
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO cars (registration, brand, model, rate, status) VALUES (?, ?, ?, ?, ?)") ) {
            ps.setString(1, cleanReg);
            ps.setString(2, brand);
            ps.setString(3, model);
            ps.setDouble(4, rate);
            ps.setString(5, "AVAILABLE");
            ps.executeUpdate();
        } catch (SQLException error) {
            if (error.getMessage().toLowerCase(Locale.ROOT).contains("unique")) {
                throw new InputException("Registration number already exists");
            }
            throw error;
        }
    }

    // CONCEPT: interface polymorphism - Billable variable holds a Rental object.
    public void rentCar(int customerId, int carId, int days) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false); // CONCEPT: JDBC transaction
            try {
                if (!exists(connection, "customers", customerId)) throw new InputException("Customer not found");
                double rate;
                try (PreparedStatement ps = connection.prepareStatement("SELECT rate FROM cars WHERE id=? AND status='AVAILABLE'")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InputException("Choose an available car");
                        rate = rs.getDouble("rate");
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement("UPDATE cars SET status='RENTED' WHERE id=? AND status='AVAILABLE'")) {
                    ps.setInt(1, carId);
                    if (ps.executeUpdate() != 1) throw new InputException("Car is already booked");
                }
                Rental rental = new Rental(rate, days);
                Billable bill = rental; // one object can be used through its interface type
                FileLogger.write("Creating " + rental.description());
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO rentals (customer_id, car_id, days, total, status) VALUES (?, ?, ?, ?, 'ACTIVE')")) {
                    ps.setInt(1, customerId); ps.setInt(2, carId);
                    ps.setInt(3, days); ps.setDouble(4, bill.calculateBill());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    public void returnCar(int rentalId) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                int carId = getCarId(connection, "rentals", rentalId, "ACTIVE", "Rental is not active");
                try (PreparedStatement ps = connection.prepareStatement("UPDATE rentals SET status='RETURNED' WHERE id=?")) {
                    ps.setInt(1, rentalId); ps.executeUpdate();
                }
                changeCarStatus(connection, carId, "AVAILABLE");
                connection.commit();
            } catch (SQLException | RuntimeException error) { connection.rollback(); throw error; }
        }
    }

    public void addRepair(int carId, String problem, String mechanic, double cost) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE cars SET status='REPAIR' WHERE id=? AND status='AVAILABLE'")) {
                    ps.setInt(1, carId);
                    if (ps.executeUpdate() != 1) throw new InputException("Only an available car can go for repair");
                }
                // An interface reference can also hold a Repair object.
                Billable bill = new Repair(cost);
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO repairs (car_id, problem, mechanic, cost, status) VALUES (?, ?, ?, ?, 'OPEN')")) {
                    ps.setInt(1, carId); ps.setString(2, problem);
                    ps.setString(3, mechanic); ps.setDouble(4, bill.calculateBill());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException error) { connection.rollback(); throw error; }
        }
    }

    public void completeRepair(int repairId) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                int carId = getCarId(connection, "repairs", repairId, "OPEN", "Repair is not open");
                try (PreparedStatement ps = connection.prepareStatement("UPDATE repairs SET status='DONE' WHERE id=?")) {
                    ps.setInt(1, repairId); ps.executeUpdate();
                }
                changeCarStatus(connection, carId, "AVAILABLE");
                connection.commit();
            } catch (SQLException | RuntimeException error) { connection.rollback(); throw error; }
        }
    }

    private boolean exists(Connection connection, String table, int id) throws SQLException {
        // table is an internal literal: "customers" only.
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM " + table + " WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private int getCarId(Connection connection, String table, int id, String status, String problem) throws SQLException {
        // Only called with hard-coded "rentals" or "repairs".
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT car_id FROM " + table + " WHERE id=? AND status=?")) {
            ps.setInt(1, id); ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new InputException(problem);
                return rs.getInt(1);
            }
        }
    }

    private void changeCarStatus(Connection connection, int id, String status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE cars SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        }
    }

    // CONCEPT 24: Java Collections - ArrayList stores Car objects read from JDBC.
    public String carsJson() throws SQLException {
        List<Car> cars = new ArrayList<>();
        try (Connection connection = connect(); Statement ps = connection.createStatement();
             ResultSet rs = ps.executeQuery("SELECT * FROM cars ORDER BY id")) {
            while (rs.next()) {
                cars.add(new Car(rs.getInt("id"), rs.getString("registration"),
                        rs.getString("brand"), rs.getString("model"),
                        rs.getDouble("rate"), rs.getString("status")));
            }
        }
        List<String> output = new ArrayList<>();
        for (Car car : cars) output.add(car.toJson());
        return "[" + String.join(",", output) + "]";
    }

    public String customersJson() throws SQLException {
        List<String> output = new ArrayList<>();
        try (Connection connection = connect(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM customers ORDER BY id")) {
            while (rs.next()) {
                Customer customer = new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("phone"));
                output.add(customer.toJson());
            }
        }
        return "[" + String.join(",", output) + "]";
    }

    public String rentalsJson() throws SQLException {
        List<String> output = new ArrayList<>();
        String sql = "SELECT r.id, r.days, r.total, r.status, c.name, a.registration " +
                "FROM rentals r JOIN customers c ON r.customer_id=c.id " +
                "JOIN cars a ON r.car_id=a.id ORDER BY r.id DESC";
        try (Connection connection = connect(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) output.add("{\"id\":" + rs.getInt("id")
                    + ",\"customer\":" + CarRentalRepairSystem.quote(rs.getString("name"))
                    + ",\"car\":" + CarRentalRepairSystem.quote(rs.getString("registration"))
                    + ",\"days\":" + rs.getInt("days")
                    + ",\"total\":" + CarRentalRepairSystem.money(rs.getDouble("total"))
                    + ",\"status\":" + CarRentalRepairSystem.quote(rs.getString("status")) + "}");
        }
        return "[" + String.join(",", output) + "]";
    }

    public String repairsJson() throws SQLException {
        List<String> output = new ArrayList<>();
        String sql = "SELECT r.id, r.problem, r.mechanic, r.cost, r.status, a.registration " +
                "FROM repairs r JOIN cars a ON r.car_id=a.id ORDER BY r.id DESC";
        try (Connection connection = connect(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) output.add("{\"id\":" + rs.getInt("id")
                    + ",\"car\":" + CarRentalRepairSystem.quote(rs.getString("registration"))
                    + ",\"problem\":" + CarRentalRepairSystem.quote(rs.getString("problem"))
                    + ",\"mechanic\":" + CarRentalRepairSystem.quote(rs.getString("mechanic"))
                    + ",\"cost\":" + CarRentalRepairSystem.money(rs.getDouble("cost"))
                    + ",\"status\":" + CarRentalRepairSystem.quote(rs.getString("status")) + "}");
        }
        return "[" + String.join(",", output) + "]";
    }

    public String dashboardJson() throws SQLException {
        // CONCEPT 25: Java Collections - HashMap stores dashboard counts.
        Map<String, Integer> counts = new HashMap<>();
        try (Connection connection = connect(); Statement st = connection.createStatement()) {
            counts.put("cars", queryCount(st, "SELECT COUNT(*) FROM cars"));
            counts.put("available", queryCount(st, "SELECT COUNT(*) FROM cars WHERE status='AVAILABLE'"));
            counts.put("activeRentals", queryCount(st, "SELECT COUNT(*) FROM rentals WHERE status='ACTIVE'"));
            counts.put("openRepairs", queryCount(st, "SELECT COUNT(*) FROM repairs WHERE status='OPEN'"));
            counts.put("customers", queryCount(st, "SELECT COUNT(*) FROM customers"));
        }
        return "{\"cars\":" + counts.get("cars") + ",\"available\":" + counts.get("available")
                + ",\"activeRentals\":" + counts.get("activeRentals") + ",\"openRepairs\":"
                + counts.get("openRepairs") + ",\"customers\":" + counts.get("customers") + "}";
    }

    private int queryCount(Statement st, String sql) throws SQLException {
        try (ResultSet rs = st.executeQuery(sql)) { rs.next(); return rs.getInt(1); }
    }

    public void shutdown() {
        try (Connection connection = connect(); Statement st = connection.createStatement()) {
            st.execute("SHUTDOWN");
        } catch (SQLException error) {
            System.err.println("Database shutdown: " + error.getMessage());
        }
    }
}

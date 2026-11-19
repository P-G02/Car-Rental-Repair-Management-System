# Viva guide — 20 Java concepts in this one source file

Open `CarRentalRepairSystem.java` and press **Ctrl+F** for each label `CONCEPT`.

1. **Class:** Blueprint for data and methods. `Car`, `Customer`, `Rental`, `Repair` are classes.
2. **Object:** An instance of a class. `new Admin(...)` and `new Car(...)` create objects.
3. **Static variable:** One variable belongs to the class, shared by its objects. `Car.objectsCreated` counts constructed Java Car objects (not database rows).
4. **Private:** Restricts direct access to fields outside their class. `User.name` is private, read via `getName()`.
5. **Inheritance:** Reuse fields/methods from a parent. `Customer extends User` is single; `User → Staff → Mechanic` is multilevel; `Staff → Admin` and `Staff → Mechanic` is hierarchical.
6. **Types of inheritance:** Single, multilevel, hierarchical are shown. Java does **not** support inheriting multiple classes; `Rental implements Billable, Describable` implements two interfaces. `Mechanic extends Staff implements Describable` combines class inheritance and an interface (hybrid-style).
7. **Method overloading:** Same name with different parameter lists: `PriceCalculator.total(rate, days)` vs `PriceCalculator.total(rate, days, discountPercent)`.
8. **Method overriding:** A child supplies a new implementation of a parent/interface method. See `@Override getRole()`.
9. **Abstract method:** Defined without method body in an abstract class: `public abstract String getRole();`.
10. **Interface:** A contract. `Billable` declares `calculateBill()`, implemented by both `Rental` and `Repair`.
11. **Java Collections:** `ArrayList<Car>` temporarily stores rows read from JDBC; `HashMap` stores request fields and dashboard counts.
12. **String:** `trim`, `length`, `isEmpty`, `replace`, `toUpperCase`, `String.join`, `StringBuilder`.
13. **Exception handling:** `try`/`catch` converts bad form values into HTTP 400; `InputException` is a custom exception; SQL transactions roll back on failure.
14. **Multithreading:** `Executors.newFixedThreadPool(4)` allows concurrent HTTP requests. `newSingleThreadScheduledExecutor()` logs counts regularly.
15. **File handling:** `Files.readString()` serves HTML/CSS. `Files.writeString()` appends `activity.log` entries.
16. **JDBC:** Java Database Connectivity. `DriverManager.getConnection()` opens the DB; `PreparedStatement` safely passes parameters; `ResultSet` reads rows.
17. **REST API:** Browser uses GET requests to read data and POST requests to perform actions. Java's `HttpServer` implements endpoints like `/api/cars`.
18. **Final:** A `final` variable can't be reassigned; a `final` method (`User.identity()`) can't be overridden.
19. **Super:** `super(...)` invokes the parent constructor to initialize inherited fields.
20. **Polymorphism:** `Billable bill = new Rental(rate, days)` invokes Rental's `calculateBill()`; `Billable bill = new Repair(cost)` invokes Repair's version.

## Quick live demonstration (2–3 minutes)

1. Run `run.bat`. Explain that Java starts the web server and connects to HSQLDB using JDBC.
2. Open `localhost:8080`. Show the demo vehicles and customer.
3. Book a car for 2 days. Explain `Rental`, `Billable`, and the two-argument `total()`.
4. Return the car, then rent the same car for 7 days. Show the discount using the three-argument overloaded `total()`.
5. Return it and create a repair. Show that the car's status switches to `REPAIR`; complete the job to make it `AVAILABLE`.
6. Open `activity.log` to demonstrate file writing, then search the Java file for `CONCEPT 10` (abstract), `CONCEPT 19` (overloading), and `CONCEPT 23` (JDBC).

## Viva questions with short answers

**Q: Where is the backend?** All Java code is inside `CarRentalRepairSystem.java`.

**Q: Why is there a JAR in lib?** JDBC defines an API, but we need a database driver implementation; HSQLDB provides it.

**Q: What's the difference between overloading and overriding?** Overloading: same name, different parameters, often same class. Overriding: subclass changes an inherited method's behavior.

**Q: Why do we use an interface?** Both Rental and Repair can calculate a bill, even though they are different classes.

**Q: Why use private fields?** To control access using constructors and getters (encapsulation).

**Q: Is this the full attached EER?** No. It's a simplified runnable mini project based on the main Customer–Car–Rental–Repair relationships.

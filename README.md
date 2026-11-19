# Car Rental & Repair Management System (Java OOP Mini Project)

**Simple frontend:** `index.html` + `style.css` (JavaScript is inside `index.html`)

**Entire Java backend:** `CarRentalRepairSystem.java` — this is the **only `.java` source file**. All classes and methods are commented with `CONCEPT` labels for an easy practical demonstration.

## Run on Windows (easy)

1. Install **Java JDK 17 or newer**. Test in PowerShell: `java -version` and `javac -version`.
2. Extract **the entire ZIP**, including its `lib` folder, into one folder.
3. Double-click **`run.bat`**, or open PowerShell in the project folder and run `.\run.bat`.
4. Open **http://localhost:8080** in your browser.
5. Add a customer or use the demo customer; add cars or use the 3 demo cars. Try renting, returning, scheduling a repair, and completing a repair.
6. Press **Ctrl+C** inside the terminal window to stop the server and save the database.

You do **not** need MySQL, Maven, npm, Python, or internet access. The JDBC driver (`lib/hsqldb-1.8.0.10.jar`) is included.

## Run on Linux / macOS

```sh
./run.sh
```

Or compile/run manually:

**Windows PowerShell:**
```powershell
javac -encoding UTF-8 -cp "lib/*" CarRentalRepairSystem.java
java -cp ".;lib/*" CarRentalRepairSystem
```

**macOS / Linux:**
```sh
javac -encoding UTF-8 -cp 'lib/*' CarRentalRepairSystem.java
java -cp '.:lib/*' CarRentalRepairSystem
```

If port 8080 is already being used, stop the other program or change `PORT` at the top of the Java file and the sample address in the HTML header. Open your new localhost port.

## Features to demonstrate

- **Car module:** Add vehicles; view available, rented and under-repair cars.
- **Customer module:** Add a customer.
- **Rental module:** Rent an available car, calculate cost, return it. **7+ days = 10% discount**.
- **Repair module:** Assign a mechanic name and cost to an available car; complete the job.
- **JDBC:** Save/read data in local HSQLDB tables: `customers`, `cars`, `rentals`, `repairs`.
- **REST API:** Java HTTP server uses GET and POST; the frontend calls these APIs via `fetch`.
- **File handling:** Writes `activity.log` and reads HTML/CSS; database files are in `data/`.
- **Multithreading:** 4 HTTP worker threads and 1 periodic background logger thread.

The application creates its local database and example records automatically on first run. Your own data is saved between runs. To reset, stop the Java server, then delete the generated **`data/` folder** and `activity.log`. Restart to recreate the examples.

## Source-code locations (search for the marker)

| Java syllabus concept | Where to show the examiner |
|---|---|
| Classes and objects | `class Car`, `class Customer`, `class Rental`, `new Admin(...)`, `new Car(...)` |
| Static variable | `Car.objectsCreated` |
| Private variables | `User` fields `private final`, `Car` fields |
| Single inheritance | `class Customer extends User` |
| Multilevel inheritance | `User -> Staff -> Mechanic` |
| Hierarchical inheritance | `Staff -> Admin` and `Staff -> Mechanic` |
| Multiple inheritance alternative | Java doesn't allow multiple class inheritance; `Rental implements Billable, Describable` demonstrates two interfaces, and `Mechanic extends Staff implements Describable` shows a hybrid-style combination |
| Method overloading | Two `PriceCalculator.total(...)` methods, with different parameters |
| Method overriding | `@Override getRole()` in Customer/Admin/Mechanic, `calculateBill()` in Rental/Repair |
| Abstract method | `abstract String getRole()` in abstract `User` |
| Interface | `interface Billable` and its Rental/Repair implementations |
| Java Collections | `List`, `ArrayList`, `Map`, `HashMap`, `List.of()` |
| String methods | `trim()`, `isEmpty()`, `length()`, `replace()`, `toUpperCase()`, `StringBuilder` |
| Exception handling | `try/catch` in `handleRequest`, `InputException`, DB `rollback()` |
| Multithreading | `WORKERS`, `BACKGROUND.scheduleAtFixedRate(...)` |
| File handling | `FileLogger.write()`, `Files.readString()` |
| JDBC | `Database.connect()`, `PreparedStatement`, `ResultSet`, transactions |
| REST API | `handleApi()` GET/POST, frontend `fetch()` |
| `final` keyword | `PORT`, `PROJECT_NAME`, `User.identity()` |
| `super` keyword | `Customer`, `Staff`, `Mechanic` constructors |

Full concept guide: **`CONCEPT_GUIDE.md`**.

## API examples

| HTTP method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/dashboard` | Live statistics |
| GET | `/api/cars` | All cars |
| GET | `/api/customers` | All customers |
| GET | `/api/rentals` | Rental bookings |
| GET | `/api/repairs` | Repair jobs |
| POST | `/api/customers` | Create customer (`name`, `phone`) |
| POST | `/api/cars` | Add car (`registration`, `brand`, `model`, `price`) |
| POST | `/api/rentals` | Book car (`customerId`, `carId`, `days`) |
| POST | `/api/rentals/return` | Return car (`rentalId`) |
| POST | `/api/repairs` | Create job (`carId`, `problem`, `mechanic`, `cost`) |
| POST | `/api/repairs/complete` | Close job (`repairId`) |

POST requests use `application/x-www-form-urlencoded` forms. For classroom simplicity, this is a **local educational demo**: it has no login, role-based authorization, or production security hardening. Do not expose it to the public internet.

## Relationship to your attached EER diagram

A copy of your uploaded diagram is at `docs/Reference_EER_Diagram.pdf`. The diagram includes a **larger proposed system** with Payment, Invoice, Car Inspection, Spare Part and other detailed fields. This **small teaching demonstration deliberately implements the four central database tables only** (Customer, Car, Rental, Repair) and shows Admin/Mechanic via Java class inheritance or a repair mechanic name. It is **not** a complete implementation of every field/entity in that reference EER.

## JDBC driver licensing

Included `lib/hsqldb-1.8.0.10.jar` is an HSQLDB JDBC driver. Its redistribution license and notices are in `lib/THIRD_PARTY_LICENSE.txt`.

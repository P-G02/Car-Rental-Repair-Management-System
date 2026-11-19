# 🚗 Car Rental & Repair Management System

### Java OOP Mini Project

A Java-based web application designed to simplify car rental operations and vehicle repair management through an integrated platform.

The project demonstrates the practical implementation of **Object-Oriented Programming, JDBC, HSQLDB, REST APIs, Multithreading, Exception Handling and File Handling**.

---

## 📌 Project Overview

The Car Rental & Repair Management System provides a centralized platform to manage vehicles, customers, rental bookings and repair operations.

It allows users to manage vehicle availability, calculate rental charges, process vehicle returns and track repair activities through a simple web interface.

**Main Objective:** To apply Core Java and Object-Oriented Programming concepts to develop a practical, database-driven management application.

---

## ✨ Key Features

### 🚘 1. Car Management
- Add new vehicles with registration number, brand, model and rental price.
- View vehicle records.
- Monitor vehicle availability.
- Track available, rented and under-repair vehicles.

### 👥 2. Customer Management
- Register new customers.
- Store customer details.
- View registered customer records.

### 📅 3. Rental Management
- Book available vehicles for customers.
- Specify rental duration.
- Automatically calculate rental charges.
- Return rented vehicles.
- Automatically apply a **10% discount for rentals of 7 or more days**.

### 🔧 4. Repair Management
- Register vehicle repair requests.
- Record problem descriptions.
- Assign mechanic names.
- Store repair costs.
- Mark repair jobs as completed.
- Automatically update vehicle availability after repair completion.

### 📊 5. Dashboard
- View overall system statistics.
- Monitor cars, customers, rentals and repairs.
- Get a consolidated overview of system activities.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Backend development |
| HTML5 | Frontend structure |
| CSS3 | User interface styling |
| JavaScript | Frontend interactions |
| HSQLDB | Database management |
| JDBC | Database connectivity |
| Java HttpServer | Local HTTP server |
| REST API | Frontend-backend communication |

---

## 🧠 Core Java Concepts Implemented

One of the main objectives of this project is to demonstrate Core Java concepts through practical implementation.

| Concept | Implementation |
|---|---|
| Classes and Objects | Car, Customer, Rental, Repair, User |
| Static Variables | Car object counter |
| Encapsulation | Private fields and getter methods |
| Inheritance | Single, multilevel and hierarchical inheritance |
| Method Overloading | PriceCalculator.total() |
| Method Overriding | getRole() and calculateBill() |
| Abstraction | Abstract User class |
| Interfaces | Billable and Describable |
| Polymorphism | Common Billable interface for Rental and Repair |
| Java Collections | ArrayList, HashMap, List and Map |
| String Handling | trim(), length(), replace(), StringBuilder |
| Exception Handling | try-catch and custom InputException |
| Multithreading | ExecutorService and scheduled background tasks |
| File Handling | Reading HTML/CSS and writing activity.log |
| JDBC | Connection, PreparedStatement and ResultSet |
| REST API | HTTP GET and POST requests |
| final Keyword | Final variables and methods |
| super Keyword | Parent constructor invocation |

**Additional documentation:** Refer to `CONCEPT_GUIDE.md` for a detailed explanation of all 20 Java concepts and their locations in the source code.

---

## 🗄️ Database Design

The application uses HSQLDB with JDBC connectivity.

Four main database tables are implemented:

| Table | Description |
|---|---|
| customers | Stores customer information |
| cars | Stores vehicle information and availability status |
| rentals | Stores rental bookings and transaction details |
| repairs | Stores repair records, mechanic information and costs |

### Database Workflow

1. A customer books an available vehicle.
2. A rental record is created.
3. The vehicle status changes to RENTED.
4. On returning the vehicle, its status changes to AVAILABLE.
5. If a repair is registered, the vehicle status changes to REPAIR.
6. Once the repair is completed, the vehicle becomes AVAILABLE again.

The database is initialized automatically when the application runs for the first time. Records are persisted locally between executions.

---

## 🔗 REST API Endpoints

The Java backend exposes HTTP endpoints for frontend communication.

### GET Requests

| Endpoint | Functionality |
|---|---|
| `/api/dashboard` | Retrieve dashboard statistics |
| `/api/cars` | Retrieve all vehicles |
| `/api/customers` | Retrieve customer records |
| `/api/rentals` | Retrieve rental records |
| `/api/repairs` | Retrieve repair records |

### POST Requests

| Endpoint | Functionality |
|---|---|
| `/api/customers` | Register a customer |
| `/api/cars` | Add a vehicle |
| `/api/rentals` | Book a vehicle |
| `/api/rentals/return` | Return a vehicle |
| `/api/repairs` | Register a repair |
| `/api/repairs/complete` | Complete a repair |

The frontend communicates with the Java backend using JavaScript Fetch API requests.

---

## 📂 Project Structure

```text
Car-Rental-Repair-Management-System/
│
├── CarRentalRepairSystem.java
│
├── index.html
├── style.css
│
├── run.bat
├── run.sh
│
├── README.md
├── CONCEPT_GUIDE.md
│
├── lib/
│   ├── hsqldb-1.8.0.10.jar
│   └── THIRD_PARTY_LICENSE.txt
│
├── docs/
│   └── Reference_EER_Diagram.pdf
│
├── data/              (Generated at runtime)
└── activity.log       (Generated at runtime)
```

---

## ⚙️ Installation & Setup

### Prerequisites

- Java JDK 17 or newer.
- Git (optional, for cloning the repository).
- A modern web browser.

No separate MySQL installation, Maven, npm or Python installation is required.

The HSQLDB JDBC driver is already included in the `lib` directory.

### Step 1: Clone the Repository

```bash
git clone https://github.com/P-G02/Car-Rental-Repair-Management-System.git
```

### Step 2: Navigate to the Project Folder

```bash
cd Car-Rental-Repair-Management-System
```

### Step 3: Run the Application

**Windows:**

```powershell
.\run.bat
```

**Linux / macOS:**

```bash
chmod +x run.sh
./run.sh
```

### Step 4: Open the Application

Visit the following address in your browser:

```text
http://localhost:8080
```

The application initializes the database and sample records automatically on its first run.

### Step 5: Stop the Application

Press `Ctrl + C` in the terminal running the Java server.

---

## 📸 Demonstration Workflow

The following workflow can be used to demonstrate the application:

1. Open the dashboard and view system statistics.
2. Add a new customer.
3. Add a new vehicle.
4. Book the vehicle for a customer.
5. Demonstrate automatic rental bill calculation.
6. Return the vehicle and verify its availability.
7. Register a repair request.
8. Complete the repair and verify the updated vehicle status.
9. Inspect the activity log.

---

## 🎓 Academic Relevance

This project was developed as a Core Java / Object-Oriented Programming mini project.

It focuses on applying theoretical programming concepts to a practical application while demonstrating:

- Object-oriented software design.
- Database connectivity and persistence.
- Client-server communication.
- Concurrent request handling.
- Exception handling and validation.
- File operations and logging.

---

## ⚠️ Project Scope

This is a local educational demonstration project.

- It does not currently implement login or role-based authentication.
- The implemented database focuses on four central entities: Customer, Car, Rental and Repair.
- The accompanying reference EER diagram represents a larger proposed system and is not fully implemented.
- The application is intended for local demonstration, not public production deployment.

---

## 👩‍💻 Developed As

**Java OOP Mini Project**

**Project:** Car Rental & Repair Management System

**Primary Language:** Java

**Database:** HSQLDB

**Backend Connectivity:** JDBC

---

⭐ If you find this project interesting, feel free to explore the source code and its concept guide.
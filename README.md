# UrusSewa – Professional Car Rental & Fleet Management System

UrusSewa is a Java Swing-based desktop application designed for streamlined car rental and fleet management. It features modern animations, real-time live search, an asynchronous vehicle preview sidebar, PDF invoice generation, double-booking prevention, and automatic future scheduling handlers.

---

## 📂 Project Architecture

```text
urus-sewa/
├── compile.sh            # MacOS/Linux compilation script
├── run.sh                # MacOS/Linux execution script
├── car_inventory.txt     # Car Fleet text database
├── rental_history.txt    # Booking history text database
├── invoices/             # PDF Invoices directory (auto-generated)
├── docs/                 # Project documentation & rubrics
└── src/                  # Java Source Root
    └── carrental/        # Main namespace (ensures no default package issues)
        ├── CarRentalApp.java  # Main Application Entry Point
        ├── model/             # OOP Data Models (Encapsulation, Inheritance)
        │   ├── Car.java
        │   ├── StandardCar.java
        │   ├── ElectricCar.java
        │   ├── HybridCar.java
        │   └── Rental.java
        ├── service/           # File System I/O & Core Services
        │   ├── FileService.java
        │   ├── InventoryService.java
        │   └── InvoiceService.java
        └── ui/                # Swing GUI Components & Event Listeners
            ├── UIUtils.java
            ├── MainWindow.java
            ├── CarTablePanel.java
            ├── CarFormDialog.java
            ├── RentalDialog.java
            ├── RentalHistoryDialog.java
            └── DatePickerDialog.java
```

---

## ❓ Why Use a Package Structure (`src/carrental/...`)?

It is **highly recommended and standard** to put classes in a named package structure (like `carrental`) rather than directly under `src` (the "default package") because:

1. **Import Restrictions**: Java classes in the default package cannot be imported by classes inside named packages. By putting everything under `carrental`, components can import and communicate cleanly across sub-packages (e.g. `carrental.ui` importing models from `carrental.model`).
2. **Modularity & Encapsulation**: It reflects professional production-level software design, grouping related logic together (Models, Services, and UI Views).
3. **Namespacing**: It avoids potential class-naming collisions with standard Java libraries or third-party packages.

---

## 🚀 How to Run the Program

Follow the instructions matching your Operating System. Make sure you have **Java Development Kit (JDK 8 or higher)** installed.

### 🍎 MacOS & 🐧 Linux

We have provided convenient executable shell scripts to automate compilation and running:

1. **Compile the program:**
   ```bash
   chmod +x compile.sh run.sh
   ./compile.sh
   ```
2. **Run the program:**
   ```bash
   ./run.sh
   ```

_(Alternatively, you can compile manually: `javac -d out src/carrental/CarRentalApp.java src/carrental/model/_.java src/carrental/service/_.java src/carrental/ui/_.java`and run:`java -cp out carrental.CarRentalApp`)\*

---

### 🪟 Windows

You can compile and run directly using Command Prompt or PowerShell:

#### **Via Command Prompt (`cmd`)**

1. **Compile the program:**
   ```cmd
   mkdir out
   javac -d out -sourcepath src src\carrental\CarRentalApp.java
   ```
2. **Run the program:**
   ```cmd
   java -cp out carrental.CarRentalApp
   ```

#### **Via PowerShell**

1. **Compile the program:**
   ```powershell
   New-Item -ItemType Directory -Force -Path out
   javac -d out -sourcepath src src/carrental/CarRentalApp.java
   ```
2. **Run the program:**
   ```powershell
   java -cp out carrental.CarRentalApp
   ```

---

## 🔑 Admin Credentials

- **Username:** `admin`
- **Password:** `admin123`

_(Alternatively, click **"Continue as Guest"** to access the read-only portal!)_

## 🧭 Quick Code Walkthrough (for students)

This section explains where to look in the code when you want to understand how each feature works.

- **Main entry:** [src/carrental/CarRentalApp.java](src/carrental/CarRentalApp.java) — sets Look&Feel and opens the login screen.
- **Models (data classes):** [src/carrental/model](src/carrental/model) — contains `Car`, `StandardCar`, `ElectricCar`, `HybridCar`, and `Rental`.
  - `Car` is an abstract base class (common fields and abstract methods).
  - Subclasses implement `calculateRentalCost(...)`, `getCarType()` and `toFileString()`.
- **Services (logic & I/O):** [src/carrental/service](src/carrental/service)
  - `FileService` handles reading/writing `car_inventory.txt` and `rental_history.txt`.
  - `InventoryService` contains the in-memory lists and business rules (search, id generation, availability, overlap checks).
  - `InvoiceService` generates simple PDF invoices without external libraries.
- **UI (Swing views):** [src/carrental/ui](src/carrental/ui)
  - `LoginDialog` → `MainWindow` → `CarTablePanel` (main table + actions).
  - `CarFormDialog` for Add/Edit, `RentalDialog` for booking, and `RentalHistoryDialog` for reports.

Tips for studying the code:

- Start in `CarRentalApp` then `LoginDialog` to see how the app boots and switches roles.
- Open `InventoryService` and `FileService` to follow data from disk → memory → UI.
- Trace a rental: `CarTablePanel.handleRent()` → `RentalDialog.handleConfirm()` → `InventoryService` updates → `FileService.saveRentals()`.

If you'd like, I can also generate a single annotated PDF or inline comments for each class. Which would you prefer?

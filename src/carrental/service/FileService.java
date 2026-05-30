package carrental.service;

import carrental.model.Car;
import carrental.model.ElectricCar;
import carrental.model.HybridCar;
import carrental.model.Rental;
import carrental.model.StandardCar;
import java.io.*;
import java.util.ArrayList;

/**
 * Handles all text file input/output for the UrusSewa system.
 *
 * Separating file operations into this dedicated class follows the
 * Single Responsibility Principle – the UI and service layers do not
 * need to know how data is persisted.
 *
 * Files used:
 *   - car_inventory.txt  : stored in the project root directory
 *   - rental_history.txt : stored in the project root directory
 *
 * File format (car_inventory.txt):
 *   STANDARD,P001,Perodua,Myvi,120.0,true
 *   ELECTRIC,E001,Tesla,Model 3,450.0,true,75
 *   HYBRID,H001,Toyota,Camry Hybrid,280.0,true,18
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class FileService {

    /** Path to the car inventory file resolved to the project root directory. */
    public static final String CAR_FILE    = resolvePath("car_inventory.txt");

    /** Path to the rental history file resolved to the project root directory. */
    public static final String RENTAL_FILE = resolvePath("rental_history.txt");

    /**
     * Dynamically finds the project root directory (which contains the 'src' folder
     * or compile scripts) and returns the absolute path for the target database file.
     * This guarantees that whether the program is run from the project root or deep
     * inside the package subfolders like 'src/carrental/', it will ALWAYS read and
     * write to the exact same central database files in the project root directory.
     *
     * @param fileName the database filename to resolve
     * @return the absolute path to the file resolved in the project root
     */
    public static String resolvePath(String fileName) {
        String userDir = System.getProperty("user.dir");
        java.io.File file = new java.io.File(userDir);
        
        // Walk upwards from the current working directory until we find
        // the project root (heuristic: contains a `src` folder or a compile script).
        // This makes file paths stable whether the app is launched from
        // the project root or from a nested IDE run configuration.
        while (file != null && file.exists()) {
            java.io.File srcDir = new java.io.File(file, "src");
            java.io.File compileScript = new java.io.File(file, "compile.sh");
            if (srcDir.exists() || compileScript.exists()) {
                return new java.io.File(file, fileName).getAbsolutePath();
            }
            file = file.getParentFile();
        }
        return fileName; // Fallback to relative path
    }

    // =========================================================
    // Car Inventory – Load
    // =========================================================

    /**
     * Reads the car inventory from file.
     * Uses BufferedReader for efficient line-by-line text reading.
     *
     * @return ArrayList of Car objects; empty list if file not found
     */
    public synchronized ArrayList<Car> loadInventory() {
        return loadInventoryFromFile(new File(CAR_FILE));
    }

    /**
     * Reads car inventory from a specified file (useful for importing).
     *
     * @param file the File object to read from
     * @return ArrayList of Car objects
     */
    public synchronized ArrayList<Car> loadInventoryFromFile(File file) {
        ArrayList<Car> list = new ArrayList<>();

        // If the file does not exist yet, return an empty list
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines
                Car car = parseCarLine(line);
                if (car != null) list.add(car);
            }
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
        return list;
    }

    /**
     * Parses a single CSV line into a Car object.
     * Supports three types: STANDARD (Petrol), ELECTRIC, HYBRID.
     *
     * @param line raw CSV line from file
     * @return StandardCar, ElectricCar, or HybridCar; null if malformed
     */
    private Car parseCarLine(String line) {
        String[] t = line.split(",");
        if (t.length < 6) return null;

        try {
            String type    = t[0];
            String id      = t[1];
            String brand   = t[2];
            String model   = t[3];
            double price   = Double.parseDouble(t[4]);
            boolean avail  = Boolean.parseBoolean(t[5]);

            Car car;
            // Expected CSV variations by type (examples):
            // STANDARD,id,brand,model,price,true,fuelCapacity[,imageUrl]
            // ELECTRIC,id,brand,model,price,true,batteryCapacity[,imageUrl]
            // HYBRID,id,brand,model,price,true,batteryCapacity,fuelCapacity[,imageUrl]

            if ("ELECTRIC".equalsIgnoreCase(type) && t.length >= 7) {
                int battery = Integer.parseInt(t[6]);
                car = new ElectricCar(id, brand, model, price, avail, battery);
                if (t.length >= 8) car.setImageUrl(t[7]);
            } else if ("HYBRID".equalsIgnoreCase(type) && t.length >= 7) {
                int battery = Integer.parseInt(t[6]);
                int fuel = (t.length >= 8) ? Integer.parseInt(t[7]) : 40;
                car = new HybridCar(id, brand, model, price, avail, battery, fuel);
                if (t.length >= 9) car.setImageUrl(t[8]);
            } else {
                int fuel = (t.length >= 7) ? Integer.parseInt(t[6]) : 40;
                car = new StandardCar(id, brand, model, price, avail, fuel);
                if (t.length >= 8) car.setImageUrl(t[7]);
            }
            return car;
        } catch (NumberFormatException e) {
            System.err.println("Skipping malformed car record: " + line);
            return null;
        }
    }

    // =========================================================
    // Car Inventory – Save
    // =========================================================

    /**
     * Writes the full car inventory to file, overwriting existing content.
     * Uses BufferedWriter for efficient text output.
     *
     * @param inventory the current list of Car objects to persist
     */
    public synchronized void saveInventory(ArrayList<Car> inventory) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CAR_FILE))) {
            for (Car car : inventory) {
                bw.write(car.toFileString()); // Polymorphism: each subclass formats itself
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    // =========================================================
    // Rental History – Load
    // =========================================================

    /**
     * Reads the rental history from file.
     *
     * @return ArrayList of Rental objects; empty list if file not found
     */
    public synchronized ArrayList<Rental> loadRentals() {
        ArrayList<Rental> list = new ArrayList<>();
        File file = new File(RENTAL_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Rental r = Rental.fromFileString(line);
                if (r != null) list.add(r);
            }
        } catch (IOException e) {
            System.err.println("Error loading rentals: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // Rental History – Save
    // =========================================================

    /**
     * Writes the full rental history to file.
     *
     * @param rentals the list of Rental objects to persist
     */
    public synchronized void saveRentals(ArrayList<Rental> rentals) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RENTAL_FILE))) {
            for (Rental r : rentals) {
                bw.write(r.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving rentals: " + e.getMessage());
        }
    }

    // =========================================================
    // Default Data
    // =========================================================

    /**
     * Returns a preset inventory used when no data file exists yet.
     * Includes StandardCar, ElectricCar, and HybridCar objects to
     * demonstrate the full inheritance hierarchy from the first run.
     *
     * ID prefixes: P = Petrol, E = Electric, H = Hybrid
     *
     * @return ArrayList with 6 default Car objects
     */
    public ArrayList<Car> getDefaultInventory() {
        ArrayList<Car> defaults = new ArrayList<>();
        defaults.add(new StandardCar("P001", "Perodua", "Myvi",         120.0, true, 36));
        defaults.add(new StandardCar("P002", "Proton",  "Saga",         100.0, true, 40));
        defaults.add(new StandardCar("P003", "Honda",   "Civic",        250.0, true, 47));
        defaults.add(new ElectricCar("E001", "Tesla",   "Model 3",      450.0, true, 75));
        defaults.add(new ElectricCar("E002", "BYD",     "Atto 3",       300.0, true, 60));
        defaults.add(new HybridCar(  "H001", "Toyota",  "Camry Hybrid", 280.0, true, 18, 50));
        return defaults;
    }
}

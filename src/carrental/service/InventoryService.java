package carrental.service;

import carrental.model.Car;
import carrental.model.Rental;
import java.util.ArrayList;
import java.time.LocalDate;

/**
 * Service class that manages the car inventory and rental history.
 *
 * Uses ArrayList – a dynamic data structure from java.util – to store
 * Car and Rental objects in memory during the program session.
 *
 * Advantages of ArrayList over arrays:
 *   1. Dynamic size – grows/shrinks automatically; no fixed length needed.
 *   2. Built-in methods – add(), remove(), size(), get() reduce boilerplate.
 *   3. Works with generics – ArrayList&lt;Car&gt; enforces type safety at compile time.
 *
 * OOP Concepts Demonstrated:
 *   - Encapsulation: ArrayLists are private; external code calls methods.
 *   - Polymorphism: ArrayList&lt;Car&gt; stores both StandardCar and ElectricCar.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class InventoryService {

    /** Dynamic list of all cars in the fleet. */
    private ArrayList<Car> carInventory;

    /** Dynamic list of all completed rental transactions. */
    private ArrayList<Rental> rentalHistory;

    /**
     * Constructs the service and initialises empty lists.
     * Data is loaded from file by FileService before use.
     */
    public InventoryService() {
        carInventory  = new ArrayList<>();
        rentalHistory = new ArrayList<>();
    }

    // =========================================================
    // Car CRUD Operations
    // =========================================================

    /**
     * Adds a new car to the fleet inventory.
     *
     * @param car the Car object to add (StandardCar or ElectricCar)
     */
    public void addCar(Car car) {
        carInventory.add(car);
    }

    /**
     * Removes a car from the fleet by its ID.
     *
     * @param id the car ID to remove
     * @return true if found and removed; false if not found
     */
    public boolean removeCar(String id) {
        for (int i = 0; i < carInventory.size(); i++) {
            if (carInventory.get(i).getId().equals(id)) {
                carInventory.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a car by its unique ID.
     *
     * @param id the car ID to search for
     * @return the matching Car object, or null if not found
     */
    public Car findCarById(String id) {
        for (Car car : carInventory) {
            if (car.getId().equals(id)) return car;
        }
        return null;
    }

    /**
     * Searches the inventory by brand or model keyword (case-insensitive).
     *
     * @param keyword the search term (partial matches accepted)
     * @return ArrayList of matching Car objects (may be empty)
     */
    public ArrayList<Car> searchCars(String keyword) {
        String kw = keyword.toLowerCase();
        ArrayList<Car> results = new ArrayList<>();
        for (Car car : carInventory) {
            boolean matchBrand = car.getBrand().toLowerCase().contains(kw);
            boolean matchModel = car.getModel().toLowerCase().contains(kw);
            if (matchBrand || matchModel) {
                results.add(car);
            }
        }
        return results;
    }

    /**
     * Returns the full car inventory list.
     *
     * @return ArrayList of all Car objects
     */
    public ArrayList<Car> getAllCars() {
        return carInventory;
    }

    /**
     * Replaces the entire inventory list (used during file load).
     *
     * @param inventory the new full inventory
     */
    public void setInventory(ArrayList<Car> inventory) {
        this.carInventory = inventory;
    }

    // =========================================================
    // Rental Operations
    // =========================================================

    /**
     * Records a new rental transaction.
     *
     * @param rental the completed Rental object to add
     */
    public void addRental(Rental rental) {
        rentalHistory.add(rental);
    }

    /**
     * Returns all rental history records.
     *
     * @return ArrayList of Rental objects
     */
    public ArrayList<Rental> getRentalHistory() {
        return rentalHistory;
    }

    /**
     * Replaces the rental history list (used during file load).
     *
     * @param rentalHistory the loaded rental records
     */
    public void setRentalHistory(ArrayList<Rental> rentalHistory) {
        this.rentalHistory = rentalHistory;
    }

    /**
     * Generates the next sequential rental ID number.
     *
     * @return integer count based on existing rental history size
     */
    public int getNextRentalId() {
        return rentalHistory.size() + 1;
    }

    /**
     * Finds the currently active rental for a given car.
     *
     * @param carId the car ID to search for
     * @return the active Rental object, or null if none found
     */
    public Rental findActiveRentalByCarId(String carId) {
        Rental futureRental = null;
        for (Rental r : rentalHistory) {
            if (r.getCarId().equals(carId) && r.isActive()) {
                try {
                    LocalDate start = LocalDate.parse(r.getRentalDate());
                    if (!LocalDate.now().isBefore(start)) {
                        return r; // Prioritise ongoing or overdue rentals
                    }
                    futureRental = r; // Keep track of future booking as fallback
                } catch (Exception e) {
                    return r;
                }
            }
        }
        return futureRental;
    }

    /** Detects if a future booking is blocked/delayed because the current renter is overdue. */
    public Rental findBlockedFutureBooking(String carId, Rental currentOverdue) {
        if (currentOverdue == null || !currentOverdue.isOverdue()) return null;
        for (Rental r : rentalHistory) {
            if (r.getCarId().equals(carId) && (r.isActive() || r.isBooked()) && !r.getRentalId().equals(currentOverdue.getRentalId())) {
                try {
                    LocalDate start = LocalDate.parse(r.getRentalDate());
                    LocalDate currentReturn = LocalDate.parse(currentOverdue.getReturnDate());
                    // If the other booking starts on or after the expected return date of the overdue one
                    if (!start.isBefore(currentReturn)) {
                        return r;
                    }
                } catch (Exception e) {}
            }
        }
        return null;
    }

    /**
     * Returns only active (ongoing) rentals.
     *
     * @return ArrayList of Rental objects with status ACTIVE
     */
    public ArrayList<Rental> getActiveRentals() {
        ArrayList<Rental> active = new ArrayList<>();
        for (Rental r : rentalHistory) {
            if (r.isActive() || r.isBooked()) active.add(r);
        }
        return active;
    }

    /**
     * Returns only completed (past) rentals.
     *
     * @return ArrayList of Rental objects with status COMPLETED
     */
    public ArrayList<Rental> getCompletedRentals() {
        ArrayList<Rental> completed = new ArrayList<>();
        for (Rental r : rentalHistory) {
            if (Rental.STATUS_COMPLETED.equals(r.getStatus())) completed.add(r);
        }
        return completed;
    }

    // =========================================================
    // Auto ID Generation
    // =========================================================

    /**
     * Generates the next sequential car ID based on car type.
     *
     * Prefix convention:
     *   "P" → Petrol  (e.g. P001, P002, ...)
     *   "E" → Electric (e.g. E001, E002, ...)
     *   "H" → Hybrid  (e.g. H001, H002, ...)
     *
     * Scans existing IDs in the inventory to find the highest number
     * for the given prefix, then increments by 1.
     *
     * @param prefix the single-character prefix ("P", "E", or "H")
     * @return the next available car ID string (e.g. "P004")
     */
    public String generateCarId(String prefix) {
        int maxNum = 0;
        for (Car car : carInventory) {
            String id = car.getId();
            // Check if this car ID starts with the same prefix
            if (id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException e) {
                    // Skip malformed IDs
                }
            }
        }
        return prefix + String.format("%03d", maxNum + 1);
    }

    /**
     * Checks if a car is available *today*.
     * A car is available if it is statically marked as available OR
     * if its active rental starts in the future.
     */
    public boolean isCarAvailable(String carId) {
        Car car = findCarById(carId);
        if (car == null) return false;

        // If statically marked available, yes
        if (car.isAvailable()) return true;

        // If statically marked unavailable, check if the active rental starts in the future
        Rental active = findActiveRentalByCarId(carId);
        if (active != null) {
            try {
                LocalDate start = LocalDate.parse(active.getRentalDate());
                return LocalDate.now().isBefore(start);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if a proposed rent date range overlaps with any active bookings for the given car.
     *
     * @param carId      the car to check
     * @param proposedStart proposed start date
     * @param proposedEnd   proposed end date
     * @return the overlapping Rental if an overlap exists; null if the range is free
     */
    public Rental findOverlappingRental(String carId, LocalDate proposedStart, LocalDate proposedEnd) {
        for (Rental r : rentalHistory) {
            if (r.getCarId().equals(carId) && (r.isActive() || r.isBooked())) {
                try {
                    LocalDate start = LocalDate.parse(r.getRentalDate());
                    LocalDate end = start.plusDays(r.getRentalDays());

                    // Overlap condition: proposed starts before/on existing end, and proposed ends after/on existing start
                    boolean startsBeforeEnd = !proposedStart.isAfter(end);
                    boolean endsAfterStart  = !proposedEnd.isBefore(start);

                    if (startsBeforeEnd && endsAfterStart) {
                        return r; // Overlap detected!
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }
        return null;
    }

    /**
     * Scans all bookings with status "BOOKED". If the current date is on or after
     * the booking start date, automatically transitions the booking to "ACTIVE"
     * and sets the associated car's availability to false.
     * Saves changes to disk.
     */
    public boolean checkAndTransitionBookings(FileService fileService) {
        boolean changed = false;
        for (Rental r : rentalHistory) {
            if (Rental.STATUS_BOOKED.equals(r.getStatus())) {
                try {
                    LocalDate start = LocalDate.parse(r.getRentalDate());
                    if (!LocalDate.now().isBefore(start)) {
                        r.setStatus(Rental.STATUS_ACTIVE);

                        // Mark the corresponding car as rented out immediately
                        Car car = findCarById(r.getCarId());
                        if (car != null) {
                            car.setAvailable(false);
                        }
                        changed = true;
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }

        if (changed) {
            fileService.saveInventory(carInventory);
            fileService.saveRentals(rentalHistory);
        }
        return changed;
    }

    /** Checks if a car has any ongoing active rentals or upcoming future bookings. */
    public boolean hasActiveOrBookedRentals(String carId) {
        for (Rental r : rentalHistory) {
            if (r.getCarId().equals(carId) && (r.isActive() || r.isBooked())) {
                return true;
            }
        }
        return false;
    }
}

package carrental.model;

import java.time.LocalDate;

/**
 * Represents a rental transaction record with full customer information.
 *
 * Stores customer details (name, IC, phone), rental duration,
 * computed cost, date, and status (ACTIVE / COMPLETED).
 *
 * OOP Concepts Demonstrated:
 *   - Encapsulation: all fields private with public getters.
 *   - Static factory method: fromFileString() for deserialisation.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class Rental {

    /** Rental status constants. */
    public static final String STATUS_BOOKED    = "BOOKED";
    public static final String STATUS_ACTIVE    = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // --- Private fields (Encapsulation) ---
    private String rentalId;
    private String carId;
    private String customerName;
    private String customerIC;
    private String customerPhone;
    private int    rentalDays;
    private double totalCost;
    private String rentalDate;    // ISO format: YYYY-MM-DD
    private String status;        // ACTIVE or COMPLETED

    /**
     * Constructs a Rental record with full customer information.
     *
     * @param rentalId      unique rental identifier (e.g. "R001")
     * @param carId         ID of the rented car
     * @param customerName  full name of the customer
     * @param customerIC    customer IC or passport number
     * @param customerPhone customer phone number
     * @param rentalDays    number of days rented
     * @param totalCost     total charge in RM
     * @param rentalDate    date of rental (ISO format)
     * @param status        ACTIVE or COMPLETED
     */
    public Rental(String rentalId, String carId, String customerName,
                  String customerIC, String customerPhone,
                  int rentalDays, double totalCost,
                  String rentalDate, String status) {
        this.rentalId      = rentalId;
        this.carId         = carId;
        this.customerName  = customerName;
        this.customerIC    = customerIC;
        this.customerPhone = customerPhone;
        this.rentalDays    = rentalDays;
        this.totalCost     = totalCost;
        this.rentalDate    = rentalDate;
        this.status        = status;
    }

    // =========================================================
    // Getters
    // =========================================================

    public String getRentalId()      { return rentalId; }
    public String getCarId()         { return carId; }
    public String getCustomerName()  { return customerName; }
    public String getCustomerIC()    { return customerIC; }
    public String getCustomerPhone() { return customerPhone; }
    public int    getRentalDays()    { return rentalDays; }
    public double getTotalCost()     { return totalCost; }
    public String getRentalDate()    { return rentalDate; }
    public String getStatus()        { return status; }

    /** Calculates return date dynamically based on rentalDate and rentalDays. */
    public String getReturnDate() {
        try {
            LocalDate start = LocalDate.parse(rentalDate);
            return start.plusDays(rentalDays).toString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Returns true if the rental is ACTIVE and current date is past return date. */
    public boolean isOverdue() {
        if (!isActive()) return false;
        try {
            LocalDate returnDt = LocalDate.parse(getReturnDate());
            return LocalDate.now().isAfter(returnDt);
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // Setters
    // =========================================================

    public void setStatus(String status) { this.status = status; }

    /** Marks this rental as completed (car returned). */
    public void setCompleted() { this.status = STATUS_COMPLETED; }

    /** @return true if the rental is currently active */
    public boolean isActive() { return STATUS_ACTIVE.equals(status); }

    /** @return true if the rental is a future reservation */
    public boolean isBooked() { return STATUS_BOOKED.equals(status); }

    // =========================================================
    // File I/O Helpers
    // =========================================================

    /**
     * Serialises this rental record to a CSV line for file storage.
     * Format: rentalId,carId,name,ic,phone,days,cost,date,status
     *
     * @return CSV string
     */
    public String toFileString() {
        return rentalId + "," + carId + "," + customerName + ","
                + customerIC + "," + customerPhone + ","
                + rentalDays + "," + totalCost + ","
                + rentalDate + "," + status;
    }

    /**
     * Static factory: creates a Rental object from a CSV line.
     *
     * @param line a single CSV line from the rentals file
     * @return Rental object, or null if the line is malformed
     */
    public static Rental fromFileString(String line) {
        String[] p = line.split(",");
        if (p.length < 9) return null;
        try {
            return new Rental(
                p[0],                        // rentalId
                p[1],                        // carId
                p[2],                        // customerName
                p[3],                        // customerIC
                p[4],                        // customerPhone
                Integer.parseInt(p[5]),      // rentalDays
                Double.parseDouble(p[6]),    // totalCost
                p[7],                        // rentalDate
                p[8]                         // status
            );
        } catch (NumberFormatException e) {
            System.err.println("Skipping malformed rental record: " + line);
            return null;
        }
    }

    // =========================================================
    // Overridden Object Method
    // =========================================================

    @Override
    public String toString() {
        return rentalId + " | " + customerName + " (" + customerIC + ")"
                + " | Car: " + carId + " | " + rentalDays + " day(s)"
                + " | RM " + String.format("%.2f", totalCost)
                + " | " + status;
    }
}

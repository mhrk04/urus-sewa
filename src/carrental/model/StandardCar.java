package carrental.model;

/**
 * Represents a standard petrol or diesel rental car.
 *
 * OOP Concepts Demonstrated:
 *   - Inheritance: extends Car, reusing all base fields and getters/setters.
 *   - Polymorphism: overrides getCarType(), calculateRentalCost(), toFileString().
 *
 * Cost formula: pricePerDay × days (no surcharge).
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class StandardCar extends Car {

    /** Fuel tank capacity in litres. */
    private int fuelCapacity;

    /**
     * Constructs a StandardCar (Petrol).
     *
     * @param id           unique car identifier
     * @param brand        manufacturer name
     * @param model        model name
     * @param pricePerDay  base daily rental rate in RM
     * @param available    rental availability flag
     * @param fuelCapacity fuel tank capacity in litres
     */
    public StandardCar(String id, String brand, String model,
                       double pricePerDay, boolean available, int fuelCapacity) {
        super(id, brand, model, pricePerDay, available);
        this.fuelCapacity = fuelCapacity;
    }

    // =========================================================
    // Getter / Setter
    // =========================================================

    public int getFuelCapacity() { return fuelCapacity; }
    public void setFuelCapacity(int fuelCapacity) { this.fuelCapacity = fuelCapacity; }

    // =========================================================
    // Abstract Method Implementations (Polymorphism)
    // =========================================================

    /**
     * Returns the engine type label for standard cars.
     *
     * @return "Standard (Petrol/Diesel)"
     */
    @Override
    public String getCarType() {
        return "Petrol (" + fuelCapacity + "L)";
    }

    /**
     * Calculates rental cost: no surcharge applied.
     * Formula: pricePerDay × days
     *
     * @param days number of rental days
     * @return total rental cost in RM
     */
    @Override
    public double calculateRentalCost(int days) {
        return getPricePerDay() * days;
    }

    @Override
    public String getSurchargeInfo() {
        return "No Surcharge (0%)";
    }

    /**
     * Serialises to CSV for file storage.
     * Format: STANDARD,id,brand,model,pricePerDay,available,fuelCapacity
     *
     * @return CSV string
     */
    @Override
    public String toFileString() {
        return "STANDARD," + getId() + "," + getBrand() + ","
                + getModel() + "," + getPricePerDay() + ","
                + isAvailable() + "," + fuelCapacity + "," + getImageUrl();
    }
}

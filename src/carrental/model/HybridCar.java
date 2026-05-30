package carrental.model;

/**
 * Represents a hybrid vehicle (petrol + electric) in the rental fleet.
 *
 * OOP Concepts Demonstrated:
 *   - Inheritance: extends Car, adding the batteryCapacity field.
 *   - Polymorphism: overrides getCarType() and calculateRentalCost()
 *     to apply a 5% hybrid surcharge on the daily rate.
 *
 * Cost formula: pricePerDay × days × 1.05 (5% hybrid premium).
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class HybridCar extends Car {

    /** Battery capacity in kilowatt-hours (kWh). */
    private int batteryCapacity;
    
    /** Fuel tank capacity in litres. */
    private int fuelCapacity;

    /**
     * Constructs a HybridCar.
     *
     * @param id              unique car identifier (auto-generated, e.g. "H001")
     * @param brand           manufacturer name
     * @param model           model name
     * @param pricePerDay     base daily rental rate in RM
     * @param available       rental availability flag
     * @param batteryCapacity battery size in kWh
     * @param fuelCapacity    fuel tank size in litres
     */
    public HybridCar(String id, String brand, String model,
                     double pricePerDay, boolean available, int batteryCapacity, int fuelCapacity) {
        super(id, brand, model, pricePerDay, available);
        this.batteryCapacity = batteryCapacity;
        this.fuelCapacity = fuelCapacity;
    }

    // =========================================================
    // Getter / Setter
    // =========================================================

    /** @return battery capacity in kWh */
    public int getBatteryCapacity()                    { return batteryCapacity; }

    /** @param batteryCapacity new battery size in kWh */
    public void setBatteryCapacity(int batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    
    public int getFuelCapacity() { return fuelCapacity; }
    public void setFuelCapacity(int fuelCapacity) { this.fuelCapacity = fuelCapacity; }

    // =========================================================
    // Abstract Method Implementations (Polymorphism)
    // =========================================================

    /**
     * Returns the car type label including battery size.
     *
     * @return e.g. "Hybrid (45 kWh)"
     */
    @Override
    public String getCarType() {
        return "Hybrid (" + batteryCapacity + " kWh, " + fuelCapacity + "L)";
    }

    /**
     * Calculates rental cost with a 5% hybrid surcharge.
     * Formula: pricePerDay × days × 1.05
     *
     * Lower surcharge than full EV because hybrid uses both
     * petrol and electric, reducing infrastructure dependency.
     *
     * @param days number of rental days
     * @return total rental cost in RM (including 5% premium)
     */
    @Override
    public double calculateRentalCost(int days) {
        return getPricePerDay() * days * 1.05;
    }

    @Override
    public String getSurchargeInfo() {
        return "+5% Hybrid Surcharge (Eco Surcharge)";
    }

    /**
     * Serialises to CSV for file storage (includes batteryCapacity).
     * Format: HYBRID,id,brand,model,pricePerDay,available,batteryCapacity
     *
     * @return CSV string
     */
    @Override
    public String toFileString() {
        return "HYBRID," + getId() + "," + getBrand() + ","
                + getModel() + "," + getPricePerDay() + ","
                + isAvailable() + "," + batteryCapacity + "," + fuelCapacity + "," + getImageUrl();
    }
}

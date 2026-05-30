package carrental.model;

/**
 * Represents an electric vehicle (EV) in the rental fleet.
 *
 * OOP Concepts Demonstrated:
 *   - Inheritance: extends Car, adding the batteryCapacity field.
 *   - Polymorphism: overrides getCarType() and calculateRentalCost()
 *     to apply a 10% EV surcharge on the daily rate.
 *
 * Cost formula: pricePerDay × days × 1.10 (10% EV premium).
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class ElectricCar extends Car {

    /** Battery capacity in kilowatt-hours (kWh). */
    private int batteryCapacity;

    /**
     * Constructs an ElectricCar.
     *
     * @param id              unique car identifier
     * @param brand           manufacturer name
     * @param model           model name
     * @param pricePerDay     base daily rental rate in RM
     * @param available       rental availability flag
     * @param batteryCapacity battery size in kWh
     */
    public ElectricCar(String id, String brand, String model,
                       double pricePerDay, boolean available, int batteryCapacity) {
        // Inherit all base car fields via super constructor
        super(id, brand, model, pricePerDay, available);
        this.batteryCapacity = batteryCapacity;
    }

    // =========================================================
    // EV-Specific Getter / Setter
    // =========================================================

    /** @return battery capacity in kWh */
    public int getBatteryCapacity()                   { return batteryCapacity; }

    /** @param batteryCapacity new battery size in kWh */
    public void setBatteryCapacity(int batteryCapacity){ this.batteryCapacity = batteryCapacity; }

    // =========================================================
    // Abstract Method Implementations (Polymorphism)
    // =========================================================

    /**
     * Returns a detailed engine type label including battery size.
     *
     * @return e.g. "Electric (75 kWh)"
     */
    @Override
    public String getCarType() {
        return "Electric (" + batteryCapacity + " kWh)";
    }

    /**
     * Calculates rental cost with a 10% EV surcharge.
     * Formula: pricePerDay × days × 1.10
     *
     * Advantage: reflects higher infrastructure/charging costs.
     * This polymorphic override is called automatically when
     * iterating an ArrayList&lt;Car&gt; containing ElectricCar objects.
     *
     * @param days number of rental days
     * @return total rental cost in RM (including 10% EV premium)
     */
    @Override
    public double calculateRentalCost(int days) {
        return getPricePerDay() * days * 1.10;
    }

    @Override
    public String getSurchargeInfo() {
        return "+10% EV Surcharge (Environmental Surcharge)";
    }

    /**
     * Serialises to CSV for file storage (includes batteryCapacity).
     * Format: ELECTRIC,id,brand,model,pricePerDay,available,batteryCapacity
     *
     * @return CSV string
     */
    @Override
    public String toFileString() {
        return "ELECTRIC," + getId() + "," + getBrand() + ","
                + getModel() + "," + getPricePerDay() + ","
                + isAvailable() + "," + batteryCapacity + "," + getImageUrl();
    }
}

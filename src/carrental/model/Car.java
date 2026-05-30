package carrental.model;

/**
 * Abstract base class representing a car in the rental fleet.
 *
 * OOP Concepts Demonstrated:
 *   - Encapsulation: All fields are private; accessed via public getters/setters.
 *   - Abstraction: Abstract methods getCarType(), calculateRentalCost(), and
 *     toFileString() are declared here but implemented by each subclass.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public abstract class Car {

    // --- Private fields (Encapsulation) ---
    private String  id;
    private String  brand;
    private String  model;
    private double  pricePerDay;
    private boolean available;
    private String  imageUrl = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=400";

    /**
     * Constructs a Car with all required fields.
     *
     * @param id          unique car identifier (e.g. "C001")
     * @param brand       car manufacturer name
     * @param model       car model name
     * @param pricePerDay daily rental rate in RM
     * @param available   true if the car is available for rental
     */
    public Car(String id, String brand, String model,
               double pricePerDay, boolean available) {
        this.id          = id;
        this.brand       = brand;
        this.model       = model;
        this.pricePerDay = pricePerDay;
        this.available   = available;
    }

    // =========================================================
    // Getters
    // =========================================================

    /** @return unique car identifier */
    public String getId()           { return id; }

    /** @return car manufacturer name */
    public String getBrand()        { return brand; }

    /** @return car model name */
    public String getModel()        { return model; }

    /** @return daily rental rate in RM */
    public double getPricePerDay()  { return pricePerDay; }

    /** @return true if available; false if rented out */
    public boolean isAvailable()    { return available; }

    // =========================================================
    // Setters (used by the Edit feature)
    // =========================================================

    /** @param brand new manufacturer name */
    public void setBrand(String brand)            { this.brand = brand; }

    /** @param model new model name */
    public void setModel(String model)            { this.model = model; }

    /** @param pricePerDay new daily rate in RM */
    public void setPricePerDay(double pricePerDay){ this.pricePerDay = pricePerDay; }

    /** @param available rental availability flag */
    public void setAvailable(boolean available)   { this.available = available; }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrl = imageUrl.trim();
        }
    }

    // =========================================================
    // Abstract Methods (Abstraction + Polymorphism)
    // =========================================================

    /**
     * Returns a human-readable label describing the car type.
     * Each subclass provides its own implementation (Polymorphism).
     *
     * @return car type description string
     */
    public abstract String getCarType();

    /**
     * Returns a human-readable description of the surcharge rate applied to this car type.
     * Each subclass provides its own specific details (Polymorphism).
     *
     * @return surcharge rate description
     */
    public abstract String getSurchargeInfo();

    /**
     * Calculates the total rental cost for a given number of days.
     * Behaviour differs by subclass (Polymorphism).
     *
     * @param days number of rental days (must be > 0)
     * @return total cost in RM
     */
    public abstract double calculateRentalCost(int days);

    /**
     * Serialises the car to a CSV-formatted line for file storage.
     * Each subclass includes its own fields.
     *
     * @return comma-separated string representation
     */
    public abstract String toFileString();

    // =========================================================
    // Overridden Object Method
    // =========================================================

    /**
     * Returns a friendly string representation of the car.
     * Overrides Object.toString() to produce a readable label.
     */
    @Override
    public String toString() {
        return brand + " " + model + " [" + id + "]";
    }
}

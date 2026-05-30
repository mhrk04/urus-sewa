package carrental.ui;

import carrental.model.Car;
import carrental.model.Rental;
import carrental.service.FileService;
import carrental.service.InvoiceService;
import carrental.service.InventoryService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Modal dialog for processing a car rental transaction.
 *
 * Collects full customer information:
 *   - Customer Name
 *   - IC / Passport Number
 *   - Phone Number
 *   - Number of Rental Days
 *
 * Computes the total cost using Car.calculateRentalCost() –
 * demonstrating POLYMORPHISM:
 *   StandardCar → cost = pricePerDay × days
 *   ElectricCar → cost = pricePerDay × days × 1.10 (10% EV surcharge)
 *   HybridCar   → cost = pricePerDay × days × 1.05 (5% hybrid surcharge)
 *
 * After confirmation, generates a downloadable PDF invoice.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class RentalDialog extends JDialog {

    private final Car              car;
    private final InventoryService inventoryService;
    private final FileService      fileService;

    private JTextField txtCustomerName;
    private JTextField txtCustomerIC;
    private JTextField txtCustomerPhone;
    private JTextField txtRentDate;
    private JComboBox<String> cmbIdentityType;
    private JLabel            lblIdentityLabel;
    private JTextField txtReturnDate;
    private JLabel     lblCalculatedDays;
    private JLabel     lblEstimate;

    private LocalDate  rentDate;
    private LocalDate  returnDate;

    /**
     * Constructs the rental dialog.
     *
     * @param parent           parent JFrame
     * @param car              the car to be rented
     * @param inventoryService service to record the rental
     * @param fileService      service to persist data to file
     */
    public RentalDialog(JFrame parent, Car car,
                        InventoryService inventoryService, FileService fileService) {
        super(parent, "Rent Vehicle – " + car.getBrand() + " " + car.getModel(), true);
        this.car              = car;
        this.inventoryService = inventoryService;
        this.fileService      = fileService;
        this.rentDate         = LocalDate.now();
        this.returnDate       = LocalDate.now().plusDays(1);
        buildUI();
        pack();
        setMinimumSize(new Dimension(430, 0));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // =========================================================
    // UI Construction
    // =========================================================

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIUtils.CARD);

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonRow(), BorderLayout.SOUTH);
    }

    /** Builds the car info + customer input form. */
    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBackground(UIUtils.CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        // --- Section: Vehicle Details (read-only) ---
        JLabel vehicleHeader = UIUtils.boldLabel("─── VEHICLE DETAILS ───");
        vehicleHeader.setForeground(UIUtils.PRIMARY);
        p.add(vehicleHeader); p.add(new JLabel());

        p.add(UIUtils.boldLabel("Car ID:"));
        p.add(UIUtils.plainLabel(car.getId()));

        p.add(UIUtils.boldLabel("Vehicle:"));
        p.add(UIUtils.plainLabel(car.getBrand() + " " + car.getModel()));

        p.add(UIUtils.boldLabel("Type:"));
        p.add(UIUtils.plainLabel(car.getCarType()));   // Polymorphism

        p.add(UIUtils.boldLabel("Rate / Day:"));
        p.add(UIUtils.plainLabel(String.format("RM %.2f", car.getPricePerDay())));

        // --- Separator ---
        p.add(new JLabel()); p.add(new JLabel());

        // --- Section: Customer Details ---
        JLabel custHeader = UIUtils.boldLabel("─── CUSTOMER DETAILS ───");
        custHeader.setForeground(UIUtils.PRIMARY);
        p.add(custHeader); p.add(new JLabel());

        p.add(UIUtils.boldLabel("Customer Name:"));
        txtCustomerName = new JTextField();
        UIUtils.styleTextField(txtCustomerName);
        p.add(txtCustomerName);

        p.add(UIUtils.boldLabel("Identity Type:"));
        cmbIdentityType = new JComboBox<>(new String[]{"IC", "Passport"});
        cmbIdentityType.setBackground(UIUtils.CARD);
        cmbIdentityType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbIdentityType.addActionListener(e -> updateIdentityLabel());
        p.add(cmbIdentityType);

        lblIdentityLabel = UIUtils.boldLabel("IC Number (YYMMDD-XX-XXXX):");
        p.add(lblIdentityLabel);

        txtCustomerIC = new JTextField();
        UIUtils.styleTextField(txtCustomerIC);
        p.add(txtCustomerIC);

        p.add(UIUtils.boldLabel("Phone Number:"));
        txtCustomerPhone = new JTextField();
        UIUtils.styleTextField(txtCustomerPhone);
        p.add(txtCustomerPhone);

        // --- Separator ---
        p.add(new JLabel()); p.add(new JLabel());

        // --- Section: Rental Calculation ---
        JLabel rentalHeader = UIUtils.boldLabel("─── RENTAL DETAILS ───");
        rentalHeader.setForeground(UIUtils.PRIMARY);
        p.add(rentalHeader); p.add(new JLabel());

        p.add(UIUtils.boldLabel("Rent Date:"));
        JPanel rentPanel = new JPanel(new BorderLayout(5, 0));
        rentPanel.setBackground(UIUtils.CARD);
        txtRentDate = new JTextField(rentDate.toString());
        txtRentDate.setEditable(false);
        UIUtils.styleTextField(txtRentDate);
        JButton btnRentCal = new JButton("📅");
        btnRentCal.setPreferredSize(new Dimension(34, 30));
        UIUtils.styleButton(btnRentCal, UIUtils.ACCENT);
        btnRentCal.addActionListener(e -> chooseRentDate());
        rentPanel.add(txtRentDate, BorderLayout.CENTER);
        rentPanel.add(btnRentCal, BorderLayout.EAST);
        p.add(rentPanel);

        p.add(UIUtils.boldLabel("Return Date:"));
        JPanel returnPanel = new JPanel(new BorderLayout(5, 0));
        returnPanel.setBackground(UIUtils.CARD);
        txtReturnDate = new JTextField(returnDate.toString());
        txtReturnDate.setEditable(false);
        UIUtils.styleTextField(txtReturnDate);
        JButton btnReturnCal = new JButton("📅");
        btnReturnCal.setPreferredSize(new Dimension(34, 30));
        UIUtils.styleButton(btnReturnCal, UIUtils.ACCENT);
        btnReturnCal.addActionListener(e -> chooseReturnDate());
        returnPanel.add(txtReturnDate, BorderLayout.CENTER);
        returnPanel.add(btnReturnCal, BorderLayout.EAST);
        p.add(returnPanel);

        p.add(UIUtils.boldLabel("Rental Days:"));
        lblCalculatedDays = new JLabel("1 day(s)");
        lblCalculatedDays.setFont(new Font("Arial", Font.PLAIN, 13));
        p.add(lblCalculatedDays);

        // --- Estimated cost (updates dynamically) ---
        p.add(UIUtils.boldLabel("Estimated Total:"));
        lblEstimate = new JLabel("RM 0.00");
        lblEstimate.setFont(new Font("Arial", Font.BOLD, 15));
        lblEstimate.setForeground(UIUtils.SUCCESS);
        p.add(lblEstimate);

        // Initialize estimates
        updateEstimate();

        return p;
    }

    /** Builds the Cancel and Confirm Rental buttons. */
    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        p.setBackground(UIUtils.CARD);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));

        JButton btnCancel = new JButton("Cancel");
        UIUtils.styleButton(btnCancel, new Color(113, 128, 150));
        btnCancel.setPreferredSize(new Dimension(90, 34));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("Confirm Rental");
        UIUtils.styleButton(btnConfirm, UIUtils.SUCCESS);
        btnConfirm.setPreferredSize(new Dimension(145, 34));
        btnConfirm.addActionListener(e -> handleConfirm());

        p.add(btnCancel);
        p.add(btnConfirm);
        return p;
    }

    // =========================================================
    // Live Cost Estimate
    // =========================================================

    /**
     * Recalculates the estimated rental cost as the user types.
     *
     * KEY POLYMORPHISM POINT:
     * car.calculateRentalCost(days) calls:
     *   → StandardCar.calculateRentalCost() if car is a StandardCar
     *   → ElectricCar.calculateRentalCost() if car is an ElectricCar
     *   → HybridCar.calculateRentalCost() if car is a HybridCar
     */
    private void updateIdentityLabel() {
        if (cmbIdentityType.getSelectedIndex() == 0) {
            lblIdentityLabel.setText("IC Number (YYMMDD-XX-XXXX):");
        } else {
            lblIdentityLabel.setText("Passport Number:");
        }
    }

    private void updateEstimate() {
        if (rentDate == null || returnDate == null) {
            lblCalculatedDays.setText("0 day(s)");
            lblEstimate.setText("RM 0.00");
            return;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(rentDate, returnDate);
        if (days <= 0) {
            lblCalculatedDays.setText("Invalid Date Range");
            lblCalculatedDays.setForeground(UIUtils.DANGER);
            lblEstimate.setText("RM 0.00");
        } else {
            Rental overlapping = inventoryService.findOverlappingRental(car.getId(), rentDate, returnDate);
            if (overlapping != null) {
                lblCalculatedDays.setText("Overlap: Booked by " + overlapping.getCustomerName());
                lblCalculatedDays.setForeground(UIUtils.DANGER);
                lblEstimate.setText("RM 0.00");
            } else {
                lblCalculatedDays.setText(days + " day(s)");
                lblCalculatedDays.setForeground(Color.BLACK);
                double cost = car.calculateRentalCost((int) days); // Polymorphism
                lblEstimate.setText(String.format("RM %.2f", cost));
            }
        }
    }

    private void chooseRentDate() {
        DatePickerDialog dialog = new DatePickerDialog(this, rentDate, LocalDate.now());
        dialog.setVisible(true);
        if (dialog.getSelectedDate() != null) {
            rentDate = dialog.getSelectedDate();
            txtRentDate.setText(rentDate.toString());
            // Auto-adjust return date to rent date + 1 if return date is invalid
            if (!returnDate.isAfter(rentDate)) {
                returnDate = rentDate.plusDays(1);
                txtReturnDate.setText(returnDate.toString());
            }
            updateEstimate();
        }
    }

    private void chooseReturnDate() {
        DatePickerDialog dialog = new DatePickerDialog(this, returnDate, rentDate.plusDays(1));
        dialog.setVisible(true);
        if (dialog.getSelectedDate() != null) {
            returnDate = dialog.getSelectedDate();
            txtReturnDate.setText(returnDate.toString());
            updateEstimate();
        }
    }

    // =========================================================
    // Confirm Rental
    // =========================================================

    /**
     * Validates all input, creates a Rental record, marks the car
     * as rented out, persists data to file, and generates PDF invoice.
     */
    private void handleConfirm() {
        // --- Validate customer name ---
        String name = txtCustomerName.getText().trim();
        if (name.isEmpty()) {
            showWarning("Please enter the customer's name.");
            return;
        }

        // --- Validate IC / Passport ---
        String ic = txtCustomerIC.getText().trim();
        if (ic.isEmpty()) {
            String type = cmbIdentityType.getSelectedItem().toString();
            showWarning("Please enter the customer's " + type + ".");
            return;
        }
        
        if (cmbIdentityType.getSelectedIndex() == 0) {
            // IC Validation: Malaysian IC (e.g. 900101-14-5555)
            if (!ic.matches("^\\d{6}-?\\d{2}-?\\d{4}$")) {
                showWarning("Invalid IC format. Expected format: YYMMDD-XX-XXXX (e.g., 900101-14-5555).");
                return;
            }
        } else {
            // Passport Validation: 5-20 alphanumeric characters
            if (!ic.matches("^[A-Za-z0-9]{5,20}$")) {
                showWarning("Invalid Passport format. Expected 5-20 alphanumeric characters.");
                return;
            }
        }

        // --- Validate phone ---
        String phone = txtCustomerPhone.getText().trim();
        if (phone.isEmpty()) {
            showWarning("Please enter the customer's phone number.");
            return;
        }
        if (!phone.matches("^\\+?\\d+$")) {
            showWarning("Invalid Phone format. Expected digits only (e.g., 0123456789).");
            return;
        }

        // --- Validate rent/return dates ---
        if (rentDate == null || returnDate == null) {
            showWarning("Please select valid rent and return dates.");
            return;
        }

        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(rentDate, returnDate);
        if (days <= 0) {
            showWarning("Return date must be at least 1 day after the rent date.");
            return;
        }

        // Check for date range overlap with existing bookings
        Rental overlapping = inventoryService.findOverlappingRental(car.getId(), rentDate, returnDate);
        if (overlapping != null) {
            showWarning("Overlap Detected!\n\nThis vehicle is already booked by " 
                    + overlapping.getCustomerName() + " from " 
                    + overlapping.getRentalDate() + " to " 
                    + overlapping.getReturnDate() + ".\n\n"
                    + "Please select a different date range.");
            return;
        }

        // Compute cost via Polymorphism: the concrete `Car` subclass
        // determines the final price when `calculateRentalCost(days)` is called.
        // This keeps pricing logic inside the model classes (Single Responsibility).
        double totalCost = car.calculateRentalCost(days);

        // Confirm details before writing to file and finalizing booking
        int confirm = JOptionPane.showConfirmDialog(this,
                "Please confirm the rental details:\n\n"
                + "Customer Name: " + name + "\n"
                + "IC/Passport  : " + ic + "\n"
                + "Phone Number : " + phone + "\n"
                + "Vehicle      : " + car.getBrand() + " " + car.getModel() + "\n"
                + "Duration     : " + days + " day(s)\n"
                + "Total Cost   : RM " + String.format("%.2f", totalCost) + "\n\n"
                + "Do you want to proceed with this booking?",
                "Confirm Rental Details", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Live double-check: reload the latest rentals from disk to avoid
        // race conditions where another user/process may have written a new
        // booking while this dialog was open. If a fresh overlap is found,
        // abort the operation so the user can choose new dates.
        ArrayList<Rental> freshHistory = fileService.loadRentals();
        inventoryService.setRentalHistory(freshHistory);
        Rental freshOverlapping = inventoryService.findOverlappingRental(car.getId(), rentDate, returnDate);
        if (freshOverlapping != null) {
            showWarning("Collision Detected!\n\nAnother user just booked this vehicle for the same dates while you were confirming.\n"
                    + "Please select a different date range.");
            return;
        }

        // Generate rental ID
        String rentalId = "R" + String.format("%03d", inventoryService.getNextRentalId());
        String rentalDate = rentDate.toString(); // Use selected rent date!

        // Create Rental object with full customer info.
        // If the rental `rentDate` is in the future, mark the record as
        // BOOKED so it will be automatically transitioned to ACTIVE when
        // the start date arrives (see InventoryService.checkAndTransitionBookings).
        String status = LocalDate.now().isBefore(rentDate)
            ? Rental.STATUS_BOOKED
            : Rental.STATUS_ACTIVE;

        Rental rental = new Rental(rentalId, car.getId(), name, ic, phone,
                                   days, totalCost, rentalDate, status);

        // Update car status and record rental (only set false if active today)
        if (Rental.STATUS_ACTIVE.equals(status)) {
            car.setAvailable(false);
        } else {
            car.setAvailable(true); // Remains available until start date
        }
        inventoryService.addRental(rental);

        // Persist to files
        fileService.saveInventory(inventoryService.getAllCars());
        fileService.saveRentals(inventoryService.getRentalHistory());

        // Generate PDF invoice
        InvoiceService invoiceService = new InvoiceService();
        String pdfPath = invoiceService.generateInvoice(rental, car);

        // Show confirmation with invoice info
        String invoiceMsg = (pdfPath != null)
                ? "\n\nInvoice saved: " + pdfPath
                : "\n\n(Invoice generation failed)";

        JOptionPane.showMessageDialog(this,
                "Rental Confirmed!\n\n"
                + "Rental ID   : " + rentalId + "\n"
                + "Customer    : " + name + "\n"
                + "IC/Passport : " + ic + "\n"
                + "Phone       : " + phone + "\n"
                + "Vehicle     : " + car.getBrand() + " " + car.getModel() + "\n"
                + "Type        : " + car.getCarType() + "\n"
                + "Duration    : " + days + " day(s)\n"
                + "Total Cost  : RM " + String.format("%.2f", totalCost)
                + invoiceMsg,
                "Rental Confirmed", JOptionPane.INFORMATION_MESSAGE);

        // Ask if user wants to open the invoice file
        if (pdfPath != null) {
            int open = JOptionPane.showConfirmDialog(this,
                    "Open the invoice PDF now?",
                    "Open Invoice", JOptionPane.YES_NO_OPTION);
            if (open == JOptionPane.YES_OPTION) {
                openFile(pdfPath);
            }
        }

        dispose();
    }

    /**
     * Opens a file using the system's default application.
     *
     * @param filePath path to the file to open
     */
    private void openFile(String filePath) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.open(new java.io.File(filePath));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not open file automatically.\n"
                    + "Please open manually: " + filePath,
                    "File Open Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Helper to show a warning dialog. */
    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}

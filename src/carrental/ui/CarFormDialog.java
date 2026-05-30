package carrental.ui;

import carrental.model.Car;
import carrental.model.ElectricCar;
import carrental.model.HybridCar;
import carrental.model.StandardCar;
import carrental.service.FileService;
import carrental.service.InventoryService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modal dialog for adding or editing a car in the fleet.
 *
 * Opens as a popup window when the user clicks "Add Vehicle" or "Edit"
 * button on the main window. This keeps the main window clean and
 * only shows the form when needed.
 *
 * Operates in two modes:
 *   - Add mode  : Car ID is auto-generated based on car type prefix
 *                 (P = Petrol, E = Electric, H = Hybrid).
 *   - Edit mode : form is pre-populated; updates existing Car fields.
 *
 * OOP Concepts:
 *   - Encapsulation: all form fields private; state accessed via methods.
 *   - Polymorphism: creates StandardCar, ElectricCar, or HybridCar
 *     based on JComboBox selection.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class CarFormDialog extends JDialog {

    private final InventoryService inventoryService;
    private final FileService      fileService;
    private final CarTablePanel    tablePanel;

    // --- Form fields ---
    private JLabel        lblIdValue;       // Read-only (auto-generated)
    private JTextField    txtBrand, txtModel, txtPrice, txtBattery, txtFuel, txtImageUrl;
    private JComboBox<String> cmbType;
    private JLabel        lblBattery, lblFuel;
    private JButton       btnSave;
    private JLabel        lblTitle;

    /**
     * Tracks whether we are in Edit mode.
     * null  = Add mode | non-null = car being edited.
     */
    private Car editingCar = null;

    /**
     * Whether a save operation occurred (used by caller to know
     * if the table should refresh).
     */
    private boolean saved = false;

    /**
     * Constructs the form dialog in Add mode.
     *
     * @param parent           parent JFrame
     * @param inventoryService shared inventory service
     * @param fileService      shared file service
     * @param tablePanel       reference to refresh the table after changes
     */
    public CarFormDialog(JFrame parent, InventoryService inventoryService,
                         FileService fileService, CarTablePanel tablePanel) {
        super(parent, "Add New Vehicle", true);
        this.inventoryService = inventoryService;
        this.fileService      = fileService;
        this.tablePanel       = tablePanel;
        buildUI();
        updateAutoId(); // Generate initial ID for default type
        pack();
        setMinimumSize(new Dimension(400, 0));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Constructs the form dialog in Edit mode with pre-populated data.
     *
     * @param parent           parent JFrame
     * @param inventoryService shared inventory service
     * @param fileService      shared file service
     * @param tablePanel       reference to refresh the table
     * @param carToEdit        the Car object to edit
     */
    public CarFormDialog(JFrame parent, InventoryService inventoryService,
                         FileService fileService, CarTablePanel tablePanel,
                         Car carToEdit) {
        this(parent, inventoryService, fileService, tablePanel);
        populateForEdit(carToEdit);
    }

    /** @return true if a save was performed */
    public boolean isSaved() { return saved; }

    // =========================================================
    // UI Construction
    // =========================================================

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIUtils.CARD);

        add(buildFormPanel(),  BorderLayout.CENTER);
        add(buildButtonRow(), BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIUtils.CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Panel title
        lblTitle = new JLabel("Add New Vehicle", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 15));
        lblTitle.setForeground(UIUtils.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        p.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Car Type (comes FIRST so auto-ID generates on type change)
        addLabel(p, gbc, 1, "Car Type:");
        cmbType = new JComboBox<>(new String[]{"Petrol", "Electric", "Hybrid"});
        cmbType.setBackground(UIUtils.CARD);
        cmbType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbType.addActionListener((ActionEvent e) -> {
            toggleFields();
            if (editingCar == null) updateAutoId(); // Only update ID in Add mode
        });
        gbc.gridx = 1; gbc.gridy = 1;
        p.add(cmbType, gbc);

        // Car ID (auto-generated, read-only)
        addLabel(p, gbc, 2, "Car ID:");
        lblIdValue = new JLabel("P001");
        lblIdValue.setFont(new Font("Arial", Font.BOLD, 13));
        lblIdValue.setForeground(UIUtils.ACCENT);
        gbc.gridx = 1; gbc.gridy = 2;
        p.add(lblIdValue, gbc);

        // Brand
        addLabel(p, gbc, 3, "Brand:");
        txtBrand = new JTextField(14); UIUtils.styleTextField(txtBrand);
        gbc.gridx = 1; gbc.gridy = 3;
        p.add(txtBrand, gbc);

        // Model
        addLabel(p, gbc, 4, "Model:");
        txtModel = new JTextField(14); UIUtils.styleTextField(txtModel);
        gbc.gridx = 1; gbc.gridy = 4;
        p.add(txtModel, gbc);

        // Price
        addLabel(p, gbc, 5, "Price / Day (RM):");
        txtPrice = new JTextField(14); UIUtils.styleTextField(txtPrice);
        gbc.gridx = 1; gbc.gridy = 5;
        p.add(txtPrice, gbc);

        // Battery row (visible for Electric and Hybrid)
        lblBattery = UIUtils.boldLabel("Battery (kWh):");
        lblBattery.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 6;
        p.add(lblBattery, gbc);

        txtBattery = new JTextField(14); UIUtils.styleTextField(txtBattery);
        txtBattery.setVisible(false);
        gbc.gridx = 1; gbc.gridy = 6;
        p.add(txtBattery, gbc);

        // Fuel row (visible for Petrol and Hybrid)
        lblFuel = UIUtils.boldLabel("Fuel Tank (L):");
        gbc.gridx = 0; gbc.gridy = 7;
        p.add(lblFuel, gbc);

        txtFuel = new JTextField(14); UIUtils.styleTextField(txtFuel);
        gbc.gridx = 1; gbc.gridy = 7;
        p.add(txtFuel, gbc);

        // Image URL row
        addLabel(p, gbc, 8, "Image URL:");
        txtImageUrl = new JTextField(14); UIUtils.styleTextField(txtImageUrl);
        gbc.gridx = 1; gbc.gridy = 8;
        p.add(txtImageUrl, gbc);

        return p;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        p.setBackground(UIUtils.CARD);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));

        JButton btnCancel = new JButton("Cancel");
        UIUtils.styleButton(btnCancel, new Color(113, 128, 150));
        btnCancel.setPreferredSize(new Dimension(90, 34));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton("Add Vehicle");
        UIUtils.styleButton(btnSave, UIUtils.SUCCESS);
        btnSave.setPreferredSize(new Dimension(140, 34));
        btnSave.addActionListener(e -> handleSave());

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    /** Helper: adds a bold label to column 0 of the given row. */
    private void addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row;
        p.add(UIUtils.boldLabel(text), gbc);
    }

    // =========================================================
    // Auto ID Generation
    // =========================================================

    /**
     * Updates the displayed car ID based on the selected car type.
     * ID prefix: P = Petrol, E = Electric, H = Hybrid.
     * The next sequential number is generated by InventoryService.
     */
    private void updateAutoId() {
        String prefix = getSelectedPrefix();
        String nextId = inventoryService.generateCarId(prefix);
        lblIdValue.setText(nextId);
    }

    /**
     * Returns the ID prefix letter for the currently selected car type.
     *
     * @return "P", "E", or "H"
     */
    private String getSelectedPrefix() {
        int idx = cmbType.getSelectedIndex();
        if (idx == 1) return "E";       // Electric
        if (idx == 2) return "H";       // Hybrid
        return "P";                     // Petrol (default)
    }

    /** Shows battery/fuel fields dynamically based on car type. */
    private void toggleFields() {
        int idx = cmbType.getSelectedIndex();
        boolean showBattery = (idx == 1 || idx == 2); // Electric or Hybrid
        boolean showFuel    = (idx == 0 || idx == 2); // Petrol or Hybrid
        
        lblBattery.setVisible(showBattery);
        txtBattery.setVisible(showBattery);
        lblFuel.setVisible(showFuel);
        txtFuel.setVisible(showFuel);
        pack(); // Resize dialog to fit
    }

    // =========================================================
    // Edit Mode
    // =========================================================

    /**
     * Pre-fills the form with an existing car's data for editing.
     * Switches the dialog into Edit mode.
     *
     * @param car the Car object to edit
     */
    private void populateForEdit(Car car) {
        editingCar = car;
        setTitle("Edit Vehicle – " + car.getId());
        lblTitle.setText("Edit Vehicle");
        lblIdValue.setText(car.getId());
        txtBrand.setText(car.getBrand());
        txtModel.setText(car.getModel());
        txtPrice.setText(String.valueOf(car.getPricePerDay()));

        if (car instanceof ElectricCar) {
            cmbType.setSelectedIndex(1);
            txtBattery.setText(String.valueOf(((ElectricCar) car).getBatteryCapacity()));
        } else if (car instanceof HybridCar) {
            cmbType.setSelectedIndex(2);
            txtBattery.setText(String.valueOf(((HybridCar) car).getBatteryCapacity()));
            txtFuel.setText(String.valueOf(((HybridCar) car).getFuelCapacity()));
        } else if (car instanceof StandardCar) {
            cmbType.setSelectedIndex(0);
            txtFuel.setText(String.valueOf(((StandardCar) car).getFuelCapacity()));
        }
        txtImageUrl.setText(car.getImageUrl());
        cmbType.setEnabled(false); // Cannot change car type during edit
        toggleFields();

        btnSave.setText("Update Vehicle");
        UIUtils.styleButton(btnSave, UIUtils.ACCENT);
    }

    // =========================================================
    // Save Logic
    // =========================================================

    /** Dispatches to handleAdd or handleUpdate based on current mode. */
    private void handleSave() {
        // --- Input Validation ---
        String brand    = txtBrand.getText().trim();
        String model    = txtModel.getText().trim();
        String priceStr = txtPrice.getText().trim();

        if (brand.isEmpty() || model.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields (Brand, Model, Price) are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Price must be a positive number (e.g. 120.00).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (editingCar != null) {
            handleUpdate(brand, model, price);
        } else {
            handleAdd(brand, model, price);
        }
    }

    /**
     * Creates and adds a new Car using the auto-generated ID.
     * Polymorphism: creates StandardCar, ElectricCar, or HybridCar
     * based on the JComboBox selection.
     */
    private void handleAdd(String brand, String model, double price) {
        String id = lblIdValue.getText(); // Auto-generated ID
        int typeIdx = cmbType.getSelectedIndex();

        Car car;
        if (typeIdx == 1) { // Electric
            int battery = parseBattery();
            if (battery < 0) return;
            car = new ElectricCar(id, brand, model, price, true, battery);
        } else if (typeIdx == 2) { // Hybrid
            int battery = parseBattery();
            int fuel = parseFuel();
            if (battery < 0 || fuel < 0) return;
            car = new HybridCar(id, brand, model, price, true, battery, fuel);
        } else { // Petrol
            int fuel = parseFuel();
            if (fuel < 0) return;
            car = new StandardCar(id, brand, model, price, true, fuel);
        }

        String imageUrl = txtImageUrl.getText().trim();
        if (!imageUrl.isEmpty()) {
            car.setImageUrl(imageUrl);
        }

        inventoryService.addCar(car);
        fileService.saveInventory(inventoryService.getAllCars());
        tablePanel.refreshTable();
        saved = true;

        JOptionPane.showMessageDialog(this,
                "Vehicle added successfully!\nCar ID: " + id,
                "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /**
     * Updates an existing car's fields (brand, model, price, battery, fuel).
     */
    private void handleUpdate(String brand, String model, double price) {
        editingCar.setBrand(brand);
        editingCar.setModel(model);
        editingCar.setPricePerDay(price);

        if (editingCar instanceof ElectricCar) {
            int battery = parseBattery();
            if (battery < 0) return;
            ((ElectricCar) editingCar).setBatteryCapacity(battery);
        } else if (editingCar instanceof HybridCar) {
            int battery = parseBattery();
            int fuel = parseFuel();
            if (battery < 0 || fuel < 0) return;
            ((HybridCar) editingCar).setBatteryCapacity(battery);
            ((HybridCar) editingCar).setFuelCapacity(fuel);
        } else if (editingCar instanceof StandardCar) {
            int fuel = parseFuel();
            if (fuel < 0) return;
            ((StandardCar) editingCar).setFuelCapacity(fuel);
        }

        editingCar.setImageUrl(txtImageUrl.getText().trim());

        fileService.saveInventory(inventoryService.getAllCars());
        tablePanel.refreshTable();
        saved = true;

        JOptionPane.showMessageDialog(this,
                "Vehicle updated successfully!",
                "Updated", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /** Parses the battery capacity field with validation. */
    private int parseBattery() {
        try {
            int val = Integer.parseInt(txtBattery.getText().trim());
            if (val <= 0) throw new NumberFormatException();
            return val;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Battery capacity must be a positive whole number (e.g. 75).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    /** Parses the fuel capacity field with validation. */
    private int parseFuel() {
        try {
            int val = Integer.parseInt(txtFuel.getText().trim());
            if (val <= 0) throw new NumberFormatException();
            return val;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Fuel capacity must be a positive whole number (e.g. 40).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
}

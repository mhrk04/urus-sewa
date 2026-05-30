package carrental.ui;

import carrental.model.Car;
import carrental.model.Rental;
import carrental.service.FileService;
import carrental.service.InventoryService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.time.LocalDate;

/**
 * Main content panel displaying the car inventory table.
 *
 * Features:
 *   - Display  : shows all cars in a JTable (ArrayList iterated via polymorphism)
 *   - Search   : filters by brand or model keyword (case-insensitive)
 *   - Filter   : checkbox to show available cars only
 *   - Add      : (Admin) opens CarFormDialog popup to add a new car
 *   - Edit     : (Admin) opens CarFormDialog popup to edit selected car
 *   - Delete   : (Admin) removes selected car from inventory and file
 *   - Rent     : (Admin) opens RentalDialog to process a rental transaction
 *   - Return   : (Admin) marks a rented car as available again and marks rental COMPLETED
 *   - History  : (Admin) opens RentalHistoryDialog to view customer records
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class CarTablePanel extends JPanel {

    private final boolean          isAdmin;
    private final InventoryService inventoryService;
    private final FileService      fileService;

    private JTable            carTable;
    private DefaultTableModel tableModel;
    private JTextField        txtSearch;
    private JPanel            detailPanel;
    private JLabel            lblDetailImage;
    private JLabel            lblDetailTitle;
    private JLabel            lblDetailSpecs;
    private JCheckBox         chkAvail;

    /**
     * Constructs the table panel.
     *
     * @param isAdmin          true for Admin mode (extra action buttons visible)
     * @param inventoryService shared inventory service
     * @param fileService      shared file service
     */
    public CarTablePanel(boolean isAdmin, InventoryService inventoryService,
                         FileService fileService) {
        this.isAdmin          = isAdmin;
        this.inventoryService = inventoryService;
        this.fileService      = fileService;
        buildUI();
    }

    // =========================================================
    // UI Construction
    // =========================================================

    private void buildUI() {
        setBackground(UIUtils.BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 14, 14));

        add(buildSearchPanel(),     BorderLayout.NORTH);

        // Sidebar detail panel on the EAST, table scroll pane in the CENTER
        JPanel centerWrapper = new JPanel(new BorderLayout(10, 0));
        centerWrapper.setBackground(UIUtils.BG);
        centerWrapper.add(buildTableScrollPane(), BorderLayout.CENTER);
        centerWrapper.add(buildDetailPanel(), BorderLayout.EAST);
        add(centerWrapper, BorderLayout.CENTER);

        // Action buttons only visible to Admin
        if (isAdmin) add(buildActionPanel(), BorderLayout.SOUTH);
    }

    /** Builds the search bar and availability filter row. */
    private JPanel buildSearchPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(UIUtils.BG);

        p.add(UIUtils.boldLabel("Search:"));
        txtSearch = new JTextField(22);
        UIUtils.styleTextField(txtSearch);
        
        // Add DocumentListener for real-time live search as user types
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { handleSearch(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { handleSearch(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { handleSearch(); }
        });
        
        p.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        UIUtils.styleButton(btnSearch, UIUtils.ACCENT);
        btnSearch.addActionListener(e -> handleSearch());
        p.add(btnSearch);

        JButton btnReset = new JButton("Show All");
        UIUtils.styleButton(btnReset, new Color(113, 128, 150));
        btnReset.addActionListener(e -> { txtSearch.setText(""); refreshTable(); });
        p.add(btnReset);

        JButton btnReload = new JButton("Reload");
        UIUtils.styleButton(btnReload, UIUtils.PRIMARY);
        btnReload.addActionListener(e -> {
            inventoryService.setInventory(fileService.loadInventory());
            inventoryService.setRentalHistory(fileService.loadRentals());
            refreshTable();
            JOptionPane.showMessageDialog(this, 
                    "Inventory and rental records reloaded successfully from disk.", 
                    "Reload Success", JOptionPane.INFORMATION_MESSAGE);
        });
        p.add(btnReload);

        // Availability filter checkbox
        p.add(Box.createHorizontalStrut(10));
        p.add(UIUtils.boldLabel("Filter:"));
        chkAvail = new JCheckBox("Available Only");
        chkAvail.setBackground(UIUtils.BG);
        chkAvail.setFont(new Font("Arial", Font.PLAIN, 12));
        chkAvail.addActionListener(e -> {
            if (chkAvail.isSelected()) displayCars(getAvailableCars());
            else refreshTable();
        });
        p.add(chkAvail);

        return p;
    }

    /**
     * Builds the JTable wrapped in a JScrollPane.
     * Table cells are read-only (isCellEditable returns false).
     */
    private JScrollPane buildTableScrollPane() {
        String[] columns = {"Car ID", "Brand", "Model", "Price / Day", "Vehicle Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        carTable = new JTable(tableModel);
        carTable.setRowHeight(27);
        carTable.setFont(new Font("Arial", Font.PLAIN, 12));
        carTable.setGridColor(new Color(237, 242, 249));
        carTable.setSelectionBackground(new Color(190, 227, 248));
        carTable.setShowVerticalLines(false);
        carTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = carTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(UIUtils.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 33));
        header.setReorderingAllowed(false);

        // Set preferred column widths
        int[] widths = {70, 100, 130, 110, 150, 100};
        for (int i = 0; i < widths.length; i++) {
            carTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Add selection listener to update the details/image sidebar dynamically
        carTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Car selected = getSelectedCarSilently();
                updateDetailPanel(selected);
            }
        });

        refreshTable(); // Populate table on load
        return new JScrollPane(carTable);
    }

    /**
     * Builds the bottom action button bar (Admin only).
     * Contains: Add, Edit, Delete, Rent, Return, Rental History.
     */
    private JPanel buildActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        p.setBackground(UIUtils.BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));

        // --- Add Vehicle (opens popup form) ---
        JButton btnAdd = new JButton("+ Add Vehicle");
        UIUtils.styleButton(btnAdd, UIUtils.SUCCESS);
        btnAdd.setPreferredSize(new Dimension(130, 34));
        btnAdd.addActionListener(e -> handleAdd());
        p.add(btnAdd);

        // --- Edit Selected ---
        JButton btnEdit = new JButton("Edit");
        UIUtils.styleButton(btnEdit, UIUtils.ACCENT);
        btnEdit.setPreferredSize(new Dimension(80, 34));
        btnEdit.addActionListener(e -> handleEdit());
        p.add(btnEdit);

        // --- Delete Selected ---
        JButton btnDelete = new JButton("Delete");
        UIUtils.styleButton(btnDelete, UIUtils.DANGER);
        btnDelete.setPreferredSize(new Dimension(80, 34));
        btnDelete.addActionListener(e -> handleDelete());
        p.add(btnDelete);

        // --- Rent Car ---
        JButton btnRent = new JButton("Rent Car");
        UIUtils.styleButton(btnRent, new Color(56, 161, 105));
        btnRent.setPreferredSize(new Dimension(100, 34));
        btnRent.addActionListener(e -> handleRent());
        p.add(btnRent);

        // --- Return Car ---
        JButton btnReturn = new JButton("Return");
        UIUtils.styleButton(btnReturn, new Color(113, 128, 150));
        btnReturn.setPreferredSize(new Dimension(80, 34));
        btnReturn.addActionListener(e -> handleReturn());
        p.add(btnReturn);

        // --- View Rental History (reporting) ---
        JButton btnHistory = new JButton("Rental Logs & Invoices");
        UIUtils.styleButton(btnHistory, UIUtils.PRIMARY);
        btnHistory.setPreferredSize(new Dimension(170, 34));
        btnHistory.addActionListener(e -> handleViewHistory());
        p.add(btnHistory);

        return p;
    }

    // =========================================================
    // Table Data Methods
    // =========================================================

    /**
     * Reloads the table with the complete inventory list.
     * Called after any Add / Edit / Delete / Rent / Return operation.
     */
    public void refreshTable() {
        // 1. Remember selection
        Car selected = getSelectedCarSilently();
        String selectedId = (selected != null) ? selected.getId() : null;

        // 2. Display updated cars list honoring the "Show Available Only" checkbox
        if (chkAvail != null && chkAvail.isSelected()) {
            displayCars(getAvailableCars());
        } else {
            displayCars(inventoryService.getAllCars());
        }

        // 3. Restore selection and hot-reload the preview panel details
        if (selectedId != null) {
            for (int row = 0; row < carTable.getRowCount(); row++) {
                String rowId = carTable.getValueAt(row, 0).toString();
                if (rowId.equals(selectedId)) {
                    carTable.setRowSelectionInterval(row, row);
                    
                    // Force the details panel on the right to show latest refreshed data
                    updateDetailPanel(inventoryService.findCarById(selectedId));
                    return;
                }
            }
        }
        
        // If selection was lost or car deleted, clear details
        updateDetailPanel(null);
    }

    /**
     * Populates the JTable with the given list of cars.
     * Iterates ArrayList&lt;Car&gt; using polymorphism:
     * getCarType() resolves to the correct subclass implementation.
     *
     * @param list the list of cars to display
     */
    private void displayCars(ArrayList<Car> list) {
        tableModel.setRowCount(0);
        for (Car c : list) {
            /*
             * Determine a human friendly status string for the table.
             * Logic order (priority):
             * 1) If InventoryService reports the car not available today,
             *    try to find the active rental and show Overdue/Blocked states.
             * 2) If InventoryService reports available but there is a future
             *    booking, show "Available (Booked from ...)" so users know
             *    it is reserved in the future.
             */
            String statusStr = "Available";
            if (!inventoryService.isCarAvailable(c.getId())) {
                Rental activeRental = inventoryService.findActiveRentalByCarId(c.getId());
                if (activeRental != null) {
                    if (activeRental.isOverdue()) {
                        Rental blocked = inventoryService.findBlockedFutureBooking(c.getId(), activeRental);
                        if (blocked != null) {
                            statusStr = "Conflict! Blocked " + blocked.getCustomerName() + " (" + blocked.getRentalDate() + ")";
                        } else {
                            statusStr = "Overdue (Return: " + activeRental.getReturnDate() + ")";
                        }
                    } else {
                        statusStr = "Rented Out";
                    }
                } else {
                    statusStr = "Rented Out";
                }
            } else {
                Rental activeRental = inventoryService.findActiveRentalByCarId(c.getId());
                if (activeRental != null) {
                    statusStr = "Available (Booked from " + activeRental.getRentalDate() + ")";
                }
            }
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getBrand(),
                    c.getModel(),
                    String.format("RM %.2f", c.getPricePerDay()),
                    c.getCarType(),                             // Polymorphism
                    statusStr
            });
        }
    }

    /** Returns only the available cars from the full inventory. */
    private ArrayList<Car> getAvailableCars() {
        ArrayList<Car> available = new ArrayList<>();
        for (Car car : inventoryService.getAllCars()) {
            if (inventoryService.isCarAvailable(car.getId())) available.add(car);
        }
        return available;
    }

    // =========================================================
    // Search
    // =========================================================

    /** Filters the table by the search keyword (brand or model). */
    private void handleSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) { refreshTable(); return; }
        displayCars(inventoryService.searchCars(kw));
    }

    // =========================================================
    // Action Handlers (Admin Only)
    // =========================================================

    /**
     * Retrieves the Car object corresponding to the selected table row.
     * Shows a warning if no row is selected.
     *
     * @return selected Car, or null if none selected
     */
    private Car getSelectedCar() {
        int row = carTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a car from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        return inventoryService.findCarById(id);
    }

    /**
     * Add – opens the CarFormDialog popup in Add mode.
     * Car ID is auto-generated inside the dialog.
     */
    private void handleAdd() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        CarFormDialog dialog = new CarFormDialog(parent, inventoryService, fileService, this);
        dialog.setVisible(true);
        // Table is refreshed inside the dialog after saving
    }

    /**
     * Edit – opens the CarFormDialog popup in Edit mode with the
     * selected car's data pre-populated.
     */
    private void handleEdit() {
        Car car = getSelectedCar();
        if (car == null) return;
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        CarFormDialog dialog = new CarFormDialog(parent, inventoryService, fileService, this, car);
        dialog.setVisible(true);
    }

    /**
     * Delete – removes the selected car after confirmation.
     * Prevents deletion of cars that are currently rented out.
     */
    private void handleDelete() {
        Car car = getSelectedCar();
        if (car == null) return;

        if (inventoryService.hasActiveOrBookedRentals(car.getId())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this vehicle because it has active rentals or upcoming reservations.\n"
                    + "Cancel or complete the bookings first.",
                    "Delete Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete " + car.getBrand() + " " + car.getModel()
                + " (" + car.getId() + ")?\n\nThis action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            inventoryService.removeCar(car.getId());
            fileService.saveInventory(inventoryService.getAllCars());
            refreshTable();
            JOptionPane.showMessageDialog(this,
                    "Vehicle removed from fleet.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Rent – opens the RentalDialog for the selected available car.
     * Polymorphism: RentalDialog calls car.calculateRentalCost() which
     * executes the correct subclass logic at runtime.
     */
    private void handleRent() {
        Car car = getSelectedCar();
        if (car == null) return;

        if (!inventoryService.isCarAvailable(car.getId())) {
            JOptionPane.showMessageDialog(this,
                    "This car is already rented out.\n"
                    + "Select an available car to process a new rental.",
                    "Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        RentalDialog dialog = new RentalDialog(parent, car, inventoryService, fileService);
        dialog.setVisible(true);
        refreshTable();
    }

    /**
     * Return – marks a rented car as available again and marks rental COMPLETED.
     */
    private void handleReturn() {
        Car car = getSelectedCar();
        if (car == null) return;

        if (car.isAvailable()) {
            JOptionPane.showMessageDialog(this,
                    "This car is not currently rented out.",
                    "Not Rented", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark " + car.getBrand() + " " + car.getModel() + " as returned?",
                "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            car.setAvailable(true);
            
            // Mark the active rental as completed
            Rental activeRental = inventoryService.findActiveRentalByCarId(car.getId());
            if (activeRental != null) {
                activeRental.setCompleted();
            }
            
            fileService.saveInventory(inventoryService.getAllCars());
            fileService.saveRentals(inventoryService.getRentalHistory());
            refreshTable();
            JOptionPane.showMessageDialog(this,
                    "Car returned and marked as available.",
                    "Returned", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * View Rental History – opens the RentalHistoryDialog to show
     * all customer rental records (reporting bonus feature).
     */
    private void handleViewHistory() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        RentalHistoryDialog dialog = new RentalHistoryDialog(parent, inventoryService);
        dialog.setVisible(true);
    }

    /** Builds the premium side details and image viewer panel. */
    private JPanel buildDetailPanel() {
        detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBackground(UIUtils.CARD);
        detailPanel.setPreferredSize(new Dimension(210, 0));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        lblDetailTitle = UIUtils.boldLabel("No Vehicle Selected");
        lblDetailTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblDetailTitle.setForeground(UIUtils.PRIMARY);
        lblDetailTitle.setHorizontalAlignment(SwingConstants.CENTER);

        lblDetailImage = new JLabel("No Image", SwingConstants.CENTER);
        lblDetailImage.setPreferredSize(new Dimension(180, 120));
        lblDetailImage.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        lblDetailImage.setBackground(new Color(247, 250, 252));
        lblDetailImage.setOpaque(true);

        lblDetailSpecs = new JLabel("Select a vehicle to view its image and details.");
        lblDetailSpecs.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDetailSpecs.setForeground(new Color(74, 85, 104));

        JPanel specsWrapper = new JPanel(new BorderLayout(8, 8));
        specsWrapper.setBackground(UIUtils.CARD);
        specsWrapper.add(lblDetailImage, BorderLayout.NORTH);
        specsWrapper.add(lblDetailSpecs, BorderLayout.CENTER);

        detailPanel.add(lblDetailTitle, BorderLayout.NORTH);
        detailPanel.add(specsWrapper, BorderLayout.CENTER);

        return detailPanel;
    }

    /** Retrieves the selected car row without prompting warning pop-ups. */
    private Car getSelectedCarSilently() {
        int row = carTable.getSelectedRow();
        if (row == -1) return null;
        String id = tableModel.getValueAt(row, 0).toString();
        return inventoryService.findCarById(id);
    }

    private void updateDetailPanel(Car car) {
        // Prevent NullPointerExceptions if this is called during initial load before UI panels are constructed
        if (lblDetailImage == null || lblDetailTitle == null || lblDetailSpecs == null) {
            return;
        }

        if (car == null) {
            lblDetailImage.setIcon(null);
            lblDetailImage.setText("No Image");
            lblDetailTitle.setText("No Vehicle Selected");
            lblDetailSpecs.setText("Select a vehicle to view its image and details.");
            return;
        }

        lblDetailTitle.setText(car.getBrand() + " " + car.getModel());
        
        String statusStr = "Available";
        if (!inventoryService.isCarAvailable(car.getId())) {
            Rental activeRental = inventoryService.findActiveRentalByCarId(car.getId());
            if (activeRental != null) {
                if (activeRental.isOverdue()) {
                    Rental blocked = inventoryService.findBlockedFutureBooking(car.getId(), activeRental);
                    if (blocked != null) {
                        statusStr = "<font color='red'><b>Conflict! Overdue blocks " + blocked.getCustomerName() + " (" + blocked.getRentalDate() + ")</b></font>";
                    } else {
                        statusStr = "OVERDUE (Due: " + activeRental.getReturnDate() + ")";
                    }
                } else {
                    statusStr = "Rented Out";
                }
            } else {
                statusStr = "Rented Out";
            }
        } else {
            Rental activeRental = inventoryService.findActiveRentalByCarId(car.getId());
            if (activeRental != null) {
                statusStr = "Available (Booked from " + activeRental.getRentalDate() + ")";
            }
        }
        
        // Find all active/booked future rentals for this car to list them in the details sidebar
        ArrayList<Rental> futureBookings = new ArrayList<>();
        for (Rental r : inventoryService.getRentalHistory()) {
            if (r.getCarId().equals(car.getId()) && (r.isBooked() || (r.isActive() && LocalDate.parse(r.getRentalDate()).isAfter(LocalDate.now())))) {
                futureBookings.add(r);
            }
        }
        
        StringBuilder bookingsHtml = new StringBuilder();
        if (!futureBookings.isEmpty()) {
            bookingsHtml.append("<br><br><b>Upcoming Bookings:</b>");
            for (Rental fb : futureBookings) {
                bookingsHtml.append("<br>• ")
                        .append(fb.getCustomerName())
                        .append("<br>  (")
                        .append(fb.getRentalDate())
                        .append(" to ")
                        .append(fb.getReturnDate())
                        .append(")");
            }
        } else {
            bookingsHtml.append("<br><br><i>No future reservations.</i>");
        }

        lblDetailSpecs.setText("<html><b>Type:</b> " + car.getCarType()
                + "<br><b>Rate:</b> RM " + String.format("%.2f", car.getPricePerDay()) + " / Day"
                + "<br><b>Surcharge:</b> " + car.getSurchargeInfo()
                + "<br><b>Status:</b> " + statusStr 
                + bookingsHtml.toString() + "</html>");

        lblDetailImage.setText("Loading Image...");
        lblDetailImage.setIcon(null);

        // Load image asynchronously so UI thread is never blocked
        new Thread(() -> {
            try {
                java.net.URL url = java.net.URI.create(car.getImageUrl()).toURL();
                java.awt.Image img = javax.imageio.ImageIO.read(url);
                if (img != null) {
                    java.awt.Image scaled = img.getScaledInstance(180, 120, java.awt.Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaled);
                    SwingUtilities.invokeLater(() -> {
                        // Check if selection hasn't changed since starting download
                        Car current = getSelectedCarSilently();
                        if (current != null && current.getId().equals(car.getId())) {
                            lblDetailImage.setText("");
                            lblDetailImage.setIcon(icon);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> lblDetailImage.setText("No image preview"));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> lblDetailImage.setText("No image preview"));
            }
        }).start();
    }
}

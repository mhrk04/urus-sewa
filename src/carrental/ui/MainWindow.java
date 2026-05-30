package carrental.ui;

import carrental.service.FileService;
import carrental.service.InventoryService;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Main application window (JFrame) for the UrusSewa Car Rental System.
 *
 * Acts as the top-level container, composing:
 *   - A header panel (title + access level indicator)
 *   - A CarTablePanel (table, search, action buttons)
 *   - A JMenuBar with User and About menus
 *
 * The Add/Edit forms are popup dialogs (CarFormDialog), not embedded
 * in the main window. This keeps the layout clean and the form only
 * appears when the user clicks the relevant button.
 *
 * Uses BorderLayout:
 *   NORTH  → header panel
 *   CENTER → table panel
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class MainWindow extends JFrame {

    private final boolean          isAdmin;
    private final InventoryService inventoryService;
    private final FileService      fileService;

    private CarTablePanel tablePanel;
    private long          lastCarFileTime = 0;
    private long          lastRentalFileTime = 0;

    /**
     * Constructs the main window for the given access level.
     *
     * @param isAdmin true for full Admin access; false for Guest (read-only)
     */
    public MainWindow(boolean isAdmin) {
        this.isAdmin          = isAdmin;
        this.fileService      = new FileService();
        this.inventoryService = new InventoryService();

        loadData();
        buildWindow();
        startFileWatcher();
    }

    // =========================================================
    // Initialisation
    // =========================================================

    /**
     * Loads car inventory and rental history from file.
     * Seeds default data on first run when no file exists.
     */
    private void loadData() {
        ArrayList<carrental.model.Car> cars = fileService.loadInventory();
        if (cars.isEmpty()) {
            // First-time run: seed default fleet and save to file
            cars = fileService.getDefaultInventory();
            fileService.saveInventory(cars);
        }
        inventoryService.setInventory(cars);
        inventoryService.setRentalHistory(fileService.loadRentals());
        
        // Auto-transition BOOKED to ACTIVE bookings if their start time has arrived
        inventoryService.checkAndTransitionBookings(fileService);
    }

    /** Builds and assembles all window components. */
    private void buildWindow() {
        String role = isAdmin ? "Administrator" : "Guest (View Only)";
        setTitle("UrusSewa – Car Rental Management System  [" + role + "]");
        setSize(820, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG);
        setLayout(new BorderLayout(0, 0));

        setJMenuBar(buildMenuBar());
        add(buildHeaderPanel(), BorderLayout.NORTH);

        // Table panel (CENTER) – always visible
        tablePanel = new CarTablePanel(isAdmin, inventoryService, fileService);
        add(tablePanel, BorderLayout.CENTER);
    }

    // =========================================================
    // Header Panel
    // =========================================================

    /**
     * Builds the top navy-blue header showing the system title
     * and the current access level (Admin / Guest).
     */
    // =========================================================
    // Header Panel
    // =========================================================

    /**
     * Returns an animated header panel with a horizontal moving car.
     */
    private JPanel buildHeaderPanel() {
        return new AnimatedCarHeaderPanel(isAdmin);
    }

    /**
     * A premium custom panel that renders the top dark navy banner, the system title,
     * and a dynamic vector car that drives smoothly across the screen at 60fps.
     */
    private class AnimatedCarHeaderPanel extends JPanel {
        private int carX = -100;
        private final Timer timer;
        private final JLabel titleLabel;
        private final JLabel statusLabel;

        public AnimatedCarHeaderPanel(boolean isAdmin) {
            setLayout(new BorderLayout());
            setBackground(UIUtils.PRIMARY);
            setPreferredSize(new Dimension(820, 60));
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

            // Title label with clean shadow
            titleLabel = new JLabel("URUSSEWA – CAR RENTAL MANAGEMENT SYSTEM");
            titleLabel.setForeground(new Color(255, 255, 255, 210));
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            add(titleLabel, BorderLayout.WEST);

            Color roleColor  = isAdmin ? new Color(72, 187, 120) : new Color(237, 137, 54);
            String roleLabel = isAdmin ? "ADMINISTRATOR" : "GUEST (READ-ONLY)";
            statusLabel = new JLabel("Access: " + roleLabel);
            statusLabel.setForeground(roleColor);
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            add(statusLabel, BorderLayout.EAST);

            // Animate at ~60 FPS (16ms per frame)
            timer = new Timer(16, e -> {
                carX += 2; // Driving speed
                if (carX > getWidth() + 100) {
                    carX = -120; // Wrap around to the left
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int carY = getHeight() / 2 - 10;
            int carWidth = 70;
            int carHeight = 18;

            // Draw a subtle shadow under the moving vehicle
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(carX + 5, carY + carHeight - 4, carWidth - 10, 6);

            // Speed lines trailing behind the vehicle
            g2.setColor(new Color(255, 255, 255, 70));
            g2.drawLine(carX - 15, carY + 8, carX - 4, carY + 8);
            g2.drawLine(carX - 25, carY + 11, carX - 6, carY + 11);
            g2.drawLine(carX - 18, carY + 14, carX - 5, carY + 14);

            // Car main body chassis (sleek turquoise sports car)
            g2.setColor(new Color(56, 178, 172)); // Vibrant teal
            g2.fillRoundRect(carX, carY + 6, carWidth, carHeight - 6, 8, 8);

            // Cabin / Roof (dark tinted sports glass)
            g2.setColor(new Color(45, 55, 72));
            g2.fillRoundRect(carX + 15, carY, 35, 10, 6, 6);

            // Window details (light blue highlights)
            g2.setColor(new Color(226, 232, 240, 180));
            g2.fillRoundRect(carX + 18, carY + 2, 12, 6, 2, 2);
            g2.fillRoundRect(carX + 33, carY + 2, 14, 6, 2, 2);

            // Black rubber tyres
            g2.setColor(new Color(26, 32, 44));
            g2.fillOval(carX + 12, carY + carHeight - 6, 12, 12);
            g2.fillOval(carX + carWidth - 24, carY + carHeight - 6, 12, 12);

            // Silver wheel hubs
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(carX + 15, carY + carHeight - 3, 6, 6);
            g2.fillOval(carX + carWidth - 21, carY + carHeight - 3, 6, 6);

            // Headlight (yellow glow)
            g2.setColor(new Color(255, 236, 153));
            g2.fillRoundRect(carX + carWidth - 4, carY + 8, 4, 4, 2, 2);
            
            // Taillight (red glow)
            g2.setColor(new Color(245, 101, 101));
            g2.fillRect(carX, carY + 8, 3, 4);
        }
    }

    // =========================================================
    // Menu Bar
    // =========================================================

    /**
     * Builds the JMenuBar with User and About menus.
     * Uses JMenu / JMenuItem / addSeparator() (Module 8 – Menu Construction).
     */
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(UIUtils.CARD);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));

        // --- User Menu ---
        JMenu userMenu = new JMenu("User");
        userMenu.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem profileItem = new JMenuItem("View Access Profile");
        profileItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Current Role: " + (isAdmin ? "System Administrator (Full Access)"
                                            : "Guest User (Read-Only)"),
                "Access Profile", JOptionPane.INFORMATION_MESSAGE));

        JMenuItem logoutItem = new JMenuItem("Log Out / Switch User");
        logoutItem.addActionListener(e -> { dispose(); new LoginDialog().setVisible(true); });

        userMenu.add(profileItem);
        userMenu.addSeparator();
        userMenu.add(logoutItem);
        bar.add(userMenu);

        // --- Data Menu (Admin Only) ---
        if (isAdmin) {
            JMenu dataMenu = new JMenu("Data");
            dataMenu.setFont(new Font("Arial", Font.PLAIN, 13));

            JMenuItem importItem = new JMenuItem("Import Cars from File...");
            importItem.addActionListener(e -> handleImportCars());

            dataMenu.add(importItem);
            bar.add(dataMenu);
        }

        // --- About Menu ---
        JMenu aboutMenu = new JMenu("About");
        aboutMenu.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem aboutItem = new JMenuItem("About UrusSewa");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "UrusSewa – Car Rental Management System\n"
                + "Version 4.1  |  OOP Assignment – Group 11\n"
                + "UniKL MIIT  |  Course Code: ISB16003\n\n"
                + "Features: Add · Edit · Delete · Display · Search · Rent\n"
                + "Car Types: Petrol · Electric · Hybrid",
                "About", JOptionPane.INFORMATION_MESSAGE));
        aboutMenu.add(aboutItem);
        bar.add(aboutMenu);

        return bar;
    }

    // =========================================================
    // Menu Actions
    // =========================================================

    /**
     * Opens a file chooser to select a CSV file and appends its valid
     * records to the current inventory.
     */
    private void handleImportCars() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Car Data File (.txt)");
        int result = chooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            ArrayList<carrental.model.Car> imported = fileService.loadInventoryFromFile(file);
            
            if (imported.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "No valid car records found in selected file.", 
                        "Import Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                int successCount = 0;
                ArrayList<String> duplicateIds = new ArrayList<>();
                
                for (carrental.model.Car c : imported) {
                    // Check if car with same ID exists to avoid duplicates
                    if (inventoryService.findCarById(c.getId()) == null) {
                        inventoryService.addCar(c);
                        successCount++;
                    } else {
                        duplicateIds.add(c.getId());
                    }
                }
                
                if (successCount > 0) {
                    fileService.saveInventory(inventoryService.getAllCars());
                    tablePanel.refreshTable();
                }
                
                if (!duplicateIds.isEmpty()) {
                    StringBuilder warningMsg = new StringBuilder();
                    warningMsg.append("Warning: The following car ID(s) already exist and were skipped to avoid overwriting current records:\n");
                    for (String id : duplicateIds) {
                        warningMsg.append("- ").append(id).append("\n");
                    }
                    if (successCount > 0) {
                        warningMsg.append("\nSuccessfully imported the remaining ").append(successCount).append(" car(s).");
                    } else {
                        warningMsg.append("\nNo new cars were imported.");
                    }
                    
                    JOptionPane.showMessageDialog(this, 
                            warningMsg.toString(), 
                            "Duplicate IDs Warning", 
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                            successCount + " car(s) parsed and imported successfully.", 
                            "Import Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    /**
     * Starts a background polling thread (via javax.swing.Timer) to check for external
     * edits to car_inventory.txt or rental_history.txt every 2 seconds, and reloads
     * the table view dynamically if changes are detected.
     */
    private void startFileWatcher() {
        java.io.File carFile = new java.io.File(FileService.CAR_FILE);
        java.io.File rentalFile = new java.io.File(FileService.RENTAL_FILE);

        lastCarFileTime = carFile.lastModified();
        lastRentalFileTime = rentalFile.lastModified();

        Timer timer = new Timer(2000, e -> {
            boolean changed = false;

            // Periodically check if any BOOKED reservation has reached its start date
            if (inventoryService.checkAndTransitionBookings(fileService)) {
                changed = true;
                // Re-sync file timestamps to prevent redundant loading
                lastCarFileTime = carFile.lastModified();
                lastRentalFileTime = rentalFile.lastModified();
            }

            if (carFile.exists() && carFile.lastModified() > lastCarFileTime) {
                lastCarFileTime = carFile.lastModified();
                changed = true;
            }
            if (rentalFile.exists() && rentalFile.lastModified() > lastRentalFileTime) {
                lastRentalFileTime = rentalFile.lastModified();
                changed = true;
            }

            if (changed) {
                // Reload lists from disk
                ArrayList<carrental.model.Car> cars = fileService.loadInventory();
                ArrayList<carrental.model.Rental> rentals = fileService.loadRentals();

                // Update in-memory collections
                inventoryService.setInventory(cars);
                inventoryService.setRentalHistory(rentals);

                // Refresh the table panel
                tablePanel.refreshTable();
            }
        });
        timer.start();
    }
}

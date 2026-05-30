package carrental.ui;

import carrental.model.Car;
import carrental.model.Rental;
import carrental.service.InventoryService;
import carrental.service.InvoiceService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;

/**
 * Modal dialog displaying rental records with Active / Completed tabs.
 *
 * Active tab   → shows cars currently rented out (not yet returned)
 * Completed tab → shows past rentals (car has been returned)
 *
 * This is a "reporting" bonus feature as described in the rubric.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class RentalHistoryDialog extends JDialog {

    private final InventoryService inventoryService;
    private JTabbedPane tabs;
    private JTable tableActive;
    private JTable tableCompleted;

    /**
     * Constructs the rental history dialog with tabs.
     *
     * @param parent           parent JFrame for modal positioning
     * @param inventoryService service to retrieve rental records
     */
    public RentalHistoryDialog(JFrame parent, InventoryService inventoryService) {
        super(parent, "Rental Records – Customer Management", true);
        this.inventoryService = inventoryService;
        buildUI();
        setSize(750, 460);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    // =========================================================
    // UI Construction
    // =========================================================

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIUtils.BG);

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTabbedPane(),  BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    /** Header with title and total counts. */
    private JPanel buildHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIUtils.PRIMARY);
        p.setPreferredSize(new Dimension(getWidth(), 45));
        p.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JLabel title = new JLabel("RENTAL RECORDS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(title, BorderLayout.WEST);

        int active    = inventoryService.getActiveRentals().size();
        int completed = inventoryService.getCompletedRentals().size();
        JLabel countLabel = new JLabel(active + " active  |  " + completed + " completed");
        countLabel.setForeground(new Color(160, 210, 255));
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        p.add(countLabel, BorderLayout.EAST);

        return p;
    }

    /**
     * Builds a JTabbedPane with two tabs:
     *   - Active Rentals (cars currently rented out)
     *   - Completed Rentals (past, returned)
     */
    private JTabbedPane buildTabbedPane() {
        tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 12));
        tabs.setBackground(UIUtils.BG);

        tableActive = buildTableForList(inventoryService.getActiveRentals(), true);
        tableCompleted = buildTableForList(inventoryService.getCompletedRentals(), false);

        tabs.addTab("Active Rentals",    wrapInPanel(tableActive, inventoryService.getActiveRentals().isEmpty(), true));
        tabs.addTab("Completed Rentals", wrapInPanel(tableCompleted, inventoryService.getCompletedRentals().isEmpty(), false));

        return tabs;
    }
    
    private JPanel wrapInPanel(JTable table, boolean isEmpty, boolean isActive) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIUtils.BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        if (isEmpty) {
            String msg = isActive ? "No cars currently rented out."
                                  : "No completed rental records.";
            JLabel emptyLabel = new JLabel(msg, SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 13));
            emptyLabel.setForeground(new Color(113, 128, 150));
            p.add(emptyLabel, BorderLayout.CENTER);
        } else {
            p.add(new JScrollPane(table), BorderLayout.CENTER);
        }
        return p;
    }

    /**
     * Builds a JTable for the given rental list.
     *
     * @param rentals  the list of Rental objects to display
     * @param isActive true if showing active rentals (different styling)
     * @return JTable configured
     */
    private JTable buildTableForList(ArrayList<Rental> rentals, boolean isActive) {
        String[] columns = {"Rental ID", "Car ID", "Customer Name", "IC / Passport",
                            "Phone", "Days", "Total (RM)", "Date", "Status"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        // Populate table rows from ArrayList
        for (Rental r : rentals) {
            String statusStr = r.getStatus();
            if (r.isOverdue()) {
                statusStr = "OVERDUE (Due: " + r.getReturnDate() + ")";
            }
            model.addRow(new Object[]{
                    r.getRentalId(),
                    r.getCarId(),
                    r.getCustomerName(),
                    r.getCustomerIC(),
                    r.getCustomerPhone(),
                    r.getRentalDays(),
                    String.format("RM %.2f", r.getTotalCost()),
                    r.getRentalDate(),
                    statusStr
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setGridColor(new Color(237, 242, 249));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Colour the selection based on tab type
        table.setSelectionBackground(isActive
                ? new Color(198, 246, 213)     // Green tint for active
                : new Color(190, 227, 248));   // Blue tint for completed

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(isActive ? UIUtils.SUCCESS : UIUtils.ACCENT);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {65, 55, 120, 110, 95, 40, 85, 85, 75};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        return table;
    }

    /** Footer with buttons. */
    private JPanel buildFooterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        p.setBackground(UIUtils.BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));

        JButton btnInvoice = new JButton("View Invoice (PDF)");
        UIUtils.styleButton(btnInvoice, UIUtils.SUCCESS);
        btnInvoice.setPreferredSize(new Dimension(150, 32));
        btnInvoice.addActionListener(e -> handleViewInvoice());
        
        JButton btnClose = new JButton("Close");
        UIUtils.styleButton(btnClose, new Color(113, 128, 150));
        btnClose.setPreferredSize(new Dimension(90, 32));
        btnClose.addActionListener(e -> dispose());
        
        p.add(btnInvoice);
        p.add(btnClose);

        return p;
    }
    
    // =========================================================
    // Invoice Handling
    // =========================================================
    
    /**
     * Opens the invoice PDF for the selected rental record.
     * Regenerates the invoice if it doesn't exist on disk.
     */
    private void handleViewInvoice() {
        JTable activeTable = (tabs.getSelectedIndex() == 0) ? tableActive : tableCompleted;
        int row = activeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a rental record from the table first.", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String rentalId = activeTable.getValueAt(row, 0).toString();
        String pdfPath = "invoices/" + rentalId + "_invoice.pdf";
        java.io.File file = new java.io.File(pdfPath);
        
        if (file.exists()) {
            openPdfFile(file);
        } else {
            // Attempt to regenerate
            Rental rental = findRentalById(rentalId);
            if (rental != null) {
                Car car = inventoryService.findCarById(rental.getCarId());
                if (car != null) {
                    InvoiceService invService = new InvoiceService();
                    String newPath = invService.generateInvoice(rental, car);
                    if (newPath != null) {
                        openPdfFile(new java.io.File(newPath));
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(this, 
                    "Invoice PDF not found and car data missing to regenerate it.", 
                    "Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openPdfFile(java.io.File file) {
        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Could not open invoice automatically.\n"
                    + "Path: " + file.getAbsolutePath(), 
                    "Open Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private Rental findRentalById(String id) {
        for (Rental r : inventoryService.getRentalHistory()) {
            if (r.getRentalId().equals(id)) return r;
        }
        return null;
    }
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UrusSewaApp {
    private final FleetInventory inventory = new FleetInventory();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Type", "Brand", "Model", "Plate", "Daily Rate", "Available"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTextField brandField;
    private JTextField modelField;
    private JTextField plateField;
    private JTextField dailyRateField;
    private JComboBox<String> typeBox;
    private JCheckBox availableBox;
    private JTextField searchField;
    private JTable table;
    private JTextArea inventoryDisplay;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UrusSewaApp app = new UrusSewaApp();
            if (!app.showLogin()) {
                return;
            }
            app.createAndShowGui();
        });
    }

    private boolean showLogin() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] message = {
                "Username:", userField,
                "Password:", passField
        };

        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                "Staff Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return false;
        }

        boolean authenticated = StaffLoginService.authenticate(
                userField.getText().trim(),
                new String(passField.getPassword())
        );

        if (!authenticated) {
            JOptionPane.showMessageDialog(null, "Unauthorized access.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void createAndShowGui() {
        JFrame frame = new JFrame("UrusSewa Fleet Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 650);
        frame.setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(3, 5, 8, 8));
        brandField = new JTextField();
        modelField = new JTextField();
        plateField = new JTextField();
        dailyRateField = new JTextField();
        typeBox = new JComboBox<>(new String[]{"Standard", "Premium"});
        availableBox = new JCheckBox("Available", true);

        form.add(new JLabel("Brand"));
        form.add(new JLabel("Model"));
        form.add(new JLabel("Plate"));
        form.add(new JLabel("Daily Rate"));
        form.add(new JLabel("Type"));

        form.add(brandField);
        form.add(modelField);
        form.add(plateField);
        form.add(dailyRateField);
        form.add(typeBox);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear");

        form.add(availableBox);
        form.add(addButton);
        form.add(updateButton);
        form.add(deleteButton);
        form.add(clearButton);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchField = new JTextField();
        JButton searchButton = new JButton("Search Brand/Model");
        JButton showAllButton = new JButton("Show All");
        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchButtons.add(searchButton);
        searchButtons.add(showAllButton);

        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);

        inventoryDisplay = new JTextArea(9, 60);
        inventoryDisplay.setEditable(false);
        inventoryDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane displayScroll = new JScrollPane(inventoryDisplay);
        displayScroll.setBorder(BorderFactory.createTitledBorder("Formatted Inventory"));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(form, BorderLayout.NORTH);
        top.add(searchPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, displayScroll);
        split.setResizeWeight(0.65);

        frame.setLayout(new BorderLayout(10, 10));
        frame.add(top, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);

        addButton.addActionListener(e -> addCar());
        updateButton.addActionListener(e -> updateSelectedCar());
        deleteButton.addActionListener(e -> deleteSelectedCar());
        clearButton.addActionListener(e -> clearInputs());
        searchButton.addActionListener(e -> refreshTable(inventory.searchByBrandOrModel(searchField.getText().trim())));
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(inventory.getAll());
        });
        table.getSelectionModel().addListSelectionListener(e -> populateSelectedCar());

        seedData();
        refreshTable(inventory.getAll());
        frame.setVisible(true);
    }

    private void seedData() {
        inventory.addCar(new StandardCar("Perodua", "Bezza", "BZZ-1234", 120.0, true));
        inventory.addCar(new PremiumCar("Toyota", "Vellfire", "VLF-8888", 650.0, false));
    }

    private void addCar() {
        FleetCar newCar = buildCarFromInputs();
        if (newCar == null) {
            return;
        }
        String error = inventory.addCar(newCar);
        if (error != null) {
            showError(error);
            return;
        }
        refreshTable(inventory.getAll());
        clearInputs();
    }

    private void updateSelectedCar() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a car to update.");
            return;
        }
        String currentPlate = tableModel.getValueAt(selected, 3).toString();
        FleetCar updated = buildCarFromInputs();
        if (updated == null) {
            return;
        }
        String error = inventory.updateCar(currentPlate, updated);
        if (error != null) {
            showError(error);
            return;
        }
        refreshTable(inventory.getAll());
        clearInputs();
    }

    private void deleteSelectedCar() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a car to delete.");
            return;
        }
        String plate = tableModel.getValueAt(selected, 3).toString();
        inventory.deleteCar(plate);
        refreshTable(inventory.getAll());
        clearInputs();
    }

    private void populateSelectedCar() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return;
        }
        typeBox.setSelectedItem(tableModel.getValueAt(selected, 0).toString());
        brandField.setText(tableModel.getValueAt(selected, 1).toString());
        modelField.setText(tableModel.getValueAt(selected, 2).toString());
        plateField.setText(tableModel.getValueAt(selected, 3).toString());
        dailyRateField.setText(tableModel.getValueAt(selected, 4).toString().replace("RM ", ""));
        availableBox.setSelected("Yes".equals(tableModel.getValueAt(selected, 5).toString()));
    }

    private FleetCar buildCarFromInputs() {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String plate = plateField.getText().trim().toUpperCase(Locale.ROOT);
        String rateText = dailyRateField.getText().trim();
        boolean available = availableBox.isSelected();

        if (brand.isEmpty() || model.isEmpty() || plate.isEmpty() || rateText.isEmpty()) {
            showError("All fields are required.");
            return null;
        }

        double rate;
        try {
            rate = Double.parseDouble(rateText);
        } catch (NumberFormatException ex) {
            showError("Daily rate must be a valid number.");
            return null;
        }

        if (rate <= 0) {
            showError("Daily rate must be greater than 0.");
            return null;
        }

        String type = String.valueOf(typeBox.getSelectedItem());
        if ("Premium".equals(type)) {
            return new PremiumCar(brand, model, plate, rate, available);
        }
        return new StandardCar(brand, model, plate, rate, available);
    }

    private void refreshTable(List<FleetCar> cars) {
        tableModel.setRowCount(0);
        for (FleetCar car : cars) {
            tableModel.addRow(new Object[]{
                    car.getType(),
                    car.getBrand(),
                    car.getModel(),
                    car.getPlate(),
                    String.format(Locale.ROOT, "RM %.2f", car.getDailyRate()),
                    car.isAvailable() ? "Yes" : "No"
            });
        }
        inventoryDisplay.setText(inventory.getFormattedInventory());
    }

    private void clearInputs() {
        brandField.setText("");
        modelField.setText("");
        plateField.setText("");
        dailyRateField.setText("");
        typeBox.setSelectedIndex(0);
        availableBox.setSelected(true);
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    abstract static class FleetCar {
        private final String brand;
        private final String model;
        private final String plate;
        private final double dailyRate;
        private final boolean available;

        protected FleetCar(String brand, String model, String plate, double dailyRate, boolean available) {
            this.brand = brand;
            this.model = model;
            this.plate = plate;
            this.dailyRate = dailyRate;
            this.available = available;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public String getPlate() {
            return plate;
        }

        public double getDailyRate() {
            return dailyRate;
        }

        public boolean isAvailable() {
            return available;
        }

        public abstract String getType();

        public abstract double calculateRentalCost(int days);
    }

    static class StandardCar extends FleetCar {
        StandardCar(String brand, String model, String plate, double dailyRate, boolean available) {
            super(brand, model, plate, dailyRate, available);
        }

        @Override
        public String getType() {
            return "Standard";
        }

        @Override
        public double calculateRentalCost(int days) {
            return Math.max(days, 0) * getDailyRate();
        }
    }

    static class PremiumCar extends FleetCar {
        private static final double PREMIUM_MULTIPLIER = 1.2;

        PremiumCar(String brand, String model, String plate, double dailyRate, boolean available) {
            super(brand, model, plate, dailyRate, available);
        }

        @Override
        public String getType() {
            return "Premium";
        }

        @Override
        public double calculateRentalCost(int days) {
            return Math.max(days, 0) * getDailyRate() * PREMIUM_MULTIPLIER;
        }
    }

    static class FleetInventory {
        private final List<FleetCar> cars = new ArrayList<>();

        public List<FleetCar> getAll() {
            return new ArrayList<>(cars);
        }

        public String addCar(FleetCar car) {
            if (existsByPlate(car.getPlate())) {
                return "Plate already exists.";
            }
            cars.add(car);
            return null;
        }

        public String updateCar(String oldPlate, FleetCar updated) {
            int idx = findIndexByPlate(oldPlate);
            if (idx < 0) {
                return "Car not found.";
            }
            if (!oldPlate.equalsIgnoreCase(updated.getPlate()) && existsByPlate(updated.getPlate())) {
                return "Plate already exists.";
            }
            cars.set(idx, updated);
            return null;
        }

        public void deleteCar(String plate) {
            cars.removeIf(c -> c.getPlate().equalsIgnoreCase(plate));
        }

        public List<FleetCar> searchByBrandOrModel(String keyword) {
            if (keyword == null || keyword.isBlank()) {
                return getAll();
            }
            String k = keyword.toLowerCase(Locale.ROOT);
            List<FleetCar> matches = new ArrayList<>();
            for (FleetCar car : cars) {
                if (car.getBrand().toLowerCase(Locale.ROOT).contains(k)
                        || car.getModel().toLowerCase(Locale.ROOT).contains(k)) {
                    matches.add(car);
                }
            }
            return matches;
        }

        public String getFormattedInventory() {
            if (cars.isEmpty()) {
                return "No inventory records.";
            }

            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-10s %-12s %-12s %-10s %-11s %-12s%n",
                    "Type", "Brand", "Model", "Plate", "Available", "3-Day Rent"));
            sb.append("--------------------------------------------------------------------------\n");
            for (FleetCar car : cars) {
                sb.append(String.format("%-10s %-12s %-12s %-10s %-11s %-12s%n",
                        car.getType(),
                        car.getBrand(),
                        car.getModel(),
                        car.getPlate(),
                        car.isAvailable() ? "Yes" : "No",
                        currency.format(car.calculateRentalCost(3))));
            }
            return sb.toString();
        }

        private boolean existsByPlate(String plate) {
            return findIndexByPlate(plate) >= 0;
        }

        private int findIndexByPlate(String plate) {
            for (int i = 0; i < cars.size(); i++) {
                if (cars.get(i).getPlate().equalsIgnoreCase(plate)) {
                    return i;
                }
            }
            return -1;
        }
    }

    static class StaffLoginService {
        private static final Map<String, String> USERS = new HashMap<>();
        private static final String DEFAULT_STAFF_HASH =
                "0d69d0d2dc8ae1b189139ced4b8466aa469c9070b78f16b74b17142f06995f2f";

        static {
            USERS.put("staff", DEFAULT_STAFF_HASH);
        }

        public static boolean authenticate(String username, String password) {
            String expectedHash = USERS.get(username);
            return expectedHash != null && expectedHash.equals(sha256(password));
        }

        private static String sha256(String input) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(input.getBytes());
                StringBuilder hex = new StringBuilder();
                for (byte b : hash) {
                    hex.append(String.format("%02x", b));
                }
                return hex.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 unavailable", e);
            }
        }
    }
}

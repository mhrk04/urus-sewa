package carrental.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login screen for the UrusSewa system.
 *
 * Provides two entry modes:
 *   1. Admin Sign In – requires username "admin" and password "admin123".
 *      Grants full CRUD access (Add, Edit, Delete, Rent).
 *   2. Guest Mode – bypasses login, grants read-only access (View, Search).
 *
 * Uses ActionListener to handle button click events (Module 8 – Event Handling).
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class LoginDialog extends JFrame {

    private JTextField    usernameField;
    private JPasswordField passwordField;

    /** Hard-coded admin credentials (bonus security feature). */
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    /**
     * Constructs and lays out the login window.
     */
    public LoginDialog() {
        setTitle("UrusSewa – Login Portal");
        setSize(430, 310);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Root panel
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(UIUtils.CARD);
        main.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        main.add(buildTitlePanel(),  BorderLayout.NORTH);
        main.add(buildFormPanel(),   BorderLayout.CENTER);
        main.add(buildButtonPanel(), BorderLayout.SOUTH);

        add(main);
    }

    // =========================================================
    // Panel Builders
    // =========================================================

    /** Builds the title header section. */
    private JPanel buildTitlePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIUtils.CARD);

        JLabel title = new JLabel("UrusSewa Systems", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(UIUtils.PRIMARY);
        p.add(title, BorderLayout.CENTER);

        JLabel subtitle = new JLabel("Fleet Management Portal", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 11));
        subtitle.setForeground(new Color(113, 128, 150));
        p.add(subtitle, BorderLayout.SOUTH);

        return p;
    }

    /** Builds the username / password input form. */
    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 14));
        p.setBackground(UIUtils.CARD);

        p.add(UIUtils.boldLabel("Username:"));
        usernameField = new JTextField();
        UIUtils.styleTextField(usernameField);
        p.add(usernameField);

        p.add(UIUtils.boldLabel("Password:"));
        passwordField = new JPasswordField();
        UIUtils.styleTextField(passwordField);

        // Press Enter to attempt login (convenience)
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) verifyAdminCredentials();
            }
        });
        p.add(passwordField);

        return p;
    }

    /** Builds the Admin Sign In and Guest Mode buttons. */
    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 10));
        p.setBackground(UIUtils.CARD);

        JButton btnLogin = new JButton("Admin Sign In");
        UIUtils.styleButton(btnLogin, UIUtils.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(0, 38));
        btnLogin.addActionListener(e -> verifyAdminCredentials());

        JButton btnGuest = new JButton("Continue as Guest (View Only)  →");
        UIUtils.styleButton(btnGuest, UIUtils.WARNING);
        btnGuest.addActionListener(e -> openAsGuest());

        p.add(btnLogin);
        p.add(btnGuest);
        return p;
    }

    // =========================================================
    // Event Handlers
    // =========================================================

    /**
     * Validates admin credentials.
     * On success: opens MainWindow in Admin mode.
     * On failure: shows error dialog, clears password field.
     */
    private void verifyAdminCredentials() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.equals(ADMIN_USER) && pass.equals(ADMIN_PASS)) {
            dispose();
            new MainWindow(true).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.\nPlease try again.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // Clear password on failed attempt
        }
    }

    /** Bypasses login and opens MainWindow in read-only Guest mode. */
    private void openAsGuest() {
        dispose();
        new MainWindow(false).setVisible(true);
    }
}

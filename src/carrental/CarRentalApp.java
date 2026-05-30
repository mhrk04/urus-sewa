package carrental;

import carrental.ui.LoginDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the UrusSewa Car Rental Management System.
 *
 * Responsibilities:
 *   1. Apply the cross-platform Swing look-and-feel so that button
 *      background colours render correctly on macOS, Windows, and Linux.
 *   2. Launch the LoginDialog on the Swing event dispatch thread (EDT).
 *
 * macOS Note:
 *   By default, macOS overrides JButton.setBackground() and ignores
 *   custom colours. Calling UIManager.setLookAndFeel with the
 *   cross-platform (Metal) L&F restores standard Swing rendering
 *   so all colour styling works as expected on every platform.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class CarRentalApp {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Apply cross-platform look-and-feel BEFORE creating any Swing component.
        // This ensures setBackground() on JButton works on macOS.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // If L&F fails, the app still runs with the default system L&F
            System.err.println("Look-and-feel setup failed: " + e.getMessage());
        }

        // Launch the Login window on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new LoginDialog().setVisible(true));
    }
}

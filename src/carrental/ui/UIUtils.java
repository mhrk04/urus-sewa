package carrental.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Shared UI utility class for consistent styling across all panels.
 *
 * Centralises colour constants and helper methods so every panel
 * uses the same visual design without repeating code (DRY principle).
 *
 * All methods and constants are static – no instance needed.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class UIUtils {

    // =========================================================
    // Brand Colour Palette
    // =========================================================

    /** Navy blue – primary brand colour (header, buttons) */
    public static final Color PRIMARY = new Color(26, 54, 93);

    /** Slate blue – accent actions (search, edit) */
    public static final Color ACCENT  = new Color(43, 108, 176);

    /** Emerald green – positive actions (add, confirm rental) */
    public static final Color SUCCESS = new Color(34, 154, 83);

    /** Red – destructive actions (delete) */
    public static final Color DANGER  = new Color(197, 48, 48);

    /** Orange – warning/guest mode indicator */
    public static final Color WARNING = new Color(221, 107, 32);

    /** Light grey – background of main window */
    public static final Color BG      = new Color(247, 250, 252);

    /** White – card/form panel background */
    public static final Color CARD    = Color.WHITE;

    /** Light border colour used for separators and input borders */
    public static final Color BORDER  = new Color(226, 232, 240);

    // =========================================================
    // Button Styling
    // =========================================================

    /**
     * Applies a consistent visual style to a JButton.
     * setOpaque(true) is required for macOS to render background colours.
     *
     * @param btn the button to style
     * @param bg  the desired background colour
     */
    public static void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);            // Required for macOS Swing rendering
    }

    // =========================================================
    // Text Field Styling
    // =========================================================

    /**
     * Applies a consistent border and font to a JTextField.
     *
     * @param field the text field to style
     */
    public static void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(0, 30));
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)));

        // Enable native Command (macOS) / Control (Windows/Linux) copy, paste, cut, and select all shortcuts
        InputMap im = field.getInputMap(JComponent.WHEN_FOCUSED);
        int menuMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, menuMask), "copy-to-clipboard");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, menuMask), "paste-from-clipboard");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, menuMask), "cut-to-clipboard");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, menuMask), "select-all");
    }

    // =========================================================
    // Label Helpers
    // =========================================================

    /**
     * Creates a bold-font label using the standard form font.
     *
     * @param text label text
     * @return styled JLabel
     */
    public static JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        return lbl;
    }

    /**
     * Creates a plain-font label.
     *
     * @param text label text
     * @return styled JLabel
     */
    public static JLabel plainLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        return lbl;
    }
}

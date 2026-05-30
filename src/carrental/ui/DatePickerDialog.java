package carrental.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A beautiful, premium custom calendar date picker dialog.
 * Displays a 7x6 monthly grid of days, supports back/forward navigation,
 * and highlights the selected date.
 */
public class DatePickerDialog extends JDialog {

    private LocalDate selectedDate;
    private LocalDate viewingDate;

    private JLabel lblMonthYear;
    private JPanel gridPanel;
    private final LocalDate minDate; // Optional minimum date (e.g. today for rental)

    public DatePickerDialog(Window parent, LocalDate initialDate, LocalDate minDate) {
        super(parent, "Select Date", ModalityType.APPLICATION_MODAL);
        this.selectedDate = initialDate;
        this.viewingDate = initialDate;
        this.minDate = minDate;

        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(UIUtils.CARD);

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCalendarGrid(), BorderLayout.CENTER);

        setSize(320, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private JPanel buildHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(UIUtils.PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnPrev = new JButton("◀");
        UIUtils.styleButton(btnPrev, UIUtils.PRIMARY);
        btnPrev.setForeground(Color.WHITE);
        btnPrev.setPreferredSize(new Dimension(45, 30));
        btnPrev.addActionListener(e -> navigateMonth(-1));

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setForeground(Color.WHITE);
        lblMonthYear.setFont(new Font("Arial", Font.BOLD, 14));
        updateHeaderLabel();

        JButton btnNext = new JButton("▶");
        UIUtils.styleButton(btnNext, UIUtils.PRIMARY);
        btnNext.setForeground(Color.WHITE);
        btnNext.setPreferredSize(new Dimension(45, 30));
        btnNext.addActionListener(e -> navigateMonth(1));

        p.add(btnPrev, BorderLayout.WEST);
        p.add(lblMonthYear, BorderLayout.CENTER);
        p.add(btnNext, BorderLayout.EAST);

        return p;
    }

    private JPanel buildCalendarGrid() {
        gridPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        gridPanel.setBackground(UIUtils.CARD);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        renderCalendar();
        return gridPanel;
    }

    private void updateHeaderLabel() {
        String month = viewingDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = viewingDate.getYear();
        lblMonthYear.setText(month + " " + year);
    }

    private void navigateMonth(int offset) {
        viewingDate = viewingDate.plusMonths(offset);
        updateHeaderLabel();
        renderCalendar();
    }

    private void renderCalendar() {
        gridPanel.removeAll();

        // Weekday Headers
        String[] daysOfWeek = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String day : daysOfWeek) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));
            lbl.setForeground(new Color(113, 128, 150));
            gridPanel.add(lbl);
        }

        YearMonth ym = YearMonth.from(viewingDate);
        LocalDate firstOfMonth = viewingDate.withDayOfMonth(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = ym.lengthOfMonth();

        // Padding before month start
        for (int i = 0; i < startDayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }

        // Days list
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = viewingDate.withDayOfMonth(day);
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Arial", Font.PLAIN, 12));
            btn.setMargin(new Insets(2, 2, 2, 2));

            boolean isMinValid = (minDate == null || !date.isBefore(minDate));
            boolean isSelected = date.equals(selectedDate);

            if (!isMinValid) {
                btn.setEnabled(false);
                btn.setForeground(new Color(203, 213, 224));
            } else if (isSelected) {
                UIUtils.styleButton(btn, UIUtils.SUCCESS);
                btn.setForeground(Color.WHITE);
            } else {
                UIUtils.styleButton(btn, Color.WHITE);
                btn.setForeground(new Color(45, 55, 72));
                btn.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
            }

            final LocalDate finalDate = date;
            btn.addActionListener(e -> {
                selectedDate = finalDate;
                dispose();
            });

            gridPanel.add(btn);
        }

        // Reflow layout
        gridPanel.revalidate();
        gridPanel.repaint();
    }
}

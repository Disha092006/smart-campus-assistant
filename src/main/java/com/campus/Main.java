package com.campus;

import com.campus.db.Database;
import com.campus.ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Database.init(); // create tables if they do not exist
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL:\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running and db.properties is correct.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        // Swing must always be started on its own "Event Dispatch Thread"
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
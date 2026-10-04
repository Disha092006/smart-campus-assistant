package com.campus.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Small helpers shared by all panels. */
public class UiUtil {

    /** A table model that the user cannot edit by typing in cells. */
    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /** Returns the id (column 0) of the selected row, or -1 and shows a message. */
    public static int selectedId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(table, "Please select a row first.");
            return -1;
        }
        return (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    public static void error(java.awt.Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
package com.campus.ui;

import com.campus.db.Database;
import com.campus.model.Subject;
import com.campus.service.AttendanceCalculator;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AttendancePanel extends JPanel implements Refreshable {
    private final DefaultTableModel model =
            UiUtil.readOnlyModel("ID", "No.", "Subject", "Attended", "Total", "%", "Advice");
    private final JTable table = new JTable(model);
    private final JTextField nameField = Theme.field(14);
    private final JTextField targetField = Theme.field(4);

    public AttendancePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(Theme.pad(22, 28, 22, 28));
        targetField.setText("75");

        JButton add = Theme.button("+ Add Subject", Theme.ATT);
        JButton present = Theme.button("Mark Present", Theme.ATT);
        JButton absent = Theme.button("Mark Absent", Theme.TASK);
        JButton del = Theme.button("Delete", Theme.GREY);

        JPanel row = Theme.formRow();
        row.add(Theme.group("SUBJECT", nameField));
        row.add(add);
        row.add(Box.createHorizontalStrut(24));
        row.add(Theme.group("TARGET %", targetField));
        row.add(Box.createHorizontalStrut(24));
        row.add(present);
        row.add(absent);
        row.add(del);
        Theme.RoundedPanel form = Theme.card();
        form.add(row, BorderLayout.CENTER);

        add.addActionListener(e -> {
            if (nameField.getText().isBlank()) { UiUtil.error(this, "Enter a subject name."); return; }
            Database.addSubject(nameField.getText().trim());
            nameField.setText("");
            refresh();
        });
        present.addActionListener(e -> mark(true));
        absent.addActionListener(e -> mark(false));
        del.addActionListener(e -> {
            int id = UiUtil.selectedId(table, model);
            if (id > 0) { Database.deleteSubject(id); refresh(); }
        });
        targetField.addActionListener(e -> refresh());

        Theme.styleTable(table, Theme.ATT);
        table.getColumnModel().getColumn(1).setMaxWidth(60);          // No.
        table.getColumnModel().getColumn(6).setPreferredWidth(320);   // Advice
        table.removeColumn(table.getColumnModel().getColumn(0));      // hide real DB id
        // colour the % column: green if on target, red if below
        table.getColumnModel().getColumn(4).setCellRenderer(new Theme.RowRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                try {
                    double pct = Double.parseDouble(String.valueOf(v));
                    setForeground(pct >= target() ? Theme.ATT.darker() : Theme.DANGER);
                    setFont(Theme.BOLD);
                } catch (NumberFormatException ignored) { }
                return this;
            }
        });

        Theme.RoundedPanel tableCard = Theme.card();
        tableCard.add(Theme.scroll(table), BorderLayout.CENTER);
        JLabel tip = new JLabel("Tip: change TARGET % and press Enter to recalculate the advice.");
        tip.setFont(Theme.BASE);
        tip.setForeground(Theme.MUTED);

        add(form, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(tip, BorderLayout.SOUTH);
        refresh();
    }

    private double target() {
        try { return Double.parseDouble(targetField.getText().trim()); }
        catch (NumberFormatException e) { return 75; }
    }

    private void mark(boolean present) {
        int id = UiUtil.selectedId(table, model);
        if (id > 0) { Database.markAttendance(id, present); refresh(); }
    }

    @Override
    public void refresh() {
        double target = target();
        model.setRowCount(0);
        int no = 1; // display number: always 1,2,3... with no gaps
        for (Subject s : Database.listSubjects()) {
            double pct = AttendanceCalculator.percentage(s.attended(), s.total());
            model.addRow(new Object[]{s.id(), no++, s.name(), s.attended(), s.total(),
                    String.format("%.1f", pct),
                    AttendanceCalculator.advice(s.attended(), s.total(), target)});
        }
    }
}
package com.campus.ui;

import com.campus.db.Database;
import com.campus.model.Task;
import com.campus.service.ReminderService;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TasksPanel extends JPanel implements Refreshable {
    private final DefaultTableModel model = UiUtil.readOnlyModel("ID", "No.", "Title", "Type", "Due", "Status");
    private final JTable table = new JTable(model);
    private final JTextField titleField = Theme.field(14);
    private final JComboBox<String> typeBox =
            new JComboBox<>(new String[]{"Assignment", "Exam", "Class", "Other"});
    private final JTextField dueField = Theme.field(12);

    public TasksPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(Theme.pad(22, 28, 22, 28));
        Theme.styleCombo(typeBox);
        dueField.setText(LocalDateTime.now().plusDays(1).withMinute(0).format(ReminderService.FMT));

        JButton add = Theme.button("+ Add Task", Theme.TASK);
        JButton done = Theme.button("Mark Done", Theme.ATT);
        JButton del = Theme.button("Delete", Theme.GREY);

        JPanel row = Theme.formRow();
        row.add(Theme.group("TITLE", titleField));
        row.add(Theme.group("TYPE", typeBox));
        row.add(Theme.group("DUE (yyyy-MM-dd HH:mm)", dueField));
        row.add(add);
        row.add(done);
        row.add(del);
        Theme.RoundedPanel form = Theme.card();
        form.add(row, BorderLayout.CENTER);

        add.addActionListener(e -> {
            if (titleField.getText().isBlank()) { UiUtil.error(this, "Enter a title."); return; }
            try {
                LocalDateTime.parse(dueField.getText().trim(), ReminderService.FMT); // validate format
            } catch (DateTimeParseException ex) {
                UiUtil.error(this, "Use the format yyyy-MM-dd HH:mm  e.g. 2026-10-20 18:00");
                return;
            }
            Database.addTask(titleField.getText().trim(), (String) typeBox.getSelectedItem(),
                    dueField.getText().trim());
            titleField.setText("");
            refresh();
        });
        done.addActionListener(e -> {
            int id = UiUtil.selectedId(table, model);
            if (id > 0) { Database.markDone(id); refresh(); }
        });
        del.addActionListener(e -> {
            int id = UiUtil.selectedId(table, model);
            if (id > 0) { Database.deleteTask(id); refresh(); }
        });

        Theme.styleTable(table, Theme.TASK);
        table.getColumnModel().getColumn(1).setMaxWidth(60);      // No.
        table.removeColumn(table.getColumnModel().getColumn(0));  // hide real DB id
        // coloured status text
        table.getColumnModel().getColumn(4).setCellRenderer(new Theme.RowRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setFont(Theme.BOLD);
                setForeground("Done".equals(v) ? Theme.ATT.darker() : new Color(0xD97706));
                return this;
            }
        });

        Theme.RoundedPanel tableCard = Theme.card();
        tableCard.add(Theme.scroll(table), BorderLayout.CENTER);
        JLabel tip = new JLabel("A background thread pops up a reminder when a task is due within 24 hours.");
        tip.setFont(Theme.BASE);
        tip.setForeground(Theme.MUTED);

        add(form, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(tip, BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        int no = 1; // display number: always 1,2,3... with no gaps
        for (Task t : Database.listTasks())
            model.addRow(new Object[]{t.id(), no++, t.title(), t.type(), t.due(), t.done() ? "Done" : "Pending"});
    }
}
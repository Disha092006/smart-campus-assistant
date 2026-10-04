package com.campus.ui;

import com.campus.db.Database;
import com.campus.model.Exam;
import com.campus.service.StudyPlanner;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PlannerPanel extends JPanel implements Refreshable {
    private final DefaultTableModel model = UiUtil.readOnlyModel("ID", "No.", "Subject", "Exam date", "Difficulty");
    private final JTable table = new JTable(model);
    private final JTextField subjectField = new JTextField(10);
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(10).toString(), 8);
    private final JComboBox<Integer> diffBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    private final JTextField hoursField = new JTextField("3", 3);
    private final JTextArea planArea = new JTextArea(10, 40);

    public PlannerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add Exam");
        JButton del = new JButton("Delete");
        JButton gen = new JButton("Generate Plan");
        top.add(new JLabel("Subject:")); top.add(subjectField);
        top.add(new JLabel("Date (yyyy-MM-dd):")); top.add(dateField);
        top.add(new JLabel("Difficulty (1-5):")); top.add(diffBox);
        top.add(add); top.add(del);
        top.add(new JLabel("  Study hours/day:")); top.add(hoursField); top.add(gen);

        add.addActionListener(e -> {
            try {
                LocalDate.parse(dateField.getText().trim());
                if (subjectField.getText().isBlank()) throw new IllegalArgumentException();
                Database.addExam(subjectField.getText().trim(), dateField.getText().trim(),
                        (Integer) diffBox.getSelectedItem());
                subjectField.setText("");
                refresh();
            } catch (DateTimeParseException | IllegalArgumentException ex) {
                UiUtil.error(this, "Enter a subject and a date like 2026-11-15");
            }
        });
        del.addActionListener(e -> {
            int id = UiUtil.selectedId(table, model);
            if (id > 0) { Database.deleteExam(id); refresh(); }
        });
        gen.addActionListener(e -> {
            int hours;
            try { hours = Math.max(1, Math.min(12, Integer.parseInt(hoursField.getText().trim()))); }
            catch (NumberFormatException ex) { hours = 3; }
            planArea.setText(String.join("\n", StudyPlanner.plan(Database.listExams(), hours)));
            planArea.setCaretPosition(0);
        });

        planArea.setEditable(false);
        planArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane planScroll = new JScrollPane(planArea);
        planScroll.setBorder(BorderFactory.createTitledBorder("Your revision plan"));

        table.getColumnModel().getColumn(1).setMaxWidth(50);      // No. column
        table.removeColumn(table.getColumnModel().getColumn(0));  // hide real DB id
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), planScroll);
        split.setResizeWeight(0.35);
        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        int no = 1; // display number: always 1,2,3... with no gaps
        for (Exam x : Database.listExams())
            model.addRow(new Object[]{x.id(), no++, x.subject(), x.date(), x.difficulty()});
    }
}
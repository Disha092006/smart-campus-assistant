package com.campus.ui;

import com.campus.db.Database;
import com.campus.model.Expense;
import com.campus.service.ExpenseSplitter;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ExpensePanel extends JPanel implements Refreshable {
    private final DefaultTableModel model =
            UiUtil.readOnlyModel("ID", "No.", "Description", "Paid by", "Amount", "Shared among");
    private final JTable table = new JTable(model);
    private final JTextField descField = Theme.field(10);
    private final JTextField payerField = Theme.field(8);
    private final JTextField amountField = Theme.field(6);
    private final JTextField peopleField = Theme.field(16);
    private final JTextArea settlement = new JTextArea();

    public ExpensePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(Theme.pad(22, 28, 22, 28));

        JButton add = Theme.button("+ Add Expense", Theme.EXP);
        JButton del = Theme.button("Delete", Theme.GREY);

        JPanel row = Theme.formRow();
        row.add(Theme.group("WHAT", descField));
        row.add(Theme.group("PAID BY", payerField));
        row.add(Theme.group("AMOUNT", amountField));
        row.add(Theme.group("SHARED BY (comma separated)", peopleField));
        row.add(add);
        row.add(del);
        Theme.RoundedPanel form = Theme.card();
        form.add(row, BorderLayout.CENTER);

        add.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText().trim());
                if (descField.getText().isBlank() || payerField.getText().isBlank()
                        || peopleField.getText().isBlank() || amt <= 0) throw new NumberFormatException();
                Database.addExpense(descField.getText().trim(), payerField.getText().trim(),
                        amt, peopleField.getText().trim());
                descField.setText("");
                amountField.setText("");
                refresh();
            } catch (NumberFormatException ex) {
                UiUtil.error(this, "Fill all fields. Amount must be a positive number.");
            }
        });
        del.addActionListener(e -> {
            int id = UiUtil.selectedId(table, model);
            if (id > 0) { Database.deleteExpense(id); refresh(); }
        });

        Theme.styleTable(table, Theme.EXP);
        table.getColumnModel().getColumn(1).setMaxWidth(60);      // No.
        table.removeColumn(table.getColumnModel().getColumn(0));  // hide real DB id

        Theme.RoundedPanel tableCard = Theme.card();
        tableCard.add(Theme.scroll(table), BorderLayout.CENTER);

        settlement.setEditable(false);
        settlement.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        settlement.setForeground(Theme.TEXT);
        settlement.setOpaque(false);
        settlement.setBorder(Theme.pad(8, 0, 0, 0));
        Theme.RoundedPanel settleCard = Theme.card();
        settleCard.setPreferredSize(new Dimension(0, 190));
        settleCard.add(Theme.label("WHO OWES WHOM  (fewest possible payments)"), BorderLayout.NORTH);
        settleCard.add(Theme.scroll(settlement), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(settleCard, BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        java.util.List<Expense> list = Database.listExpenses();
        int no = 1; // display number: always 1,2,3... with no gaps
        for (Expense x : list)
            model.addRow(new Object[]{x.id(), no++, x.description(), x.paidBy(),
                    String.format("%.2f", x.amount()), x.participants()});
        settlement.setText(String.join("\n", ExpenseSplitter.settle(list)));
    }
}
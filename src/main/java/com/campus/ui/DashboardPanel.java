package com.campus.ui;

import com.campus.db.Database;
import com.campus.model.Expense;
import com.campus.model.Subject;
import com.campus.model.Task;
import com.campus.service.AttendanceCalculator;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class DashboardPanel extends JPanel implements Refreshable {
    private final StatCard pendingCard  = new StatCard("PENDING TASKS", Theme.TASK);
    private final StatCard spendCard    = new StatCard("GROUP SPENDING", Theme.EXP);
    private final StatCard subjectCard  = new StatCard("SUBJECTS", Theme.ATT);
    private final StatCard avgCard      = new StatCard("AVG ATTENDANCE", Theme.DASH);
    private final ChartCanvas chart = new ChartCanvas();
    private final JTextArea upcoming = new JTextArea();

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(Theme.pad(22, 28, 22, 28));

        JPanel stats = new JPanel(new GridLayout(1, 4, 20, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 120));
        stats.add(avgCard);
        stats.add(subjectCard);
        stats.add(pendingCard);
        stats.add(spendCard);

        Theme.RoundedPanel chartCard = Theme.card();
        chartCard.add(Theme.label("ATTENDANCE BY SUBJECT"), BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);

        Theme.RoundedPanel taskCard = Theme.card();
        taskCard.setPreferredSize(new Dimension(340, 0));
        taskCard.add(Theme.label("UPCOMING TASKS"), BorderLayout.NORTH);
        upcoming.setEditable(false);
        upcoming.setFont(Theme.BASE);
        upcoming.setForeground(Theme.TEXT);
        upcoming.setLineWrap(true);
        upcoming.setWrapStyleWord(true);
        upcoming.setBorder(Theme.pad(10, 0, 0, 0));
        taskCard.add(upcoming, BorderLayout.CENTER);

        JPanel lower = new JPanel(new BorderLayout(20, 0));
        lower.setOpaque(false);
        lower.add(chartCard, BorderLayout.CENTER);
        lower.add(taskCard, BorderLayout.EAST);

        add(stats, BorderLayout.NORTH);
        add(lower, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        List<Subject> subjects = Database.listSubjects();
        List<Task> tasks = Database.listTasks();
        long pending = tasks.stream().filter(t -> !t.done()).count();
        double spent = Database.listExpenses().stream().mapToDouble(Expense::amount).sum();
        double avg = subjects.stream().filter(s -> s.total() > 0)
                .mapToDouble(s -> AttendanceCalculator.percentage(s.attended(), s.total()))
                .average().orElse(0);

        avgCard.set(String.format("%.0f%%", avg));
        subjectCard.set(String.valueOf(subjects.size()));
        pendingCard.set(String.valueOf(pending));
        spendCard.set(String.format("Rs %.0f", spent));
        chart.setData(subjects);

        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (Task t : tasks) {
            if (t.done() || shown >= 6) continue;
            sb.append("•  ").append(t.title()).append("\n    ").append(t.due()).append("   [").append(t.type()).append("]\n\n");
            shown++;
        }
        upcoming.setText(shown == 0 ? "No pending tasks. Enjoy!" : sb.toString());
    }

    /** Coloured rounded number card. */
    private static class StatCard extends Theme.RoundedPanel {
        private final JLabel value = new JLabel("0");
        StatCard(String caption, Color color) {
            super(color);
            setLayout(new GridLayout(2, 1));
            setBorder(Theme.pad(14, 22, 18, 18));
            value.setFont(Theme.BIG);
            value.setForeground(Color.WHITE);
            JLabel cap = new JLabel(caption);
            cap.setFont(Theme.SMALL);
            cap.setForeground(new Color(255, 255, 255, 215));
            add(value);
            add(cap);
        }
        void set(String v) { value.setText(v); }
    }

    /** Hand-drawn bar chart (Graphics2D, no library). */
    private static class ChartCanvas extends JPanel {
        private List<Subject> data = List.of();

        ChartCanvas() { setOpaque(false); }
        void setData(List<Subject> d) { data = d; repaint(); }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int left = 44, top = 20, bottom = h - 34, right = w - 10;

            if (data.isEmpty()) {
                g.setFont(Theme.BASE);
                g.setColor(Theme.MUTED);
                String msg = "Add subjects in the Attendance page to see the chart";
                g.drawString(msg, (w - g.getFontMetrics().stringWidth(msg)) / 2, h / 2);
                g.dispose();
                return;
            }
            // grid lines
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            for (int p = 0; p <= 100; p += 25) {
                int y = bottom - (int) ((bottom - top) * p / 100.0);
                g.setColor(Theme.LINE);
                g.drawLine(left, y, right, y);
                g.setColor(Theme.MUTED);
                g.drawString(p + "%", 6, y + 4);
            }
            // 75% target line
            int y75 = bottom - (int) ((bottom - top) * 0.75);
            g.setColor(Theme.TASK);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 5f}, 0f));
            g.drawLine(left, y75, right, y75);
            g.setStroke(new BasicStroke(1f));

            int slot = (right - left) / data.size();
            int barW = Math.max(24, Math.min(80, slot - 40));
            for (int i = 0; i < data.size(); i++) {
                Subject s = data.get(i);
                double pct = AttendanceCalculator.percentage(s.attended(), s.total());
                int barH = (int) ((bottom - top) * pct / 100.0);
                int x = left + i * slot + (slot - barW) / 2;
                Color base = pct >= 75 ? Theme.ATT : Theme.DANGER;
                g.setPaint(new GradientPaint(0, bottom - barH, base, 0, bottom, Theme.lighten(base, 0.4f)));
                g.fillRoundRect(x, bottom - barH, barW, Math.max(barH, 4), 12, 12);

                g.setFont(Theme.BOLD);
                g.setColor(Theme.TEXT);
                String pt = String.format("%.0f%%", pct);
                g.drawString(pt, x + (barW - g.getFontMetrics().stringWidth(pt)) / 2, bottom - barH - 6);

                g.setFont(Theme.BASE);
                g.setColor(Theme.MUTED);
                String name = s.name().length() > 12 ? s.name().substring(0, 11) + "." : s.name();
                g.drawString(name, x + (barW - g.getFontMetrics().stringWidth(name)) / 2, bottom + 20);
            }
            g.dispose();
        }
    }
}
package com.campus.ui;

import com.campus.Session;
import com.campus.service.ReminderService;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** Full-screen window: dark sidebar on the left, coloured header + page on the right. */
public class MainFrame extends JFrame {
    private final ReminderService reminders = new ReminderService();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel header = new JPanel(new BorderLayout());
    private final JLabel titleLbl = new JLabel();
    private final JLabel subLbl = new JLabel();
    private final List<Section> sections = new ArrayList<>();
    private final List<NavButton> navButtons = new ArrayList<>();

    private record Section(String name, String subtitle, Color color, JPanel panel) { }

    public MainFrame() {
        super("Smart Campus Assistant");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));

        sections.add(new Section("Dashboard", "Your campus life at a glance", Theme.DASH, new DashboardPanel()));
        sections.add(new Section("Attendance", "Track classes and see how many you can skip", Theme.ATT, new AttendancePanel()));
        sections.add(new Section("Tasks & Reminders", "Deadlines, exams and automatic reminders", Theme.TASK, new TasksPanel()));
        sections.add(new Section("Expense Splitter", "Share costs and settle up with fewer payments", Theme.EXP, new ExpensePanel()));
        sections.add(new Section("Study Planner", "A smart day-by-day revision schedule", Theme.PLAN, new PlannerPanel()));

        // ----- sidebar -----
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(250, 0));

        JPanel brand = new JPanel(new GridLayout(2, 1));
        brand.setOpaque(false);
        brand.setBorder(Theme.pad(30, 26, 26, 20));
        JLabel b1 = new JLabel("Smart Campus");
        b1.setFont(new Font("Segoe UI", Font.BOLD, 24));
        b1.setForeground(Color.WHITE);
        JLabel b2 = new JLabel("ASSISTANT");
        b2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b2.setForeground(Theme.lighten(Theme.PLAN, 0.4f));
        brand.add(b1);
        brand.add(b2);
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        for (Section s : sections) {
            NavButton nb = new NavButton(s.name(), s.color());
            nb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            nb.addActionListener(e -> show(s.name()));
            navButtons.add(nb);
            nav.add(nb);
            content.add(s.panel(), s.name());
        }
        side.add(nav, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 10));
        bottom.setOpaque(false);
        bottom.setBorder(Theme.pad(10, 22, 24, 22));
        JLabel who = new JLabel("Logged in as  " + Session.username);
        who.setFont(Theme.BASE);
        who.setForeground(new Color(0xC8CCE0));
        JButton logout = Theme.button("Logout", Theme.DANGER);
        logout.addActionListener(e -> logout());
        bottom.add(who);
        bottom.add(logout);
        side.add(bottom, BorderLayout.SOUTH);

        // ----- header -----
        titleLbl.setFont(Theme.TITLE);
        titleLbl.setForeground(Color.WHITE);
        subLbl.setFont(Theme.BASE);
        subLbl.setForeground(new Color(255, 255, 255, 220));
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(titleLbl);
        titles.add(subLbl);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        date.setFont(Theme.BOLD);
        date.setForeground(Color.WHITE);
        header.setBorder(Theme.pad(20, 30, 20, 30));
        header.add(titles, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);

        content.setBackground(Theme.BG);
        JPanel right = new JPanel(new BorderLayout());
        right.add(header, BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(side, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        show("Dashboard");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen window

        reminders.start(this);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { reminders.stop(); }
        });
    }

    /** Switch page, recolour the header and reload that page's data. */
    public void show(String name) {
        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            boolean active = s.name().equals(name);
            navButtons.get(i).setActive(active);
            if (active) {
                cards.show(content, name);
                header.setBackground(s.color());
                titleLbl.setText(s.name());
                subLbl.setText(s.subtitle());
                if (s.panel() instanceof Refreshable r) r.refresh();
            }
        }
        header.repaint();
    }

    private void logout() {
        reminders.stop();
        Session.userId = -1;
        Session.username = "";
        dispose();
        new LoginFrame().setVisible(true);
    }

    /** Sidebar button with a coloured dot; the active one gets a highlight bar. */
    private static class NavButton extends JButton {
        private final Color accent;
        private boolean active;

        NavButton(String text, Color accent) {
            super(text);
            this.accent = accent;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(new Color(0xC8CCE0));
            setHorizontalAlignment(LEFT);
            setBorder(Theme.pad(0, 58, 0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(250, 54));
        }

        void setActive(boolean a) {
            active = a;
            setForeground(a ? Color.WHITE : new Color(0xC8CCE0));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g.setColor(new Color(255, 255, 255, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(accent);
                g.fillRect(0, 0, 6, getHeight());
            } else if (getModel().isRollover()) {
                g.setColor(new Color(255, 255, 255, 14));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(accent);
            g.fillOval(28, getHeight() / 2 - 8, 16, 16);
            g.dispose();
            super.paintComponent(g0);
        }
    }
}
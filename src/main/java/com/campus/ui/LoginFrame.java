package com.campus.ui;

import com.campus.service.AuthService;
import java.awt.*;
import javax.swing.*;

/** Split-screen login: gradient welcome panel on the left, form on the right. */
public class LoginFrame extends JFrame {
    private final JTextField userField = Theme.field(18);
    private final JPasswordField passField = new JPasswordField(18);

    public LoginFrame() {
        super("Smart Campus Assistant - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Theme.styleField(passField);

        // ----- left: gradient welcome -----
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, Theme.DASH, getWidth(), getHeight(), Theme.PLAN));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 28));
                g.fillOval(-80, getHeight() - 220, 300, 300);
                g.fillOval(getWidth() - 140, -90, 260, 260);
                g.dispose();
            }
        };
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("Smart Campus");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 38));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("Assistant");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 38));
        t2.setForeground(new Color(0xFDE68A));
        text.add(t1);
        text.add(t2);
        text.add(Box.createVerticalStrut(24));
        String[] points = {"Track attendance & predict skips", "Never miss a deadline",
                "Split expenses fairly", "Plan your revision smartly"};
        for (String p : points) {
            JLabel l = new JLabel("•  " + p);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            l.setForeground(new Color(255, 255, 255, 230));
            l.setBorder(Theme.pad(5, 0, 5, 0));
            text.add(l);
        }
        left.add(text);

        // ----- right: form -----
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(Theme.pad(30, 60, 30, 60));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 30));
        welcome.setForeground(Theme.TEXT);
        JLabel hint = new JLabel("Login, or create a new account");
        hint.setFont(Theme.BASE);
        hint.setForeground(Theme.MUTED);

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);  right.add(welcome, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 26, 0); right.add(hint, c);
        c.gridy = 2; c.insets = new Insets(0, 0, 4, 0);  right.add(Theme.label("USERNAME"), c);
        c.gridy = 3; c.insets = new Insets(0, 0, 16, 0); right.add(userField, c);
        c.gridy = 4; c.insets = new Insets(0, 0, 4, 0);  right.add(Theme.label("PASSWORD"), c);
        c.gridy = 5; c.insets = new Insets(0, 0, 26, 0); right.add(passField, c);

        JButton loginBtn = Theme.button("Login", Theme.DASH);
        JButton registerBtn = Theme.button("Register", Theme.ATT);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(loginBtn);
        buttons.add(registerBtn);
        c.gridy = 6; c.insets = new Insets(0, 0, 0, 0);
        right.add(buttons, c);

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> doRegister());
        getRootPane().setDefaultButton(loginBtn); // Enter key = login

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(left);
        root.add(right);
        setContentPane(root);
        setSize(900, 540);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (AuthService.login(u, p)) {
            dispose();
            new MainFrame().setVisible(true);
        } else {
            UiUtil.error(this, "Wrong username or password.\nNew here? Type a username and password, then click Register.");
        }
    }

    private void doRegister() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (AuthService.register(u, p)) {
            JOptionPane.showMessageDialog(this, "Account created! You can log in now.");
        } else {
            UiUtil.error(this, "Registration failed. Username may exist, or password is shorter than 4 characters.");
        }
    }
}
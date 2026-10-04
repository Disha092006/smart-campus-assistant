package com.campus.service;

import com.campus.db.Database;
import com.campus.model.Task;
import java.awt.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * MULTITHREADING: a background thread wakes up every 60 seconds, looks for
 * tasks due within 24 hours (or overdue) and shows a pop-up on the UI thread.
 */
public class ReminderService {
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "reminder-thread");
        t.setDaemon(true); // does not stop the program from exiting
        return t;
    });

    public void start(Component parent) {
        scheduler.scheduleAtFixedRate(() -> check(parent), 3, 60, TimeUnit.SECONDS);
    }

    public void stop() { scheduler.shutdownNow(); }

    private void check(Component parent) {
        try {
            for (Task t : Database.pendingUnreminded()) {
                LocalDateTime due = LocalDateTime.parse(t.due(), FMT);
                long hours = Duration.between(LocalDateTime.now(), due).toHours();
                if (hours <= 24) {
                    Database.markReminded(t.id());
                    String msg = (due.isBefore(LocalDateTime.now()) ? "OVERDUE: " : "Due soon: ")
                            + t.title() + " [" + t.type() + "] at " + t.due();
                    // Swing components may only be touched from the UI thread:
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent, msg, "Reminder", JOptionPane.WARNING_MESSAGE));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
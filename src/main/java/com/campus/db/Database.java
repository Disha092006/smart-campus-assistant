package com.campus.db;

import com.campus.Session;
import com.campus.model.*;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * All SQL lives here (this is called the DAO / data-access layer).
 * The UI never writes SQL; it just calls these methods.
 * Methods are 'synchronized' because the reminder thread and the UI thread
 * share ONE connection - only one may use it at a time.
 */
public class Database {
    private static Connection conn;
    private static Properties config;

    /** Reads db.properties (url, user, password) from the folder where the program runs. */
    private static Properties config() {
        if (config == null) {
            config = new Properties();
            File f = new File("db.properties");
            try (FileInputStream in = new FileInputStream(f)) {
                config.load(in);
            } catch (Exception e) {
                throw new RuntimeException("Cannot read db.properties in " + f.getAbsolutePath()
                        + "  -> copy db.properties.example to db.properties and put your MySQL password in it.", e);
            }
        }
        return config;
    }

    /** Converts one row of a ResultSet into a Java object. */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private static synchronized Connection get() throws SQLException {
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            Properties p = config();
            conn = DriverManager.getConnection(p.getProperty("db.url"),
                    p.getProperty("db.user"), p.getProperty("db.password"));
        }
        return conn;
    }

    /** Runs INSERT / UPDATE / DELETE. '?' placeholders prevent SQL injection. */
    public static synchronized void exec(String sql, Object... args) {
        try (PreparedStatement ps = get().prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    /** Runs SELECT and maps every row to an object using the given mapper. */
    public static synchronized <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = get().prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
        return list;
    }

    public static void init() {
        exec("CREATE TABLE IF NOT EXISTS users(id INT PRIMARY KEY AUTO_INCREMENT, "
           + "username VARCHAR(50) UNIQUE NOT NULL, salt VARCHAR(64) NOT NULL, hash VARCHAR(100) NOT NULL)");
        exec("CREATE TABLE IF NOT EXISTS subjects(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL, "
           + "name VARCHAR(100) NOT NULL, attended INT DEFAULT 0, total INT DEFAULT 0, "
           + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        exec("CREATE TABLE IF NOT EXISTS tasks(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL, "
           + "title VARCHAR(200) NOT NULL, type VARCHAR(30), due VARCHAR(20), done INT DEFAULT 0, reminded INT DEFAULT 0, "
           + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        exec("CREATE TABLE IF NOT EXISTS expenses(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL, "
           + "description VARCHAR(200), paid_by VARCHAR(50), amount DOUBLE, participants VARCHAR(300), "
           + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        exec("CREATE TABLE IF NOT EXISTS exams(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL, "
           + "subject VARCHAR(100), exam_date VARCHAR(12), difficulty INT, "
           + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
    }

    // ---------------- USERS ----------------
    public static boolean addUser(String username, String salt, String hash) {
        try {
            exec("INSERT INTO users(username,salt,hash) VALUES(?,?,?)", username, salt, hash);
            return true;
        } catch (RuntimeException e) {
            return false; // username already taken (UNIQUE constraint)
        }
    }

    /** Returns {id, salt, hash} or null if the user does not exist. */
    public static String[] findUser(String username) {
        List<String[]> r = query("SELECT id,salt,hash FROM users WHERE username=?",
                rs -> new String[]{rs.getString(1), rs.getString(2), rs.getString(3)}, username);
        return r.isEmpty() ? null : r.get(0);
    }

    // ---------------- SUBJECTS / ATTENDANCE ----------------
    public static void addSubject(String name) {
        exec("INSERT INTO subjects(user_id,name) VALUES(?,?)", Session.userId, name);
    }
    public static List<Subject> listSubjects() {
        return query("SELECT id,name,attended,total FROM subjects WHERE user_id=? ORDER BY name",
                rs -> new Subject(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4)), Session.userId);
    }
    public static void markAttendance(int id, boolean present) {
        exec("UPDATE subjects SET total=total+1, attended=attended+? WHERE id=? AND user_id=?",
                present ? 1 : 0, id, Session.userId);
    }
    public static void deleteSubject(int id) {
        exec("DELETE FROM subjects WHERE id=? AND user_id=?", id, Session.userId);
    }

    // ---------------- TASKS ----------------
    public static void addTask(String title, String type, String due) {
        exec("INSERT INTO tasks(user_id,title,type,due) VALUES(?,?,?,?)", Session.userId, title, type, due);
    }
    private static final RowMapper<Task> TASK_MAPPER = rs ->
            new Task(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5) == 1);

    public static List<Task> listTasks() {
        return query("SELECT id,title,type,due,done FROM tasks WHERE user_id=? ORDER BY due",
                TASK_MAPPER, Session.userId);
    }
    public static List<Task> pendingUnreminded() {
        return query("SELECT id,title,type,due,done FROM tasks WHERE user_id=? AND done=0 AND reminded=0",
                TASK_MAPPER, Session.userId);
    }
    public static void markDone(int id) {
        exec("UPDATE tasks SET done=1 WHERE id=? AND user_id=?", id, Session.userId);
    }
    public static void markReminded(int id) {
        exec("UPDATE tasks SET reminded=1 WHERE id=?", id);
    }
    public static void deleteTask(int id) {
        exec("DELETE FROM tasks WHERE id=? AND user_id=?", id, Session.userId);
    }

    // ---------------- EXPENSES ----------------
    public static void addExpense(String desc, String paidBy, double amount, String participants) {
        exec("INSERT INTO expenses(user_id,description,paid_by,amount,participants) VALUES(?,?,?,?,?)",
                Session.userId, desc, paidBy, amount, participants);
    }
    public static List<Expense> listExpenses() {
        return query("SELECT id,description,paid_by,amount,participants FROM expenses WHERE user_id=?",
                rs -> new Expense(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getString(5)),
                Session.userId);
    }
    public static void deleteExpense(int id) {
        exec("DELETE FROM expenses WHERE id=? AND user_id=?", id, Session.userId);
    }

    // ---------------- EXAMS (for the study planner) ----------------
    public static void addExam(String subject, String date, int difficulty) {
        exec("INSERT INTO exams(user_id,subject,exam_date,difficulty) VALUES(?,?,?,?)",
                Session.userId, subject, date, difficulty);
    }
    public static List<Exam> listExams() {
        return query("SELECT id,subject,exam_date,difficulty FROM exams WHERE user_id=? ORDER BY exam_date",
                rs -> new Exam(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)), Session.userId);
    }
    public static void deleteExam(int id) {
        exec("DELETE FROM exams WHERE id=? AND user_id=?", id, Session.userId);
    }
}
package com.campus.service;

/**
 * Attendance maths.
 *  Skip x classes:   attended / (total + x)  >= target
 *                    => x <= 100*attended/target - total
 *  Attend y classes: (attended + y) / (total + y) >= target
 *                    => y >= (target*total - 100*attended) / (100 - target)
 */
public class AttendanceCalculator {

    public static double percentage(int attended, int total) {
        return total == 0 ? 0 : attended * 100.0 / total;
    }

    public static String advice(int attended, int total, double target) {
        if (total == 0) return "No classes recorded yet";
        double pct = percentage(attended, total);
        if (pct >= target) {
            int canSkip = (int) Math.floor(100.0 * attended / target - total);
            return canSkip <= 0 ? "Safe now, but don't skip" : "You can skip " + canSkip + " more class(es)";
        }
        if (target >= 100) return "Impossible to reach 100%";
        int need = (int) Math.ceil((target * total - 100.0 * attended) / (100 - target));
        return "Attend next " + need + " class(es) to reach " + (int) target + "%";
    }
}
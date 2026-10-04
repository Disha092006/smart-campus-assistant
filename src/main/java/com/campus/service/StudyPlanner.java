package com.campus.service;

import com.campus.model.Exam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Builds a day-by-day plan. Each day has 'hoursPerDay' one-hour slots.
 * For every slot we pick the subject with the lowest (hoursGiven+1)/difficulty
 * (harder subjects get proportionally more hours); ties go to the nearest exam.
 * A subject is only eligible while its exam date is still in the future.
 */
public class StudyPlanner {

    public static List<String> plan(List<Exam> exams, int hoursPerDay) {
        List<String> out = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd MMM");
        Map<String, Integer> given = new HashMap<>();

        LocalDate last = today;
        for (Exam e : exams) {
            LocalDate d = LocalDate.parse(e.date());
            if (d.isAfter(last)) last = d;
        }
        if (!last.isAfter(today)) {
            out.add("Add exams with future dates to generate a plan.");
            return out;
        }

        for (LocalDate day = today; day.isBefore(last); day = day.plusDays(1)) {
            Map<String, Integer> todayHours = new LinkedHashMap<>();
            for (int slot = 0; slot < hoursPerDay; slot++) {
                Exam best = null;
                double bestScore = Double.MAX_VALUE;
                for (Exam e : exams) {
                    LocalDate examDay = LocalDate.parse(e.date());
                    if (!examDay.isAfter(day)) continue; // exam already over / today
                    double score = (given.getOrDefault(e.subject(), 0) + 1.0) / e.difficulty();
                    // tiny bonus for earlier exams so ties pick the closer one
                    score += examDay.toEpochDay() * 1e-6;
                    if (score < bestScore) { bestScore = score; best = e; }
                }
                if (best == null) break;
                given.merge(best.subject(), 1, Integer::sum);
                todayHours.merge(best.subject(), 1, Integer::sum);
            }
            if (todayHours.isEmpty()) continue;
            StringBuilder sb = new StringBuilder(day.format(fmt)).append(":  ");
            for (Map.Entry<String, Integer> en : todayHours.entrySet())
                sb.append(en.getKey()).append(" (").append(en.getValue()).append("h)  ");
            for (Exam e : exams)
                if (LocalDate.parse(e.date()).equals(day.plusDays(1)))
                    sb.append("<- exam tomorrow: ").append(e.subject());
            out.add(sb.toString());
        }
        return out;
    }
}
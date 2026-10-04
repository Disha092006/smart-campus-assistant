package com.campus.service;

import com.campus.model.Expense;
import java.util.*;

/**
 * Greedy "who owes whom" algorithm.
 * 1. For each expense: payer gets +amount, each participant gets -share.
 * 2. Positive balance = should receive money, negative = should pay.
 * 3. Repeatedly match the biggest debtor with the biggest creditor.
 */
public class ExpenseSplitter {

    public static Map<String, Double> balances(List<Expense> expenses) {
        Map<String, Double> bal = new TreeMap<>();
        for (Expense e : expenses) {
            String[] people = e.participants().split(",");
            List<String> names = new ArrayList<>();
            for (String p : people) if (!p.isBlank()) names.add(p.trim());
            if (names.isEmpty()) continue;
            double share = e.amount() / names.size();
            bal.merge(e.paidBy().trim(), e.amount(), Double::sum);
            for (String n : names) bal.merge(n, -share, Double::sum);
        }
        return bal;
    }

    public static List<String> settle(List<Expense> expenses) {
        Map<String, Double> bal = balances(expenses);
        List<String> result = new ArrayList<>();
        // max-heaps: biggest creditor / biggest debtor first
        PriorityQueue<Map.Entry<String, Double>> creditors =
                new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));
        PriorityQueue<Map.Entry<String, Double>> debtors =
                new PriorityQueue<>((a, b) -> Double.compare(a.getValue(), b.getValue()));
        for (Map.Entry<String, Double> e : bal.entrySet()) {
            Map.Entry<String, Double> copy = new AbstractMap.SimpleEntry<>(e);
            if (e.getValue() > 0.01) creditors.add(copy);
            else if (e.getValue() < -0.01) debtors.add(copy);
        }
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<String, Double> c = creditors.poll();
            Map.Entry<String, Double> d = debtors.poll();
            double amount = Math.min(c.getValue(), -d.getValue());
            result.add(String.format("%s owes %s  Rs %.2f", d.getKey(), c.getKey(), amount));
            c.setValue(c.getValue() - amount);
            d.setValue(d.getValue() + amount);
            if (c.getValue() > 0.01) creditors.add(c);
            if (d.getValue() < -0.01) debtors.add(d);
        }
        if (result.isEmpty()) result.add("Everyone is settled up!");
        return result;
    }
}
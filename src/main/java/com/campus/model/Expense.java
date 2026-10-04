package com.campus.model;

/** participants is a comma separated string like "Ravi,Simran,Aman". */
public record Expense(int id, String description, String paidBy, double amount, String participants) { }
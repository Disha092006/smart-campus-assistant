package com.campus.model;

/** A record is a short way to write an immutable data class (Java 16+). */
public record Subject(int id, String name, int attended, int total) { }
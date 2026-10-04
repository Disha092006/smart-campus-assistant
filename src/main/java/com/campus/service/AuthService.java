package com.campus.service;

import com.campus.Session;
import com.campus.db.Database;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Registration + login using a random salt and SHA-256 hashing. */
public class AuthService {

    private static String hash(String salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean register(String username, String password) {
        if (username.isBlank() || password.length() < 4) return false;
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        return Database.addUser(username, salt, hash(salt, password));
    }

    public static boolean login(String username, String password) {
        String[] user = Database.findUser(username); // {id, salt, hash}
        if (user == null) return false;
        if (!hash(user[1], password).equals(user[2])) return false;
        Session.userId = Integer.parseInt(user[0]);
        Session.username = username;
        return true;
    }
}
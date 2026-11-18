package com.legacy.ecommerce.manager;

import java.util.Base64;

public class PasswordHasher {

    public String hash(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    public boolean matches(String password, String hashedPassword) {
        return hash(password).equals(hashedPassword);
    }
}

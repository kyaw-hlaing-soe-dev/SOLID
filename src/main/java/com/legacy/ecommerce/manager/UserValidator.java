package com.legacy.ecommerce.manager;

public class UserValidator {

    public void validateRegistration(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        boolean hasDigit = false;
        for (char character : password.toCharArray()) {
            if (Character.isDigit(character)) {
                hasDigit = true;
                break;
            }
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
    }
}

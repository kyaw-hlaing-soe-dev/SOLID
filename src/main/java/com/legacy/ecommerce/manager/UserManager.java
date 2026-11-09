package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.CustomerTier;
import com.legacy.ecommerce.model.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Legacy God Class: UserManager.
 * Combines User CRUD, input validation, authentication, password rules,
 * loyalty tier updates, email dispatching, audit logging, and CSV export.
 */
public class UserManager {
    private final LegacyDatabase database = LegacyDatabase.getInstance();
    // Direct dependency instantiation (Tight coupling)
    private final NotificationService notificationService = new NotificationService();

    public User registerUser(String name, String email, String password, String address, String phoneNumber) {
        // Validation logic
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
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
                break;
            }
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }

        // Uniqueness check
        for (User existingUser : database.getUsers().values()) {
            if (existingUser.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Email already registered: " + email);
            }
        }

        // Mock password hashing (Base64)
        String hashedPassword = Base64.getEncoder().encodeToString(password.getBytes());

        String userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        User newUser = new User(userId, name.trim(), email.trim(), hashedPassword, address, phoneNumber);
        database.getUsers().put(userId, newUser);
        database.addAuditLog("USER_REGISTERED: " + userId + " (" + email + ")");

        // Side effect: Automatically send welcome email
        try {
            notificationService.sendWelcomeEmail(newUser);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return newUser;
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        String hashedInput = Base64.getEncoder().encodeToString(password.getBytes());
        for (User user : database.getUsers().values()) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(hashedInput)) {
                if (!user.isActive()) {
                    throw new IllegalStateException("User account is deactivated.");
                }
                database.addAuditLog("USER_LOGIN_SUCCESS: " + user.getId());
                return user;
            }
        }
        database.addAuditLog("USER_LOGIN_FAILED: " + email);
        return null;
    }

    public User getUserById(String userId) {
        return database.getUsers().get(userId);
    }

    public User getUserByEmail(String email) {
        if (email == null) return null;
        for (User u : database.getUsers().values()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public void addLoyaltyPoints(String userId, int points) {
        User user = getUserById(userId);
        if (user != null && points > 0) {
            CustomerTier oldTier = user.getTier();
            user.addLoyaltyPoints(points);
            database.addAuditLog("LOYALTY_POINTS_ADDED: User " + userId + " +" + points + " points (Total: " + user.getLoyaltyPoints() + ")");
            
            // Check if upgraded and notify
            if (user.getTier() != oldTier) {
                database.addAuditLog("TIER_UPGRADED: User " + userId + " upgraded from " + oldTier + " to " + user.getTier());
                notificationService.sendEmail(user.getEmail(),
                        "Congratulations! You've been upgraded to " + user.getTier(),
                        "Hello " + user.getName() + ",\nYour loyalty tier is now " + user.getTier() + "!");
            }
        }
    }

    public void deductLoyaltyPoints(String userId, int points) {
        User user = getUserById(userId);
        if (user != null && points > 0) {
            user.deductLoyaltyPoints(points);
            database.addAuditLog("LOYALTY_POINTS_DEDUCTED: User " + userId + " -" + points + " points (Total: " + user.getLoyaltyPoints() + ")");
        }
    }

    public void deactivateUser(String userId) {
        User user = getUserById(userId);
        if (user != null) {
            user.setActive(false);
            database.addAuditLog("USER_DEACTIVATED: " + userId);
        }
    }

    // --- File operations & CSV Export embedded directly in UserManager (SRP Violation) ---

    public String exportUsersToCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Email,Tier,LoyaltyPoints,Active\n");
        for (User user : database.getUsers().values()) {
            csv.append(user.toCsvRow()).append("\n");
        }
        return csv.toString();
    }

    public void exportUsersToFile(String filePath) throws IOException {
        String csvContent = exportUsersToCsv();
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvContent);
        }
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(database.getUsers().values());
    }
}

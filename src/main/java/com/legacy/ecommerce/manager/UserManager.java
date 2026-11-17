package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.CustomerTier;
import com.legacy.ecommerce.model.User;

import java.io.IOException;
import java.util.*;

/**
 * Legacy God Class: UserManager.
 * Combines User CRUD, input validation, authentication, password rules,
 * loyalty tier updates, email dispatching, audit logging, and CSV export.
 */
public class UserManager {
    private final LegacyDatabase database = LegacyDatabase.getInstance();
    private final UserValidator userValidator = new UserValidator();
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final UserCsvExporter userCsvExporter = new UserCsvExporter();
    // Direct dependency instantiation (Tight coupling)
    private final NotificationService notificationService = new NotificationService();

    public User registerUser(String name, String email, String password, String address, String phoneNumber) {
        userValidator.validateRegistration(name, email, password);

        // Uniqueness check
        for (User existingUser : database.getUsers().values()) {
            if (existingUser.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Email already registered: " + email);
            }
        }

        String hashedPassword = passwordHasher.hash(password);

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
        for (User user : database.getUsers().values()) {
            if (user.getEmail().equalsIgnoreCase(email) && passwordHasher.matches(password, user.getPassword())) {
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

    public String exportUsersToCsv() {
        return userCsvExporter.exportUsersToCsv(getAllUsers());
    }

    public void exportUsersToFile(String filePath) throws IOException {
        userCsvExporter.exportUsersToFile(getAllUsers(), filePath);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(database.getUsers().values());
    }
}

package com.legacy.ecommerce.model;

import java.time.LocalDateTime;

/**
 * Legacy User entity.
 * Intentionally mixes data storage, password validation, loyalty logic, and CSV formatting.
 */
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String address;
    private String phoneNumber;
    private CustomerTier tier;
    private int loyaltyPoints;
    private boolean active;
    private LocalDateTime registeredAt;

    public User() {
        this.tier = CustomerTier.REGULAR;
        this.loyaltyPoints = 0;
        this.active = true;
        this.registeredAt = LocalDateTime.now();
    }

    public User(String id, String name, String email, String password, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.tier = CustomerTier.REGULAR;
        this.loyaltyPoints = 0;
        this.active = true;
        this.registeredAt = LocalDateTime.now();
    }

    // --- Embedded business & validation logic (SRP Violation) ---

    public boolean validatePassword(String inputPassword) {
        if (inputPassword == null || inputPassword.length() < 6) {
            return false;
        }
        boolean hasDigit = false;
        for (char c : inputPassword.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
                break;
            }
        }
        return hasDigit;
    }

    public void addLoyaltyPoints(int points) {
        if (points > 0) {
            this.loyaltyPoints += points;
            evaluateTierUpgrade();
        }
    }

    public void deductLoyaltyPoints(int points) {
        this.loyaltyPoints = Math.max(0, this.loyaltyPoints - points);
        // Note: Legacy bug or behavior - downgrading tier logic
        if (this.loyaltyPoints < 500) {
            this.tier = CustomerTier.REGULAR;
        } else if (this.loyaltyPoints < 1000) {
            this.tier = CustomerTier.SILVER;
        } else if (this.loyaltyPoints < 2000) {
            this.tier = CustomerTier.GOLD;
        }
    }

    private void evaluateTierUpgrade() {
        if (this.loyaltyPoints >= 2000) {
            this.tier = CustomerTier.PLATINUM;
        } else if (this.loyaltyPoints >= 1000) {
            this.tier = CustomerTier.GOLD;
        } else if (this.loyaltyPoints >= 500) {
            this.tier = CustomerTier.SILVER;
        } else {
            this.tier = CustomerTier.REGULAR;
        }
    }

    public double getTierDiscountRate() {
        switch (this.tier) {
            case PLATINUM:
                return 0.15; // 15% discount
            case GOLD:
                return 0.10; // 10% discount
            case SILVER:
                return 0.05; // 5% discount
            case REGULAR:
            default:
                return 0.0;
        }
    }

    // --- Embedded export / formatting logic (SRP Violation) ---
    public String toCsvRow() {
        return id + "," + name + "," + email + "," + tier + "," + loyaltyPoints + "," + active;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public void setTier(CustomerTier tier) {
        this.tier = tier;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}

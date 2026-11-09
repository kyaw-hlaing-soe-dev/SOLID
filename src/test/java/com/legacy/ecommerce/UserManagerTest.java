package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.UserManager;
import com.legacy.ecommerce.model.CustomerTier;
import com.legacy.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        userManager = new UserManager();
    }

    @Test
    @DisplayName("Should successfully register a new user with default REGULAR tier")
    void testRegisterUser_Success() {
        User user = userManager.registerUser("John Doe", "john@test.com", "Password123", "123 Elm St", "555-1234");

        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals(CustomerTier.REGULAR, user.getTier());
        assertEquals(0, user.getLoyaltyPoints());
        assertTrue(user.isActive());
        assertNotNull(userManager.getUserById(user.getId()));
    }

    @Test
    @DisplayName("Should reject registration if email is invalid or missing @ / dot")
    void testRegisterUser_InvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                userManager.registerUser("Bad Email", "invalidemail", "Pass123", "Addr", "555-0000"));
    }

    @Test
    @DisplayName("Should reject registration if password is weak (under 6 chars or no digits)")
    void testRegisterUser_WeakPassword() {
        // Less than 6 characters
        assertThrows(IllegalArgumentException.class, () ->
                userManager.registerUser("Short Pass", "short@test.com", "P12", "Addr", "555-0000"));

        // No digits
        assertThrows(IllegalArgumentException.class, () ->
                userManager.registerUser("No Digit", "nodigit@test.com", "Password", "Addr", "555-0000"));
    }

    @Test
    @DisplayName("Should reject registration if email is already taken")
    void testRegisterUser_DuplicateEmail() {
        userManager.registerUser("Alice", "alice@test.com", "Pass123", "Addr 1", "555-1111");

        assertThrows(IllegalArgumentException.class, () ->
                userManager.registerUser("Alice 2", "alice@test.com", "OtherPass99", "Addr 2", "555-2222"));
    }

    @Test
    @DisplayName("Should authenticate valid user credentials and reject invalid ones")
    void testAuthenticate_SuccessAndFailure() {
        userManager.registerUser("Login User", "login@test.com", "SecretPass1", "Addr", "555-3333");

        User authenticated = userManager.authenticate("login@test.com", "SecretPass1");
        assertNotNull(authenticated);
        assertEquals("Login User", authenticated.getName());

        assertNull(userManager.authenticate("login@test.com", "WrongPassword1"));
        assertNull(userManager.authenticate("nonexistent@test.com", "SecretPass1"));
    }

    @Test
    @DisplayName("Should upgrade tier according to loyalty points milestones (500->SILVER, 1000->GOLD, 2000->PLATINUM)")
    void testLoyaltyPointsAndTierUpgrade() {
        User user = userManager.registerUser("Loyal User", "loyal@test.com", "Pass123", "Addr", "555-4444");

        assertEquals(CustomerTier.REGULAR, user.getTier());

        userManager.addLoyaltyPoints(user.getId(), 500);
        assertEquals(CustomerTier.SILVER, user.getTier());
        assertEquals(0.05, user.getTierDiscountRate(), 0.001);

        userManager.addLoyaltyPoints(user.getId(), 500); // Total 1000
        assertEquals(CustomerTier.GOLD, user.getTier());
        assertEquals(0.10, user.getTierDiscountRate(), 0.001);

        userManager.addLoyaltyPoints(user.getId(), 1000); // Total 2000
        assertEquals(CustomerTier.PLATINUM, user.getTier());
        assertEquals(0.15, user.getTierDiscountRate(), 0.001);
    }

    @Test
    @DisplayName("Should export registered users into CSV format")
    void testExportUsersToCsv() {
        userManager.registerUser("User One", "one@test.com", "Pass111", "Addr 1", "555-1111");
        userManager.registerUser("User Two", "two@test.com", "Pass222", "Addr 2", "555-2222");

        String csv = userManager.exportUsersToCsv();
        assertTrue(csv.contains("ID,Name,Email,Tier,LoyaltyPoints,Active"));
        assertTrue(csv.contains("User One,one@test.com"));
        assertTrue(csv.contains("User Two,two@test.com"));
    }
}

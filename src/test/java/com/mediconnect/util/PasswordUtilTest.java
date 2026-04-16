package com.mediconnect.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashPassword_ShouldReturnValidHash() {
        String password = "mySecretPassword";
        String hash = PasswordUtil.hashPassword(password);
        
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    @Test
    public void testVerifyPassword_CorrectPassword_ShouldReturnTrue() {
        String password = "securePassword123";
        String hash = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hash), "Password verification should succeed with correct credentials.");
    }

    @Test
    public void testVerifyPassword_WrongPassword_ShouldReturnFalse() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtil.hashPassword(password);
        
        assertFalse(PasswordUtil.verifyPassword(wrongPassword, hash), "Password verification should fail with incorrect credentials.");
    }
}

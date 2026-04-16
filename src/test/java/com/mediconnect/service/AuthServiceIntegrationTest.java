package com.mediconnect.service;

import com.mediconnect.model.Patient;
import com.mediconnect.model.User;
import com.mediconnect.repository.FakeUserRepository;
import com.mediconnect.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceIntegrationTest {

    private AuthService authService;
    private FakeUserRepository fakeRepository;

    private final String TEST_EMAIL = "test@mediconnect.com";
    private final String TEST_PASSWORD = "Password123!";
    private final String HASHED_PASSWORD = PasswordUtil.hashPassword(TEST_PASSWORD);

    @BeforeEach
    void setup() {
        fakeRepository = new FakeUserRepository();
        authService = new AuthService(fakeRepository);
    }

    private User createMockUser(boolean active) {
        Patient user = new Patient();
        user.setEmail(TEST_EMAIL);
        user.setPassword(HASHED_PASSWORD);
        user.setActive(active);
        return user;
    }

    @Test
    @DisplayName("1. Succès de l'authentification")
    void testAuthenticateSuccess() {
        fakeRepository.setMockUser(createMockUser(true));
        Optional<User> result = authService.authenticate(TEST_EMAIL, TEST_PASSWORD);
        assertTrue(result.isPresent());
        assertEquals(TEST_EMAIL, result.get().getEmail());
    }

    @Test
    @DisplayName("2. Échec - Mauvais mot de passe")
    void testAuthenticateWrongPassword() {
        fakeRepository.setMockUser(createMockUser(true));
        Optional<User> result = authService.authenticate(TEST_EMAIL, "wrongpassword");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("3. Échec - Utilisateur inexistant")
    void testAuthenticateUserNotFound() {
        fakeRepository.setMockUser(null);
        Optional<User> result = authService.authenticate("ghost@mediconnect.com", TEST_PASSWORD);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("4. Échec - Utilisateur inactif")
    void testAuthenticateInactiveUser() {
        fakeRepository.setMockUser(createMockUser(false));
        Optional<User> result = authService.authenticate(TEST_EMAIL, TEST_PASSWORD);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("5. Succès - Insensibilité à la casse")
    void testAuthenticateEmailCaseInsensitivity() {
        fakeRepository.setMockUser(createMockUser(true));
        Optional<User> result = authService.authenticate(TEST_EMAIL.toUpperCase(), TEST_PASSWORD);
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("6. Succès - Trim de l'email")
    void testAuthenticateTrimmedEmail() {
        fakeRepository.setMockUser(createMockUser(true));
        Optional<User> result = authService.authenticate("  " + TEST_EMAIL + "  ", TEST_PASSWORD);
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("7. Échec - Email nul")
    void testAuthenticateNullEmail() {
        Optional<User> result = authService.authenticate(null, TEST_PASSWORD);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("8. Échec - Mot de passe nul")
    void testAuthenticateNullPassword() {
        Optional<User> result = authService.authenticate(TEST_EMAIL, null);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("9. Échec - Email vide")
    void testAuthenticateEmptyEmail() {
        Optional<User> result = authService.authenticate("", TEST_PASSWORD);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("10. Échec - Mot de passe vide")
    void testAuthenticateEmptyPassword() {
        Optional<User> result = authService.authenticate(TEST_EMAIL, "");
        assertFalse(result.isPresent());
    }
}

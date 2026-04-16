package com.mediconnect.repository;

import com.mediconnect.model.User;
import java.util.Optional;

/**
 * Fake repository for testing without Hibernate/Database.
 */
public class FakeUserRepository extends UserRepository {
    private User mockedUser;

    public void setMockUser(User user) {
        this.mockedUser = user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (mockedUser != null && mockedUser.getEmail().equalsIgnoreCase(email.trim())) {
            return Optional.of(mockedUser);
        }
        return Optional.empty();
    }
}

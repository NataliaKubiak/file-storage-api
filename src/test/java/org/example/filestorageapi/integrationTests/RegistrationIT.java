package org.example.filestorageapi.integrationTests;

import org.example.filestorageapi.entity.User;
import org.example.filestorageapi.errors.UserAlreadyExistException;
import org.example.filestorageapi.repository.UserRepository;
import org.example.filestorageapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class RegistrationIT extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

//    @BeforeEach
//    void setup() {
//        userRepository.deleteAll();
//    }

    @Test
    void testRegisterUser_Success() {
        User user = createUser("testUser");

        userService.registerUser(user);
        assertTrue(userRepository.findByUsername("testUser").isPresent());
    }

    @Test
    void testRegisterSameUser_shouldThrowUserAlreadyExistException() {
        User user = createUser("testUser");

        userService.registerUser(user);
        assertTrue(userRepository.findByUsername("testUser").isPresent());

        assertThrows(UserAlreadyExistException.class, () -> userService.registerUser(user));
    }

    private User createUser(String username) {
        return User.builder()
                .username(username)
                .encryptedPassword(username)
                .build();
    }
}

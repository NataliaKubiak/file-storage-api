package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.entity.User;
import org.example.filestorageapi.repository.UserRepository;
import org.example.filestorageapi.utils.Roles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void registerUser(User user) {
        user.setEncryptedPassword(passwordEncoder.encode(user.getEncryptedPassword()));
        user.setRoles(Roles.ROLE_USER);

        userRepository.save(user);
    }
}

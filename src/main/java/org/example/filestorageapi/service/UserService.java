package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.entity.User;
import org.example.filestorageapi.errors.UserAlreadyExistException;
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
    private final MinioService minioService;

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Integer getUserIdByUsername(String username) {
        return userRepository.findByUsername(username).get().getId();
    }

    @Transactional
    public void registerUser(User user) {
        if (findUserByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistException("User with name '" + user.getUsername() + "' already exists.");
        }

        user.setEncryptedPassword(passwordEncoder.encode(user.getEncryptedPassword()));
        user.setRoles(Roles.ROLE_USER);

        userRepository.save(user);
        minioService.createFolder("user-" + user.getId() + "-files");
    }
}

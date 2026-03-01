package com.barbershop.impl;

import com.barbershop.model.User;
import com.barbershop.repository.UserRepository;
import com.barbershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;
import com.barbershop.model.Role;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Override
    public User findOrCreateGuestUser(String email, String name) {
        User existingUser = userRepository.findByUsername(email);
        if (existingUser != null) {
            return existingUser;
        }

        User newGuest = new User();
        newGuest.setUsername(email);
        newGuest.setName(name);
        newGuest.setRole(Role.USER);
        String randomPassword = UUID.randomUUID().toString();
        newGuest.setPassword(passwordEncoder.encode(randomPassword));

        return userRepository.save(newGuest);
    }
}

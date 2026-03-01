package com.barbershop.service;

import com.barbershop.model.User;

public interface UserService {
    User saveUser(User user);
    User findOrCreateGuestUser(String email, String name);
}

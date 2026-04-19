package com.barbershop.controller;

import com.barbershop.model.User;
import com.barbershop.model.Role;
import com.barbershop.repository.UserRepository;
import com.barbershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        user.setRole(Role.USER);
        User saved = userService.saveUser(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/current")
    public ResponseEntity<?> currentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }

        String username = null;

        // 1. Проверяем, зашел ли пользователь через Google
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            username = oauth2User.getAttribute("email"); // Берем email от Google (мы его сохраняли как username)
        }
        // 2. Иначе это обычный вход по паролю
        else {
            username = authentication.getName();
        }

        // 3. Ищем пользователя в базе
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден в БД");
        }

        // 4. Прячем пароль ради безопасности перед отправкой в React
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }
}

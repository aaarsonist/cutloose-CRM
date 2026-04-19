package com.barbershop.service; // Проверь, чтобы пакет совпадал с твоим

import com.barbershop.model.Role;
import com.barbershop.model.User;
import com.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Получаем данные пользователя из Google
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Так как scopes настроены, мы гарантированно получим эти поля
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");

        if (email == null) {
            throw new OAuth2AuthenticationException("Google не вернул email. Проверьте настройки scopes в Google Cloud.");
        }

        // 2. Ищем пользователя с помощью твоего метода (возвращает User или null)
        User existingUser = userRepository.findByUsername(email);

        User user;
        if (existingUser != null) {
            // Пользователь найден
            user = existingUser;

            // Если он раньше регистрировался сам (с паролем), привязываем его Google ID
            if (user.getProvider() == null || user.getProvider().equals("LOCAL")) {
                user.setProvider("GOOGLE");
                user.setProviderId(googleId);
                userRepository.save(user);
            }
        } else {
            // 3. Пользователь не найден - создаем нового
            user = new User();
            user.setUsername(email); // Сохраняем почту от Google в твое поле username
            user.setName(name);      // Сохраняем реальное имя от Google
            user.setProvider("GOOGLE");
            user.setProviderId(googleId);

            // Устанавливаем роль по умолчанию
            user.setRole(Role.USER);

            userRepository.save(user);
        }

        // Возвращаем объект обратно в Spring Security для создания сессии
        return oauth2User;
    }
}
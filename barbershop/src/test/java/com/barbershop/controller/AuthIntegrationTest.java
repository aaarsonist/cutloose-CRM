package com.barbershop.controller;

import com.barbershop.model.Role;
import com.barbershop.model.User;
import com.barbershop.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ShouldReturn200Success() throws Exception {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("test@example.com");
        savedUser.setName("Test Client");
        savedUser.setRole(Role.USER);

        given(userService.saveUser(any(User.class))).willReturn(savedUser);

        // Используем Map для формирования JSON запроса (это надежнее безымянного Object)
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("name", "Test Client");
        registrationRequest.put("username", "test@example.com");
        registrationRequest.put("password", "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void accessSecuredEndpoint_WithoutAuth_ShouldReturn401() throws Exception {
        // Проверяем, что неавторизованного пользователя не пускает в личный кабинет
        mockMvc.perform(get("/api/users/current"))
                .andExpect(status().isUnauthorized());
    }
}
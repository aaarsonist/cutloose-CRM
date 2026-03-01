package com.barbershop.controller;

import com.barbershop.model.Role;
import com.barbershop.model.ServiceEntity;
import com.barbershop.model.User;
import com.barbershop.service.ServiceService;
import com.barbershop.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BarbershopIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceService serviceService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getServices_ShouldReturn200AndList() throws Exception {
        ServiceEntity service = new ServiceEntity();
        service.setName("Мужская стрижка");
        service.setPrice(40.0);
        given(serviceService.getAllServices()).willReturn(List.of(service));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Мужская стрижка"));
    }

    @Test
    @WithMockUser(username = "client", roles = "USER")
    void accessAdminEndpoint_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/forecast/weekly"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerUser_ShouldReturnSuccess() throws Exception {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("test@example.com");
        savedUser.setName("Test Client");
        savedUser.setRole(Role.USER);

        given(userService.saveUser(any(User.class))).willReturn(savedUser);

        Object registrationRequest = new Object() {
            public String name = "Test Client";
            public String username = "test@example.com";
            public String password = "password123";
        };

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());
    }
}
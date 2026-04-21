package com.barbershop.controller;

import com.barbershop.model.Role;
import com.barbershop.model.User;
import com.barbershop.repository.UserRepository; // Импортируем репозиторий
import com.barbershop.service.TimetableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

// Правильные статические импорты
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Мокаем напрямую репозиторий, раз в сервисе метода нет
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TimetableService timetableService;

    @Test
    @WithMockUser(username = "test_user", roles = "USER")
    void createBooking_ShouldReturnSuccess_WhenAuthorized() throws Exception {
        // 1. ПОДГОТОВКА: Создаем объект пользователя, который якобы уже есть в БД
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
        mockUser.setRole(Role.USER);

        // Говорим моку возвращать этого юзера, когда контроллер его запросит
        when(userRepository.findByUsername("test_user")).thenReturn(mockUser);

        // 2. ДАННЫЕ ЗАПРОСА (взяты из твоего лога)
        Map<String, Object> bookingRequest = new HashMap<>();
        bookingRequest.put("masterId", 2);
        bookingRequest.put("serviceId", 1);
        bookingRequest.put("appointmentTime", "2026-05-15T14:30:00");

        // 3. ВЫПОЛНЕНИЕ
        mockMvc.perform(post("/api/timetable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk()); // Теперь вернет 200, так как юзер в базе "найден"
    }
}
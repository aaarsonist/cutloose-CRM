package com.barbershop.controller;

import com.barbershop.dto.report.CustomerKpiDto;
import com.barbershop.service.CustomerAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminReportsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerAnalyticsService customerAnalyticsService;

    // Базовый путь из твоего контроллера
    private final String BASE_URL = "/api/admin/customer-analytics";

    @Test
    @WithMockUser(roles = "USER")
    void getKpi_AsUser_ShouldReturn403Forbidden() throws Exception {
        // Обычный пользователь не имеет доступа из-за @PreAuthorize("hasRole('ADMIN')")
        mockMvc.perform(get(BASE_URL + "/kpi")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-04-19"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getKpi_AsAdmin_ShouldReturn200Ok() throws Exception {
        // Мокаем ответ сервиса
        given(customerAnalyticsService.getCustomerKpi(any(), any())).willReturn(new CustomerKpiDto());

        // Проверяем доступ для админа
        mockMvc.perform(get(BASE_URL + "/kpi")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-04-19"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAtRiskClients_AsAdmin_ShouldReturn200Ok() throws Exception {
        // Тестируем еще один эндпоинт этого контроллера
        given(customerAnalyticsService.getAtRiskClients()).willReturn(new ArrayList<>());

        mockMvc.perform(get(BASE_URL + "/at-risk"))
                .andExpect(status().isOk());
    }
}
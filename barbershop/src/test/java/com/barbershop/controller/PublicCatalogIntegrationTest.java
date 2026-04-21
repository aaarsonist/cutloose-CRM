package com.barbershop.controller;

import com.barbershop.model.ServiceEntity;
import com.barbershop.service.ServiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCatalogIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ServiceService serviceService;

    @Test
    void getServices_ShouldReturn200() throws Exception {
        given(serviceService.getAllServices()).willReturn(List.of(new ServiceEntity()));
        // Убрал /api, так как в твоем SecurityConfig доступ открыт для "/services/**"
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk());
    }
}
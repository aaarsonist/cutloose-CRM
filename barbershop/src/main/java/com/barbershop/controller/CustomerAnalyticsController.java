package com.barbershop.controller;

import com.barbershop.dto.report.AtRiskClientDto;
import com.barbershop.dto.report.ClientActionDto;
import com.barbershop.dto.report.ContactResultDto;
import com.barbershop.dto.report.CustomerKpiDto;
import com.barbershop.service.CustomerAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/customer-analytics")
@PreAuthorize("hasRole('ADMIN')") // <-- Исправлено: теперь используется hasRole, как в твоем SecurityConfig
public class CustomerAnalyticsController {

    private final CustomerAnalyticsService customerAnalyticsService;

    public CustomerAnalyticsController(CustomerAnalyticsService customerAnalyticsService) {
        this.customerAnalyticsService = customerAnalyticsService;
    }

    @GetMapping("/kpi")
    public ResponseEntity<CustomerKpiDto> getCustomerKpi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(customerAnalyticsService.getCustomerKpi(startDate, endDate));
    }

    @GetMapping("/at-risk")
    public ResponseEntity<List<AtRiskClientDto>> getAtRiskClients() {
        return ResponseEntity.ok(customerAnalyticsService.getAtRiskClients());
    }

    @GetMapping("/client-action/{clientId}")
    public ResponseEntity<ClientActionDto> getClientActionData(@PathVariable Long clientId) {
        return ResponseEntity.ok(customerAnalyticsService.getClientActionData(clientId));
    }

    @PostMapping("/client-action/{clientId}/result")
    public ResponseEntity<Void> saveContactResult(
            @PathVariable Long clientId,
            @RequestBody ContactResultDto resultDto) {

        System.out.println("LOG: Контакт с клиентом " + clientId + ". Статус: " + resultDto.getStatus() + ". Заметка: " + resultDto.getNotes());
        return ResponseEntity.ok().build();
    }
}
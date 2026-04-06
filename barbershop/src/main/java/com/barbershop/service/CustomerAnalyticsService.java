package com.barbershop.service;

import com.barbershop.dto.report.CustomerKpiDto;
import java.time.LocalDate;

public interface CustomerAnalyticsService {
    CustomerKpiDto getCustomerKpi(LocalDate startDate, LocalDate endDate);
    java.util.List<com.barbershop.dto.report.AtRiskClientDto> getAtRiskClients();
    com.barbershop.dto.report.ClientActionDto getClientActionData(Long clientId);
    void saveContactResult(Long clientId, com.barbershop.dto.report.ContactResultDto resultDto);
    java.util.List<com.barbershop.dto.report.InteractionDto> getRecentInteractions();
}
package com.barbershop.dto.report;

import java.time.LocalDateTime;

public class AtRiskClientDto {
    private Long clientId;
    private String clientName;
    private LocalDateTime lastVisitDate;
    private Double ltv;
    private Integer churnProbability;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public LocalDateTime getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(LocalDateTime lastVisitDate) { this.lastVisitDate = lastVisitDate; }

    public Double getLtv() { return ltv; }
    public void setLtv(Double ltv) { this.ltv = ltv; }

    public Integer getChurnProbability() { return churnProbability; }
    public void setChurnProbability(Integer churnProbability) { this.churnProbability = churnProbability; }
}
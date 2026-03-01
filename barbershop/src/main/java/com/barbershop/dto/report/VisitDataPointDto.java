package com.barbershop.dto.report;

import java.time.LocalDate;
import java.util.Map;

public class VisitDataPointDto {

    private LocalDate date;

    private Long totalVisits;

    private Map<String, Long> serviceVisitCounts;
    public VisitDataPointDto() {}
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Long getTotalVisits() {
        return totalVisits;
    }
    public void setTotalVisits(Long totalVisits) {
        this.totalVisits = totalVisits;
    }
    public Map<String, Long> getServiceVisitCounts() {
        return serviceVisitCounts;
    }
    public void setServiceVisitCounts(Map<String, Long> serviceVisitCounts) {
        this.serviceVisitCounts = serviceVisitCounts;
    }
}
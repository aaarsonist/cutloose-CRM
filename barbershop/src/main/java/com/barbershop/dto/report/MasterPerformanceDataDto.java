package com.barbershop.dto.report;

import java.math.BigDecimal;

public class MasterPerformanceDataDto {

    private Long masterId;
    private String masterFullName;
    private Long appointmentCount;
    private BigDecimal totalRevenue;
    private Double averageRating;

    public MasterPerformanceDataDto() {}

    public MasterPerformanceDataDto(Long masterId, String masterFullName, Long appointmentCount, BigDecimal totalRevenue, Double averageRating) {
        this.masterId = masterId;
        this.masterFullName = masterFullName;
        this.appointmentCount = appointmentCount;
        this.totalRevenue = totalRevenue;
        this.averageRating = averageRating;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

    public String getMasterFullName() {
        return masterFullName;
    }

    public void setMasterFullName(String masterFullName) {
        this.masterFullName = masterFullName;
    }

    public Long getAppointmentCount() {
        return appointmentCount;
    }

    public void setAppointmentCount(Long appointmentCount) {
        this.appointmentCount = appointmentCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
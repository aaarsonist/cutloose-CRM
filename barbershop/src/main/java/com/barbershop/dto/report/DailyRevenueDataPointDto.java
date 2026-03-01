package com.barbershop.dto.report;

import java.time.LocalDate;
import java.math.BigDecimal;

public class DailyRevenueDataPointDto {

    private LocalDate date;
    private BigDecimal totalRevenue;

    public DailyRevenueDataPointDto() {}

    public DailyRevenueDataPointDto(LocalDate date, BigDecimal totalRevenue) {
        this.date = date;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
package com.barbershop.dto.report;

import java.util.List;


public class SalesReportDataDto {

    private List<DailyRevenueDataPointDto> dailyRevenueDataPoints;

    public SalesReportDataDto() {}

    public SalesReportDataDto(List<DailyRevenueDataPointDto> dailyRevenueDataPoints) {
        this.dailyRevenueDataPoints = dailyRevenueDataPoints;
    }

    public List<DailyRevenueDataPointDto> getDailyRevenueDataPoints() {
        return dailyRevenueDataPoints;
    }

    public void setDailyRevenueDataPoints(List<DailyRevenueDataPointDto> dailyRevenueDataPoints) {
        this.dailyRevenueDataPoints = dailyRevenueDataPoints;
    }
}
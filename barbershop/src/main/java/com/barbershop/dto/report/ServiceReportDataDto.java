package com.barbershop.dto.report;

import java.util.List;

public class ServiceReportDataDto {

    private List<VisitDataPointDto> visitData;

    private List<AverageRatingDto> averageRatings;

    public ServiceReportDataDto() {}

    public List<VisitDataPointDto> getVisitData() {
        return visitData;
    }

    public void setVisitData(List<VisitDataPointDto> visitData) {
        this.visitData = visitData;
    }

    public List<AverageRatingDto> getAverageRatings() {
        return averageRatings;
    }

    public void setAverageRatings(List<AverageRatingDto> averageRatings) {
        this.averageRatings = averageRatings;
    }
}
package com.barbershop.dto.report;

import java.time.LocalDateTime;

public class VisitHistoryDto {
    private LocalDateTime date;
    private String serviceName;
    private String masterName;
    private Double price;
    private Integer reviewRating;
    private String reviewText;

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getMasterName() { return masterName; }
    public void setMasterName(String masterName) { this.masterName = masterName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getReviewRating() { return reviewRating; }
    public void setReviewRating(Integer reviewRating) { this.reviewRating = reviewRating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}
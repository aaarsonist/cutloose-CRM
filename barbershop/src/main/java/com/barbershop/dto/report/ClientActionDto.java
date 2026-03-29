package com.barbershop.dto.report;

public class ClientActionDto {
    private Long clientId;
    private String clientName;
    private String contactInfo;
    private String favoriteMaster;
    private String favoriteService;
    private String recommendation;
    private Double ltv;

    // Getters and Setters
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getFavoriteMaster() { return favoriteMaster; }
    public void setFavoriteMaster(String favoriteMaster) { this.favoriteMaster = favoriteMaster; }

    public String getFavoriteService() { return favoriteService; }
    public void setFavoriteService(String favoriteService) { this.favoriteService = favoriteService; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public Double getLtv() { return ltv; }
    public void setLtv(Double ltv) { this.ltv = ltv; }
}
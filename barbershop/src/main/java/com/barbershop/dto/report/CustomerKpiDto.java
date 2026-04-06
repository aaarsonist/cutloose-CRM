package com.barbershop.dto.report;

import java.util.List;

public class CustomerKpiDto {
    private Double averageLtv;
    private Double previousAverageLtv;

    private Integer clientsAtRisk;
    private Double churnRate; // <-- Новое поле вместо retainedRevenue

    // Данные для круговой диаграммы (Сегментация)
    private Integer newClients;
    private Integer activeClients;
    private Integer atRiskClientsSegment;
    private Integer churnedClients;

    // Данные для линейного графика (Динамика)
    private List<String> dynamicsDates;
    private List<Double> dynamicsLtv;
    private List<Double> dynamicsChurnRate;

    // Getters and Setters
    public Double getAverageLtv() { return averageLtv; }
    public void setAverageLtv(Double averageLtv) { this.averageLtv = averageLtv; }

    public Double getPreviousAverageLtv() { return previousAverageLtv; }
    public void setPreviousAverageLtv(Double previousAverageLtv) { this.previousAverageLtv = previousAverageLtv; }

    public Integer getClientsAtRisk() { return clientsAtRisk; }
    public void setClientsAtRisk(Integer clientsAtRisk) { this.clientsAtRisk = clientsAtRisk; }

    public Double getChurnRate() { return churnRate; }
    public void setChurnRate(Double churnRate) { this.churnRate = churnRate; }

    public Integer getNewClients() { return newClients; }
    public void setNewClients(Integer newClients) { this.newClients = newClients; }

    public Integer getActiveClients() { return activeClients; }
    public void setActiveClients(Integer activeClients) { this.activeClients = activeClients; }

    public Integer getAtRiskClientsSegment() { return atRiskClientsSegment; }
    public void setAtRiskClientsSegment(Integer atRiskClientsSegment) { this.atRiskClientsSegment = atRiskClientsSegment; }

    public Integer getChurnedClients() { return churnedClients; }
    public void setChurnedClients(Integer churnedClients) { this.churnedClients = churnedClients; }

    public List<String> getDynamicsDates() { return dynamicsDates; }
    public void setDynamicsDates(List<String> dynamicsDates) { this.dynamicsDates = dynamicsDates; }

    public List<Double> getDynamicsLtv() { return dynamicsLtv; }
    public void setDynamicsLtv(List<Double> dynamicsLtv) { this.dynamicsLtv = dynamicsLtv; }

    public List<Double> getDynamicsChurnRate() { return dynamicsChurnRate; }
    public void setDynamicsChurnRate(List<Double> dynamicsChurnRate) { this.dynamicsChurnRate = dynamicsChurnRate; }
}
package com.barbershop.dto.report;

public class ContactResultDto {
    private String status; // "CONTACTED" или "CHURNED"
    private String notes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
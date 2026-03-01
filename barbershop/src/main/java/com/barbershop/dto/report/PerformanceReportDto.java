package com.barbershop.dto.report;

import java.time.LocalDateTime;

public class PerformanceReportDto {

    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private ServiceReportDataDto serviceReportData;
    private MasterReportDataDto masterReportData;
    private SalesReportDataDto salesReportData;

    public PerformanceReportDto() {}

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public ServiceReportDataDto getServiceReportData() {
        return serviceReportData;
    }

    public void setServiceReportData(ServiceReportDataDto serviceReportData) {
        this.serviceReportData = serviceReportData;
    }
    public MasterReportDataDto getMasterReportData() {
        return masterReportData;
    }

    public void setMasterReportData(MasterReportDataDto masterReportData) {
        this.masterReportData = masterReportData;
    }

    public SalesReportDataDto getSalesReportData() {
        return salesReportData;
    }

    public void setSalesReportData(SalesReportDataDto salesReportData) {
        this.salesReportData = salesReportData;
    }
}
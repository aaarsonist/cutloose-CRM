package com.barbershop.dto.report;

import java.util.List;

public class MasterReportDataDto {

    private List<MasterPerformanceDataDto> masterPerformanceData;

    public MasterReportDataDto() {}

    public MasterReportDataDto(List<MasterPerformanceDataDto> masterPerformanceData) {
        this.masterPerformanceData = masterPerformanceData;
    }

    public List<MasterPerformanceDataDto> getMasterPerformanceData() {
        return masterPerformanceData;
    }

    public void setMasterPerformanceData(List<MasterPerformanceDataDto> masterPerformanceData) {
        this.masterPerformanceData = masterPerformanceData;
    }
}
package com.barbershop.dto.report;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class ExtendedAnalyticsDto {
    private String topMasterName;
    private Double topMasterRevenue;
    private Double topMasterRating;

    private Double totalRevenue;
    private Double totalRevenueTrend;

    private Double retentionRate;

    private Map<String, Long> categoryDistribution;

    private List<ServiceUsageDto> topServices;

    @Data
    public static class ServiceUsageDto {
        private String serviceName;
        private Long usageCount;

        public ServiceUsageDto(String serviceName, Long usageCount) {
            this.serviceName = serviceName;
            this.usageCount = usageCount;
        }
    }
}
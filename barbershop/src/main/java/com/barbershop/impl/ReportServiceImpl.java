package com.barbershop.impl;

import com.barbershop.dto.report.AverageRatingDto;
import com.barbershop.dto.report.PerformanceReportDto;
import com.barbershop.dto.report.ServiceReportDataDto;
import com.barbershop.dto.report.VisitDataPointDto;
import com.barbershop.dto.report.MasterPerformanceDataDto;
import com.barbershop.dto.report.MasterReportDataDto;
import com.barbershop.dto.report.DailyRevenueDataPointDto;
import com.barbershop.dto.report.SalesReportDataDto;
import com.barbershop.dto.report.ExtendedAnalyticsDto;
import com.barbershop.repository.MasterRepository;
import com.barbershop.repository.ReviewRepository;
import com.barbershop.repository.ServiceRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TimetableRepository timetableRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private MasterRepository masterRepository;

    @Autowired
    public ReportServiceImpl(TimetableRepository timetableRepository, ReviewRepository reviewRepository) {
        this.timetableRepository = timetableRepository;
        this.reviewRepository = reviewRepository;
    }
    @Override
    public PerformanceReportDto getPerformanceReport(LocalDateTime startDate, LocalDateTime endDate, String reportType, List<Long> serviceIds, List<Long> masterIds) {
        PerformanceReportDto report = new PerformanceReportDto();
        report.setReportType(reportType);
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        if ("services".equals(reportType)) {
            report.setServiceReportData(getServicePerformanceReportData(startDate, endDate, serviceIds, masterIds));
        }
        if ("masters".equals(reportType)) {
            System.out.println(">>> DEBUG: ReportServiceImpl calling getMasterPerformanceReportData with startDate: " + startDate + ", endDate: " + endDate + ", masterIds: " + masterIds);
            report.setMasterReportData(getMasterPerformanceReportData(startDate, endDate, masterIds, serviceIds));
        }
        else if ("sales".equals(reportType)) {
            System.out.println(">>> DEBUG: ReportServiceImpl calling getSalesReportData with startDate: " + startDate + ", endDate: " + endDate + ", serviceIds: " + serviceIds + ", masterIds: " + masterIds);
            report.setSalesReportData(getSalesReportData(startDate, endDate, serviceIds, masterIds));
        }
        return report;
    }

    private ServiceReportDataDto getServicePerformanceReportData(LocalDateTime startDate, LocalDateTime endDate, List<Long> serviceIds, List<Long> masterIds) {
        ServiceReportDataDto serviceReportData = new ServiceReportDataDto();
        List<Object[]> visitCountsRaw = timetableRepository.countVisitsByDayAndService(startDate, endDate, serviceIds, masterIds);

        Map<LocalDate, List<Object[]>> visitsByDate = visitCountsRaw.stream()
                .collect(Collectors.groupingBy(
                        row -> ((java.sql.Date) row[0]).toLocalDate()
                ));

        List<VisitDataPointDto> visitDataPoints = visitsByDate.entrySet().stream()
                .map(entry -> {
                    VisitDataPointDto dto = new VisitDataPointDto();
                    dto.setDate(entry.getKey());

                    Map<String, Long> serviceVisitCounts = entry.getValue().stream()
                            .collect(Collectors.toMap(
                                    row -> (String) row[1],
                                    row -> (Long) row[2]
                            ));
                    dto.setServiceVisitCounts(serviceVisitCounts);

                    dto.setTotalVisits(serviceVisitCounts.values().stream().mapToLong(Long::longValue).sum());

                    return dto;
                })
                .sorted(Comparator.comparing(VisitDataPointDto::getDate))
                .collect(Collectors.toList());

        serviceReportData.setVisitData(visitDataPoints);

        List<Object[]> averageRatingsData = reviewRepository.findAverageRatingByServiceWithinPeriod(startDate, endDate, serviceIds, masterIds); // <-- Вызов репозитория с фильтрами

        List<AverageRatingDto> averageRatings = averageRatingsData.stream()
                .map(row -> {
                    AverageRatingDto dto = new AverageRatingDto();
                    dto.setServiceId((Long) row[0]);
                    dto.setServiceName((String) row[1]);
                    dto.setAverageRating((Double) row[2]);
                    return dto;
                })
                .collect(Collectors.toList());

        serviceReportData.setAverageRatings(averageRatings);

        return serviceReportData;
    }

    private MasterReportDataDto getMasterPerformanceReportData(LocalDateTime startDate, LocalDateTime endDate, List<Long> masterIds, List<Long> serviceIds) {
        if (serviceIds != null && serviceIds.isEmpty()) serviceIds = null;
        List<Object[]> countDataRaw = timetableRepository.countAppointmentsByMaster(startDate, endDate, masterIds, serviceIds);

        List<Object[]> revenueDataRaw = timetableRepository.sumServicePricesByMaster(startDate, endDate, masterIds, serviceIds);

        List<Object[]> ratingDataRaw = reviewRepository.findAverageRatingByMasterWithinPeriod(startDate, endDate, masterIds, serviceIds);

        Map<Long, MasterPerformanceDataDto> masterDataMap = new HashMap<>();

        for (Object[] row : countDataRaw) {
            Long masterId = (Long) row[0];
            String masterName = (String) row[1];
            Long count = (Long) row[2];
            masterDataMap.put(masterId, new MasterPerformanceDataDto(masterId, masterName, count, BigDecimal.ZERO, null));
        }

        for (Object[] row : revenueDataRaw) {
            Long masterId = (Long) row[0];
            Object rawRevenue = row[2];

            BigDecimal revenue;
            if (rawRevenue instanceof Number) {
                revenue = BigDecimal.valueOf(((Number) rawRevenue).doubleValue());
            } else {
                revenue = BigDecimal.ZERO;
                System.err.println(">>> WARNING: Unexpected type for revenue: " + rawRevenue);
            }

            MasterPerformanceDataDto dto = masterDataMap.get(masterId);
            if (dto != null) {
                dto.setTotalRevenue(revenue);
            }
        }

        for (Object[] row : ratingDataRaw) {
            Long masterId = (Long) row[0];
            Double rating = (Double) row[2];
            MasterPerformanceDataDto dto = masterDataMap.get(masterId);
            if (dto != null) {
                dto.setAverageRating(rating);
            }
        }

        List<MasterPerformanceDataDto> masterPerformanceDataList = new ArrayList<>(masterDataMap.values());

        masterPerformanceDataList.sort(Comparator.comparing(MasterPerformanceDataDto::getMasterFullName));

        MasterReportDataDto masterReportData = new MasterReportDataDto();
        masterReportData.setMasterPerformanceData(masterPerformanceDataList);

        return masterReportData;
    }

    private SalesReportDataDto getSalesReportData(LocalDateTime startDate, LocalDateTime endDate, List<Long> serviceIds, List<Long> masterIds) {
        List<Object[]> dailyRevenueRaw = timetableRepository.sumServicePricesByDay(startDate, endDate, serviceIds, masterIds);

        List<DailyRevenueDataPointDto> dailyRevenueDataPoints = dailyRevenueRaw.stream()
                .map(row -> {
                    DailyRevenueDataPointDto dto = new DailyRevenueDataPointDto();
                    if (row[0] instanceof java.sql.Date) {
                        dto.setDate(((java.sql.Date) row[0]).toLocalDate());
                    } else if (row[0] instanceof LocalDate) {
                        dto.setDate((LocalDate) row[0]);
                    } else {
                        System.err.println(">>> WARNING: Unexpected date type in daily revenue: " + row[0]);
                        return null;
                    }

                    Object rawRevenue = row[1];
                    BigDecimal revenue;
                    if (rawRevenue instanceof Number) {
                        revenue = BigDecimal.valueOf(((Number) rawRevenue).doubleValue());
                    } else {
                        revenue = BigDecimal.ZERO;
                        System.err.println(">>> WARNING: Unexpected revenue type in daily revenue: " + rawRevenue);
                    }
                    dto.setTotalRevenue(revenue);

                    return dto;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(DailyRevenueDataPointDto::getDate))
                .collect(Collectors.toList());

        SalesReportDataDto salesReportData = new SalesReportDataDto();
        salesReportData.setDailyRevenueDataPoints(dailyRevenueDataPoints);

        return salesReportData;
    }
    @Override
    public SalesReportDataDto getSalesData(LocalDateTime startDate, LocalDateTime endDate, List<Long> serviceIds, List<Long> masterIds) {
        return getSalesReportData(startDate, endDate, serviceIds, masterIds);
    }

    @Override
    public ServiceReportDataDto getServiceData(LocalDateTime startDate, LocalDateTime endDate, List<Long> serviceIds, List<Long> masterIds) {
        return getServicePerformanceReportData(startDate, endDate, serviceIds, masterIds);
    }

    @Override
    public MasterReportDataDto getMasterData(LocalDateTime startDate, LocalDateTime endDate, List<Long> masterIds, List<Long> serviceIds) {
        return getMasterPerformanceReportData(startDate, endDate, masterIds, serviceIds);
    }

    @Override
    public ExtendedAnalyticsDto getExtendedAnalytics(LocalDateTime start, LocalDateTime end) {
        ExtendedAnalyticsDto dto = new ExtendedAnalyticsDto();

        List<Object[]> mastersData = timetableRepository.getMastersPerformanceData(start, end);
        if (!mastersData.isEmpty()) {
            Object[] top = mastersData.get(0);
            dto.setTopMasterName((String) top[0]);
            dto.setTopMasterRating(top[1] != null ? ((Number) top[1]).doubleValue() : 0.0);
            dto.setTopMasterRevenue(top[3] != null ? ((Number) top[3]).doubleValue() : 0.0);
        }

        List<Object[]> currentStatsList = timetableRepository.getRevenueAndCount(start, end);
        Object[] currentStats = currentStatsList.isEmpty() ? new Object[]{0.0, 0L} : currentStatsList.get(0);

        Double currentRev = currentStats[0] != null ? ((Number) currentStats[0]).doubleValue() : 0.0;
        dto.setTotalRevenue(currentRev);

        long days = ChronoUnit.DAYS.between(start, end);
        LocalDateTime prevStart = start.minusDays(days);
        LocalDateTime prevEnd = start;

        List<Object[]> prevStatsList = timetableRepository.getRevenueAndCount(prevStart, prevEnd);
        Object[] prevStats = prevStatsList.isEmpty() ? new Object[]{0.0, 0L} : prevStatsList.get(0);

        Double prevRev = prevStats[0] != null ? ((Number) prevStats[0]).doubleValue() : 0.0;

        if (prevRev > 0) {
            double trend = ((currentRev - prevRev) / prevRev) * 100.0;
            dto.setTotalRevenueTrend(trend);
        } else {
            dto.setTotalRevenueTrend(null);
        }

        Long returning = timetableRepository.countReturningClients(start, end);
        Long total = timetableRepository.countTotalClientsInPeriod(start, end);
        if (total != null && total > 0) {
            dto.setRetentionRate((double) returning / total * 100.0);
        } else {
            dto.setRetentionRate(0.0);
        }

        List<Object[]> catData = timetableRepository.getCategoryDistribution(start, end);
        Map<String, Long> catMap = new HashMap<>();
        for (Object[] row : catData) {
            catMap.put(row[0].toString(), (Long) row[1]);
        }
        dto.setCategoryDistribution(catMap);

        List<Object[]> servicesData = timetableRepository.getTopServices(start, end);
        List<ExtendedAnalyticsDto.ServiceUsageDto> topServices = new ArrayList<>();
        for (Object[] row : servicesData) {
            topServices.add(new ExtendedAnalyticsDto.ServiceUsageDto((String) row[0], (Long) row[1]));
        }
        dto.setTopServices(topServices);

        return dto;
    }
}
package com.barbershop.controller;

import com.barbershop.dto.report.*;
import com.barbershop.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    private LocalDateTime parseDate(Optional<LocalDateTime> date) {
        return date.orElse(null);
    }

    private List<Long> parseIds(Optional<List<Long>> ids) {
        if (ids.isPresent() && ids.get().isEmpty()) {
            return null;
        }
        return ids.orElse(null);
    }
    @GetMapping("/sales")
    public ResponseEntity<SalesReportDataDto> getSalesData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate,
            @RequestParam Optional<List<Long>> serviceIds,
            @RequestParam Optional<List<Long>> masterIds
    ) {
        SalesReportDataDto data = reportService.getSalesData(
                parseDate(startDate), parseDate(endDate), parseIds(serviceIds), parseIds(masterIds)
        );
        return ResponseEntity.ok(data);
    }

    @GetMapping("/services")
    public ResponseEntity<ServiceReportDataDto> getServiceData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate,
            @RequestParam Optional<List<Long>> serviceIds,
            @RequestParam Optional<List<Long>> masterIds
    ) {
        ServiceReportDataDto data = reportService.getServiceData(
                parseDate(startDate), parseDate(endDate), parseIds(serviceIds), parseIds(masterIds)
        );
        return ResponseEntity.ok(data);
    }

    @GetMapping("/masters")
    public ResponseEntity<MasterReportDataDto> getMasterData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate,
            @RequestParam Optional<List<Long>> serviceIds,
            @RequestParam Optional<List<Long>> masterIds
    ) {
        MasterReportDataDto data = reportService.getMasterData(
                parseDate(startDate), parseDate(endDate), parseIds(masterIds), parseIds(serviceIds)
        );
        return ResponseEntity.ok(data);
    }

    @GetMapping("/extended")
    public ResponseEntity<ExtendedAnalyticsDto> getExtendedAnalytics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reportService.getExtendedAnalytics(startDate, endDate));
    }
}
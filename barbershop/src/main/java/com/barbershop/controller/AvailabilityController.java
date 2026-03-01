package com.barbershop.controller;

import com.barbershop.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime; // <-- ДОБАВИТЬ ЭТОТ ИМПОРТ
import java.time.format.DateTimeFormatter; // <-- ДОБАВИТЬ ЭТОТ ИМПОРТ
import java.util.List;
import java.util.stream.Collectors; // <-- ДОБАВИТЬ ЭТОТ ИМПОРТ

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @GetMapping
    public ResponseEntity<List<String>> getAvailability(
            @RequestParam Long masterId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        List<LocalTime> localTimeSlots = availabilityService.getAvailableSlots(masterId, serviceId, date);

        List<String> stringSlots = localTimeSlots.stream()
                .map(slot -> slot.format(TIME_FORMATTER))
                .collect(Collectors.toList());

        return ResponseEntity.ok(stringSlots);
    }
}
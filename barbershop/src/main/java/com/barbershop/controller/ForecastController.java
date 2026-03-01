package com.barbershop.controller;

import com.barbershop.dto.forecast.ForecastDto;
import com.barbershop.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forecast")
@PreAuthorize("hasRole('ADMIN')")
public class ForecastController {

    @Autowired
    private ForecastService forecastService;

    @GetMapping("/weekly")
    public ResponseEntity<List<ForecastDto>> getWeeklyForecast() {
        List<ForecastDto> forecast = forecastService.getWeeklyForecast();
        return ResponseEntity.ok(forecast);
    }
}
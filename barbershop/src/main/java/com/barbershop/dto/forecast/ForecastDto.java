package com.barbershop.dto.forecast;

import lombok.Data;
import java.time.DayOfWeek;

@Data
public class ForecastDto {

    private DayOfWeek dayOfWeek;
    private String dayOfWeekRussian;

    private double supplyHours;
    private double demandHours;
    private double occupancy;

    private String recommendationLevel;
    private String recommendationText;
}
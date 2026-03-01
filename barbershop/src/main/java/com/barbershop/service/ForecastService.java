package com.barbershop.service;

import com.barbershop.dto.forecast.ForecastDto;
import java.util.List;

public interface ForecastService {
    List<ForecastDto> getWeeklyForecast();
}
package com.barbershop.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityService {

    List<LocalTime> getAvailableSlots(Long masterId, Long serviceId, LocalDate date);
}

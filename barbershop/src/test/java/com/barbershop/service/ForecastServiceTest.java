package com.barbershop.service;

import com.barbershop.dto.forecast.ForecastDto;
import com.barbershop.impl.ForecastServiceImpl;
import com.barbershop.model.BookingStatus;
import com.barbershop.model.ServiceEntity;
import com.barbershop.model.Timetable;
import com.barbershop.model.WorkSchedule;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.WorkScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @InjectMocks
    private ForecastServiceImpl forecastService;

    @Test
    void getWeeklyForecast_ShouldReturnCriticalRecommendation_WhenOccupancyIsHigh() {

        WorkSchedule schedule = new WorkSchedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));

        when(workScheduleRepository.findAll()).thenReturn(List.of(schedule));

        ServiceEntity service = new ServiceEntity();
        service.setDuration(60);

        Timetable booking = new Timetable();
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setService(service);
        booking.setAppointmentTime(LocalDateTime.of(2023, 10, 2, 9, 0));

        when(timetableRepository.findAll()).thenReturn(List.of(booking));

        when(timetableRepository.findMinAppointmentTime()).thenReturn(Optional.of(LocalDateTime.of(2023, 10, 1, 0, 0)));
        when(timetableRepository.findMaxAppointmentTime()).thenReturn(Optional.of(LocalDateTime.of(2023, 10, 7, 0, 0)));

        List<ForecastDto> result = forecastService.getWeeklyForecast();

        ForecastDto mondayForecast = result.stream()
                .filter(dto -> dto.getDayOfWeek() == DayOfWeek.MONDAY)
                .findFirst()
                .orElseThrow();

        assertEquals(1.0, mondayForecast.getSupplyHours(), "Supply should be 1.0");
        assertEquals(1.0, mondayForecast.getDemandHours(), "Demand should be 1.0");
        assertEquals(100.0, mondayForecast.getOccupancy(), "Occupancy should be 100%");

        assertEquals("CRITICAL", mondayForecast.getRecommendationLevel());
    }

    @Test
    void getWeeklyForecast_ShouldReturnLowEfficiency_WhenOccupancyIsLow() {

        WorkSchedule schedule = new WorkSchedule();
        schedule.setDayOfWeek(DayOfWeek.TUESDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(19, 0));
        when(workScheduleRepository.findAll()).thenReturn(List.of(schedule));

        ServiceEntity service = new ServiceEntity();
        service.setDuration(60);
        Timetable booking = new Timetable();
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setService(service);
        booking.setAppointmentTime(LocalDateTime.of(2023, 10, 3, 10, 0));

        when(timetableRepository.findAll()).thenReturn(List.of(booking));

        when(timetableRepository.findMinAppointmentTime()).thenReturn(Optional.of(LocalDateTime.of(2023, 10, 1, 0, 0)));
        when(timetableRepository.findMaxAppointmentTime()).thenReturn(Optional.of(LocalDateTime.of(2023, 10, 7, 0, 0)));

        List<ForecastDto> result = forecastService.getWeeklyForecast();

        ForecastDto tuesdayForecast = result.stream()
                .filter(dto -> dto.getDayOfWeek() == DayOfWeek.TUESDAY)
                .findFirst()
                .orElseThrow();

        assertEquals(10.0, tuesdayForecast.getOccupancy(), "Occupancy should be 10%");
        assertEquals("LOW", tuesdayForecast.getRecommendationLevel());
    }
}
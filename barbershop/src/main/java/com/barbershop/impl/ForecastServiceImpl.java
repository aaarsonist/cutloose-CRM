package com.barbershop.impl;

import com.barbershop.dto.forecast.ForecastDto;
import com.barbershop.model.BookingStatus;
import com.barbershop.model.Timetable;
import com.barbershop.model.WorkSchedule;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.WorkScheduleRepository;
import com.barbershop.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ForecastServiceImpl implements ForecastService {

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    private static final Map<DayOfWeek, String> DAY_NAMES_RU = Map.of(
            DayOfWeek.MONDAY, "Пн",
            DayOfWeek.TUESDAY, "Вт",
            DayOfWeek.WEDNESDAY, "Ср",
            DayOfWeek.THURSDAY, "Чт",
            DayOfWeek.FRIDAY, "Пт",
            DayOfWeek.SATURDAY, "Сб",
            DayOfWeek.SUNDAY, "Вс"
    );

    @Override
    public List<ForecastDto> getWeeklyForecast() {

        Map<DayOfWeek, Double> supplyMap = calculateWeeklySupply();

        Map<DayOfWeek, Double> demandMap = calculateWeeklyDemand();

        List<ForecastDto> forecastList = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {

            ForecastDto dto = new ForecastDto();
            dto.setDayOfWeek(day);
            dto.setDayOfWeekRussian(DAY_NAMES_RU.get(day));

            double supply = supplyMap.getOrDefault(day, 0.0);
            double demand = demandMap.getOrDefault(day, 0.0);

            dto.setSupplyHours(supply);
            dto.setDemandHours(demand);

            double occupancy = (supply == 0) ? 0 : (demand / supply) * 100.0;
            dto.setOccupancy(Math.round(occupancy * 10.0) / 10.0);

            setRecommendation(dto, occupancy);

            forecastList.add(dto);
        }

        return forecastList;
    }

    private Map<DayOfWeek, Double> calculateWeeklySupply() {
        List<WorkSchedule> allSchedules = workScheduleRepository.findAll();
        Map<DayOfWeek, Double> weeklySupplyHours = new EnumMap<>(DayOfWeek.class);

        for (WorkSchedule schedule : allSchedules) {
            if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
                double hours = ChronoUnit.MINUTES.between(schedule.getStartTime(), schedule.getEndTime()) / 60.0;
                weeklySupplyHours.merge(schedule.getDayOfWeek(), hours, Double::sum);
            }
        }
        return weeklySupplyHours;
    }

    private Map<DayOfWeek, Double> calculateWeeklyDemand() {
        List<Timetable> completedBookings = timetableRepository.findAll().stream()
                .filter(t -> t.getStatus() == BookingStatus.COMPLETED && t.getService() != null)
                .collect(Collectors.toList());

        Optional<LocalDateTime> minDateOpt = timetableRepository.findMinAppointmentTime();
        Optional<LocalDateTime> maxDateOpt = timetableRepository.findMaxAppointmentTime();

        if (minDateOpt.isEmpty() || maxDateOpt.isEmpty()) {
            return new EnumMap<>(DayOfWeek.class);
        }

        long totalDays = ChronoUnit.DAYS.between(minDateOpt.get().toLocalDate(), maxDateOpt.get().toLocalDate()) + 1;
        double totalWeeks = totalDays / 7.0;
        if (totalWeeks < 1.0) totalWeeks = 1.0;

        Map<DayOfWeek, Double> totalMinutesPerDay = completedBookings.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getAppointmentTime().getDayOfWeek(),
                        Collectors.summingDouble(t -> t.getService().getDuration())
                ));

        Map<DayOfWeek, Double> averageHoursPerDay = new EnumMap<>(DayOfWeek.class);
        for (Map.Entry<DayOfWeek, Double> entry : totalMinutesPerDay.entrySet()) {
            double totalMinutes = entry.getValue();
            double averageMinutes = totalMinutes / totalWeeks;
            averageHoursPerDay.put(entry.getKey(), Math.round((averageMinutes / 60.0) * 10.0) / 10.0);
        }

        return averageHoursPerDay;
    }

    private void setRecommendation(ForecastDto dto, double occupancy) {
        double roundedOccupancy = Math.round(occupancy * 10.0) / 10.0;

        if (roundedOccupancy > 95) {
            dto.setRecommendationLevel("CRITICAL");
            dto.setRecommendationText(String.format(
                    "КРИТИЧЕСКАЯ ЗАГРУЗКА! (%.1f%%). Вы гарантированно теряете клиентов. Настоятельно рекомендуется добавить 1 мастера.",
                    roundedOccupancy
            ));
        } else if (roundedOccupancy >= 86) {
            dto.setRecommendationLevel("HIGH");
            dto.setRecommendationText(String.format(
                    "ВЫСОКАЯ ЗАГРУЗКА (%.1f%%). Риск потери клиентов. Рассмотрите возможность добавления мастера.",
                    roundedOccupancy
            ));
        } else if (roundedOccupancy >= 60) {
            dto.setRecommendationLevel("OPTIMAL");
            dto.setRecommendationText(String.format(
                    "СБАЛАНСИРОВАНО (%.1f%%). Текущий график оптимален.",
                    roundedOccupancy
            ));
        } else if (roundedOccupancy > 0) {
            dto.setRecommendationLevel("LOW");
            dto.setRecommendationText(String.format(
                    "НИЗКАЯ ЭФФЕКТИВНОСТЬ (%.1f%%). Рекомендуется сократить смены или убрать 1 мастера.",
                    roundedOccupancy
            ));
        } else {
            dto.setRecommendationLevel("OPTIMAL");
            dto.setRecommendationText("Нет данных о спросе (0%).");
        }
    }
}
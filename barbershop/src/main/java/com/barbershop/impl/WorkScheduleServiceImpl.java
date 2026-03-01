package com.barbershop.impl;

import com.barbershop.model.BookingStatus;
import com.barbershop.model.Master;
import com.barbershop.model.Timetable;
import com.barbershop.model.WorkSchedule;
import com.barbershop.repository.MasterRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.WorkScheduleRepository;
import com.barbershop.service.WorkScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkScheduleServiceImpl implements WorkScheduleService {

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private MasterRepository masterRepository;
    @Autowired
    private TimetableRepository timetableRepository;
    private static final LocalTime MIN_OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime MAX_CLOSE_TIME = LocalTime.of(21, 0);
    private static final int MAX_WORK_HOURS = 8;
    private static final int MAX_WORK_DAYS_PER_WEEK = 5;

    @Override
    @Transactional(readOnly = true)
    public List<WorkSchedule> getAllSchedules() {
        return workScheduleRepository.findAllByOrderByMasterIdAsc();
    }

    @Override
    @Transactional
    public WorkSchedule updateSchedule(WorkSchedule scheduleData) {

        Long masterId = scheduleData.getMaster().getId();
        DayOfWeek day = scheduleData.getDayOfWeek();
        LocalTime newStart = scheduleData.getStartTime();
        LocalTime newEnd = scheduleData.getEndTime();

        if (newStart != null && newEnd != null) {

            if (newStart.isBefore(MIN_OPEN_TIME) || newEnd.isAfter(MAX_CLOSE_TIME)) {
                throw new IllegalArgumentException("Рабочее время должно быть в пределах 09:00 - 21:00");
            }
            if (!newStart.isBefore(newEnd)) {
                throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
            }

            long hours = Duration.between(newStart, newEnd).toHours();
            if (hours > MAX_WORK_HOURS || (hours == MAX_WORK_HOURS && Duration.between(newStart, newEnd).toMinutes() > MAX_WORK_HOURS * 60)) {
                throw new IllegalArgumentException("Смена не может длиться более 8 часов");
            }
        }

        Optional<WorkSchedule> existingScheduleOpt = workScheduleRepository
                .findByMasterIdAndDayOfWeek(masterId, day);

        WorkSchedule scheduleToSave;

        if (existingScheduleOpt.isPresent()) {
            scheduleToSave = existingScheduleOpt.get();
            scheduleToSave.setStartTime(newStart);
            scheduleToSave.setEndTime(newEnd);

            checkIfAppointmentsExistOnDay(masterId, day);
            if (newStart == null) {
                workScheduleRepository.delete(scheduleToSave);
                return null;
            }

        } else {

            if (newStart == null) {
                return null;
            }

            long currentWorkDays = workScheduleRepository.countByMasterId(masterId);
            if (currentWorkDays >= MAX_WORK_DAYS_PER_WEEK) {
                throw new IllegalArgumentException("Мастер не может работать более 5 дней в неделю");
            }

            scheduleToSave = new WorkSchedule();
            Master master = masterRepository.findById(masterId)
                    .orElseThrow(() -> new RuntimeException("Master not found"));

            scheduleToSave.setMaster(master);
            scheduleToSave.setDayOfWeek(day);
            scheduleToSave.setStartTime(newStart);
            scheduleToSave.setEndTime(newEnd);
        }

        return workScheduleRepository.save(scheduleToSave);
    }
    private void checkIfAppointmentsExistOnDay(Long masterId, DayOfWeek dayToRemove) {
        List<Timetable> futureAppointments = timetableRepository.findByMasterIdAndStatusAndAppointmentTimeAfter(
                masterId,
                BookingStatus.BOOKED,
                LocalDateTime.now()
        );

        boolean hasAppointmentOnThisDay = futureAppointments.stream()
                .anyMatch(appt -> appt.getAppointmentTime().getDayOfWeek() == dayToRemove);

        if (hasAppointmentOnThisDay) {
            throw new IllegalStateException("Нельзя убрать этот рабочий день, у мастера есть активные записи. Сначала отмените их.");
        }
    }
}
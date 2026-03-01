package com.barbershop.impl;

import com.barbershop.model.BookingStatus;
import com.barbershop.model.ServiceEntity;
import com.barbershop.model.Timetable;
import com.barbershop.model.WorkSchedule;
import com.barbershop.repository.ServiceRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.WorkScheduleRepository;
import com.barbershop.service.AvailabilityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final WorkScheduleRepository workScheduleRepository;
    private final TimetableRepository timetableRepository;
    private final ServiceRepository serviceRepository;

    private static final int SLOT_INTERVAL_MINUTES = 30;

    public AvailabilityServiceImpl(WorkScheduleRepository workScheduleRepository,
                                   TimetableRepository timetableRepository,
                                   ServiceRepository serviceRepository) {
        this.workScheduleRepository = workScheduleRepository;
        this.timetableRepository = timetableRepository;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public List<LocalTime> getAvailableSlots(Long masterId, Long serviceId, LocalDate date) {

        Optional<WorkSchedule> scheduleOpt = workScheduleRepository.findByMasterIdAndDayOfWeek(masterId, date.getDayOfWeek());
        if (scheduleOpt.isEmpty() || scheduleOpt.get().getStartTime() == null) {
            return new ArrayList<>();
        }
        WorkSchedule schedule = scheduleOpt.get();
        LocalTime workStart = schedule.getStartTime();
        LocalTime workEnd = schedule.getEndTime();

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        int duration = service.getDuration();

        List<Timetable> existingBookings = timetableRepository.findAllByMasterIdAndDate(masterId, date);

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentSlot = workStart;

        while (currentSlot.plusMinutes(duration).isBefore(workEnd) || currentSlot.plusMinutes(duration).equals(workEnd)) {

            boolean isOverlap = false;
            LocalTime slotStart = currentSlot;
            LocalTime slotEnd = currentSlot.plusMinutes(duration);

            for (Timetable booking : existingBookings) {
                if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.BOOKED) {

                    LocalTime bookingStart = booking.getAppointmentTime().toLocalTime();
                    int existingDuration = booking.getService().getDuration();
                    LocalTime bookingEnd = bookingStart.plusMinutes(existingDuration);

                    if (slotStart.isBefore(bookingEnd) && slotEnd.isAfter(bookingStart)) {
                        isOverlap = true;
                        break;
                    }
                }
            }

            if (!isOverlap) {
                availableSlots.add(slotStart);
            }

            currentSlot = currentSlot.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return availableSlots;
    }
}

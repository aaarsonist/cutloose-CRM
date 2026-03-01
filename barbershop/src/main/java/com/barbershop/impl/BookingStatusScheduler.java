package com.barbershop.impl;

import com.barbershop.model.BookingStatus;
import com.barbershop.model.Timetable;
import com.barbershop.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingStatusScheduler {

    @Autowired
    private TimetableRepository timetableRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void updateCompletedBookings() {
        System.out.println("SCHEDULER: Running job to update completed bookings...");

        List<Timetable> pastBooked = timetableRepository.findByStatusAndAppointmentTimeBefore(
                BookingStatus.BOOKED,
                LocalDateTime.now()
        );

        int updatedCount = 0;
        for (Timetable appointment : pastBooked) {
            Integer duration = appointment.getService().getDuration();

            if (duration == null) {
                continue;
            }

            LocalDateTime endTime = appointment.getAppointmentTime().plusMinutes(duration);

            if (endTime.isBefore(LocalDateTime.now())) {
                appointment.setStatus(BookingStatus.COMPLETED);
                timetableRepository.save(appointment);
                updatedCount++;
            }
        }
        System.out.println("SCHEDULER: Updated " + updatedCount + " appointments to COMPLETED.");
    }
}

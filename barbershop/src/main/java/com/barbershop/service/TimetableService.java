package com.barbershop.service;

import com.barbershop.dto.AdminBookingRequestDto;
import com.barbershop.model.Timetable;
import com.barbershop.dto.AppointmentDto;
import java.util.List;

public interface TimetableService {
    List<AppointmentDto> getAllAppointments();
    Timetable bookAppointment(Timetable timetable, Long userId);
    List<Timetable> getCompletedAppointmentsForUser(Long userId);

    List<Timetable> getUpcomingAppointmentsForUser(Long userId);

    void cancelAppointment(Long appointmentId, Long userId);
    void adminCancelAppointment(Long id);
    Timetable adminBookAppointment(AdminBookingRequestDto bookingRequest);
}

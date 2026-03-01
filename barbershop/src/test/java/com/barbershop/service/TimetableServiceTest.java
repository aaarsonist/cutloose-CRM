package com.barbershop.service;

import com.barbershop.impl.TimetableServiceImpl;
import com.barbershop.model.BookingStatus;
import com.barbershop.model.Timetable;
import com.barbershop.model.User;
import com.barbershop.repository.ReviewRepository;
import com.barbershop.repository.TimetableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private TimetableRepository timetableRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private TimetableServiceImpl timetableService;

    @Test
    void cancelAppointment_ShouldThrowException_WhenUserCancelsSomeoneElseBooking() {
        Long appointmentId = 100L;
        Long currentUserId = 1L;
        Long otherUserId = 2L;

        User otherUser = new User();
        otherUser.setId(otherUserId);

        Timetable appointment = new Timetable();
        appointment.setId(appointmentId);
        appointment.setBookedBy(otherUser);
        appointment.setStatus(BookingStatus.BOOKED);

        when(timetableRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            timetableService.cancelAppointment(appointmentId, currentUserId);
        });

        assertEquals("Вы не можете удалить чужую запись.", exception.getMessage());
        verify(timetableRepository, never()).delete(any());
    }

    @Test
    void cancelAppointment_ShouldThrowException_WhenCancelingCompletedBooking() {
        Long appointmentId = 100L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Timetable appointment = new Timetable();
        appointment.setId(appointmentId);
        appointment.setBookedBy(user);
        appointment.setStatus(BookingStatus.COMPLETED);

        when(timetableRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            timetableService.cancelAppointment(appointmentId, userId);
        });

        assertEquals("Нельзя отменить завершенную запись.", exception.getMessage());
    }

    @Test
    void cancelAppointment_ShouldDelete_WhenUserIsOwnerAndStatusBooked() {
        Long appointmentId = 100L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Timetable appointment = new Timetable();
        appointment.setId(appointmentId);
        appointment.setBookedBy(user);
        appointment.setStatus(BookingStatus.BOOKED);

        when(timetableRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        timetableService.cancelAppointment(appointmentId, userId);

        verify(timetableRepository).delete(appointment);
    }
}
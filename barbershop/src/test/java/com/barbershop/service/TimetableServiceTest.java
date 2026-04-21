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

import java.time.LocalDateTime;
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
        // 1. Создаем пользователя (у него обязательно должен быть ID)
        User user = new User();
        user.setId(1L); // Вот этот Long, который нужен Java
        user.setUsername("test_user");

        // 2. Создаем запись
        Timetable booking = new Timetable();
        booking.setId(10L);
        booking.setBookedBy(user);
        booking.setStatus(BookingStatus.BOOKED);
        booking.setAppointmentTime(LocalDateTime.now().plusDays(2));

        // 3. Настраиваем мок репозитория
        when(timetableRepository.findById(10L)).thenReturn(Optional.of(booking));

        // 4. ВЫЗОВ МЕТОДА (Исправленная строка 101)
        // Передаем 10L (ID записи) и user.getId() (это вернет 1L, тип Long)
        timetableService.cancelAppointment(10L, user.getId());

        // 5. Проверка
        verify(timetableRepository, times(1)).delete(booking);
    }
}
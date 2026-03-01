package com.barbershop.impl;

import com.barbershop.model.BookingStatus;
import org.springframework.security.access.AccessDeniedException;

import com.barbershop.dto.AppointmentDto;
import com.barbershop.dto.AdminBookingRequestDto;
import com.barbershop.model.*;
import com.barbershop.repository.*;
import com.barbershop.service.TimetableService;
import com.barbershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MasterRepository masterRepository;
    private final ServiceRepository serviceRepository;
    private final UserService userService;

    @Autowired
    public TimetableServiceImpl(
            TimetableRepository timetableRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            MasterRepository masterRepository,
            ServiceRepository serviceRepository,
            UserService userService) {
        this.timetableRepository = timetableRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.masterRepository = masterRepository;
        this.serviceRepository = serviceRepository;
        this.userService = userService;
    }
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments() {
        List<Timetable> appointments = timetableRepository.findAll();
        return appointments.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public Timetable bookAppointment(Timetable timetable, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        if (timetable.getService() != null) {
            ServiceEntity service = serviceRepository.findById(timetable.getService().getId())
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

            if (service.getName().startsWith("[НЕАКТИВНА]")) {
                throw new RuntimeException("Запись на данную услугу приостановлена.");
            }
        }
        timetable.setBookedBy(user);
        timetable.setStatus(BookingStatus.BOOKED);
        return timetableRepository.save(timetable);
    }

    @Override
    public List<Timetable> getCompletedAppointmentsForUser(Long userId) {
        return timetableRepository.findByBookedByIdAndStatus(userId, BookingStatus.COMPLETED);
    }

    @Override
    public List<Timetable> getUpcomingAppointmentsForUser(Long userId) {
        return timetableRepository.findByBookedByIdAndStatusAndAppointmentTimeAfter(userId, BookingStatus.BOOKED, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Timetable appointment = timetableRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Запись не найдена."));

        if (!appointment.getBookedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Вы не можете удалить чужую запись.");
        }

        if (appointment.getStatus() != BookingStatus.BOOKED) {
            throw new RuntimeException("Нельзя отменить завершенную запись.");
        }

        long minutesUntilStart = ChronoUnit.MINUTES.between(LocalDateTime.now(), appointment.getAppointmentTime());

        if (minutesUntilStart < 20) {
            throw new RuntimeException("Отмена невозможна: до начала записи осталось менее 20 минут.");
        }

        timetableRepository.delete(appointment);
    }

    private AppointmentDto mapToDto(Timetable t) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(t.getId());

        String serviceName = (t.getService() != null) ? t.getService().getName() : "Услуга";
        String masterName = (t.getMaster() != null) ? t.getMaster().getName() : "Мастер";
        Integer duration = (t.getService() != null) ? t.getService().getDuration() : 60;

        String clientName;
        String clientEmail;

        if (t.getBookedBy() != null) {
            clientName = t.getBookedBy().getName();
            clientEmail = t.getBookedBy().getUsername();
        } else {
            clientName = "Клиент";
            clientEmail = "N/A";
        }

        dto.setTitle(serviceName + " (" + masterName + ")");
        dto.setStart(t.getAppointmentTime());
        dto.setEnd(t.getAppointmentTime().plusMinutes(duration));

        dto.setServiceName(serviceName);
        dto.setMasterName(masterName);
        dto.setClientName(clientName);
        dto.setClientEmail(clientEmail);
        dto.setCreatedAt(t.getCreatedAt());

        return dto;
    }

    @Override
    @Transactional
    public void adminCancelAppointment(Long id) {
        Timetable appointment = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена с id: " + id));

        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Нельзя отменить прошедшую запись.");
        }
        reviewRepository.deleteByAppointmentId(id);
        timetableRepository.deleteById(id);
    }
    @Override
    @Transactional
    public Timetable adminBookAppointment(AdminBookingRequestDto request) {
        User client = userService.findOrCreateGuestUser(request.getClientEmail(), request.getClientName());

        Master master = masterRepository.findById(request.getMasterId())
                .orElseThrow(() -> new RuntimeException("Master not found"));
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Timetable newAppointment = new Timetable();
        newAppointment.setBookedBy(client);
        newAppointment.setMaster(master);
        newAppointment.setService(service);
        newAppointment.setAppointmentTime(request.getAppointmentTime());
        newAppointment.setStatus(BookingStatus.BOOKED);

        return timetableRepository.save(newAppointment);
    }
}

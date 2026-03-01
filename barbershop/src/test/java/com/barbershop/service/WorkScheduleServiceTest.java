package com.barbershop.service;

import com.barbershop.impl.WorkScheduleServiceImpl;
import com.barbershop.model.Master;
import com.barbershop.model.WorkSchedule;
import com.barbershop.repository.MasterRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.WorkScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;
    @Mock
    private MasterRepository masterRepository;
    @Mock
    private TimetableRepository timetableRepository;

    @InjectMocks
    private WorkScheduleServiceImpl workScheduleService;

    @Test
    void updateSchedule_ShouldThrowException_WhenShiftExceeds8Hours() {
        Master master = new Master();
        master.setId(1L);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setMaster(master);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            workScheduleService.updateSchedule(schedule);
        });

        assertEquals("Смена не может длиться более 8 часов", exception.getMessage());
    }

    @Test
    void updateSchedule_ShouldThrowException_WhenTimeIsOutsideWorkingHours() {
        Master master = new Master();
        master.setId(1L);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setMaster(master);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(12, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            workScheduleService.updateSchedule(schedule);
        });

        assertEquals("Рабочее время должно быть в пределах 09:00 - 21:00", exception.getMessage());
    }

    @Test
    void updateSchedule_ShouldThrowException_WhenStartIsAfterEnd() {
        Master master = new Master();
        master.setId(1L);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setMaster(master);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(14, 0));
        schedule.setEndTime(LocalTime.of(12, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            workScheduleService.updateSchedule(schedule);
        });
    }

    @Test
    void updateSchedule_ShouldSave_WhenDataIsValid() {
        Master master = new Master();
        master.setId(1L);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setMaster(master);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(16, 0));

        when(workScheduleRepository.findByMasterIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());
        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(workScheduleRepository.save(any(WorkSchedule.class))).thenReturn(schedule);

        WorkSchedule result = workScheduleService.updateSchedule(schedule);

        assertNotNull(result);
        verify(workScheduleRepository).save(any(WorkSchedule.class));
    }
}
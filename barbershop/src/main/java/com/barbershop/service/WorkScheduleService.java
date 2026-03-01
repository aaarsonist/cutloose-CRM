package com.barbershop.service;

import com.barbershop.model.WorkSchedule;
import java.util.List;

public interface WorkScheduleService {
    List<WorkSchedule> getAllSchedules();
    WorkSchedule updateSchedule(WorkSchedule scheduleDto);
}
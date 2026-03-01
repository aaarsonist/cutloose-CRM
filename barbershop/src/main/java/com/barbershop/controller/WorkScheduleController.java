package com.barbershop.controller;

import com.barbershop.model.WorkSchedule;
import com.barbershop.service.WorkScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-schedule")
@PreAuthorize("hasRole('ADMIN')")
public class WorkScheduleController {

    @Autowired
    private WorkScheduleService workScheduleService;

    @GetMapping("/all")
    public ResponseEntity<List<WorkSchedule>> getAllSchedules() {
        List<WorkSchedule> schedules = workScheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<WorkSchedule> updateScheduleEntry(@RequestBody WorkSchedule scheduleData) {

        if (scheduleData.getMaster() == null || scheduleData.getMaster().getId() == null || scheduleData.getDayOfWeek() == null) {
            return ResponseEntity.badRequest().build();
        }

        WorkSchedule updatedSchedule = workScheduleService.updateSchedule(scheduleData);
        return ResponseEntity.ok(updatedSchedule);
    }
}
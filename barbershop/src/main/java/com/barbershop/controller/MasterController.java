package com.barbershop.controller;

import com.barbershop.model.Master;
import com.barbershop.service.AvailabilityService;
import com.barbershop.service.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/masters")
public class MasterController {

    private final MasterService masterService;
    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    public MasterController(MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping
    public ResponseEntity<List<Master>> getAllMasters() {
        List<Master> masters = masterService.getAllMasters();
        return ResponseEntity.ok(masters);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateMaster(@PathVariable Long id) {
        masterService.deactivateMaster(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Master> createMaster(@RequestBody Master master) {
        if (master.getName() == null || master.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Master savedMaster = masterService.addMaster(master);
        return ResponseEntity.ok(savedMaster);
    }
    @GetMapping("/{masterId}/availability")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long masterId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LocalTime> slots = availabilityService.getAvailableSlots(masterId, serviceId, date);
        return ResponseEntity.ok(slots);
    }
}
package com.barbershop.controller;

import com.barbershop.model.ServiceEntity;
import com.barbershop.model.ServiceType;
import com.barbershop.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        logger.info("Получение всех услуг");
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @GetMapping("/men")
    public ResponseEntity<List<ServiceEntity>> getMenServices() {
        logger.info("Получение мужских услуг");
        return ResponseEntity.ok(serviceService.getServicesByType(ServiceType.MEN));
    }

    @GetMapping("/women")
    public ResponseEntity<List<ServiceEntity>> getWomenServices() {
        logger.info("Получение женских услуг");
        return ResponseEntity.ok(serviceService.getServicesByType(ServiceType.WOMEN));
    }

    @PostMapping
    public ResponseEntity<ServiceEntity> addService(@RequestBody ServiceEntity service) {
        logger.info("Добавление новой услуги: {}", service.getName());
        return ResponseEntity.ok(serviceService.saveService(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(@PathVariable Long id, @RequestBody ServiceEntity service) {
        logger.info("Обновление услуги с ID: {}", id);
        return ResponseEntity.ok(serviceService.updateService(id, service));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        logger.info("Удаление услуги с ID: {}", id);
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}

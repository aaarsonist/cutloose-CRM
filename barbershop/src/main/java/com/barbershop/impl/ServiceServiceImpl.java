package com.barbershop.impl;

import com.barbershop.model.ServiceEntity;
import com.barbershop.model.ServiceType;
import com.barbershop.repository.ServiceRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final TimetableRepository timetableRepository;

    @Autowired
    public ServiceServiceImpl(ServiceRepository serviceRepository, TimetableRepository timetableRepository) {
        this.serviceRepository = serviceRepository;
        this.timetableRepository = timetableRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAllActiveServices();
    }

    @Override
    public List<ServiceEntity> getServicesByType(ServiceType type) {
        return serviceRepository.findActiveByType(type);
    }

    @Override
    public ServiceEntity saveService(ServiceEntity service) {
        return serviceRepository.save(service);
    }

    @Override
    public ServiceEntity updateService(Long id, ServiceEntity service) {
        ServiceEntity existingService = serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Услуга не найдена"));
        existingService.setName(service.getName());
        existingService.setPrice(service.getPrice());
        existingService.setDuration(service.getDuration());
        existingService.setType(service.getType());
        existingService.setCategory(service.getCategory());
        return serviceRepository.save(existingService);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        boolean hasAnyBookings = timetableRepository.existsByServiceId(id);

        if (hasAnyBookings) {
            ServiceEntity service = serviceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

            if (!service.getName().startsWith("[НЕАКТИВНА]")) {
                service.setName("[НЕАКТИВНА] " + service.getName());
                serviceRepository.save(service);
            }

        } else {
            if (serviceRepository.existsById(id)) {
                serviceRepository.deleteById(id);
            } else {
                throw new RuntimeException("Услуга не найдена");
            }
        }
    }
}

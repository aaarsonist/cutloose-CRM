package com.barbershop.service;

import com.barbershop.model.ServiceEntity;
import com.barbershop.model.ServiceType;
import java.util.List;

public interface ServiceService {
    List<ServiceEntity> getAllServices();
    List<ServiceEntity> getServicesByType(ServiceType type);
    ServiceEntity saveService(ServiceEntity service);
    ServiceEntity updateService(Long id, ServiceEntity service);
    void deleteService(Long id);
}
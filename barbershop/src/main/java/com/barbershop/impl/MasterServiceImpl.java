package com.barbershop.impl;

import com.barbershop.model.BookingStatus;
import com.barbershop.model.Master;
import com.barbershop.repository.MasterRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.service.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;
    private final TimetableRepository timetableRepository;

    @Autowired
    public MasterServiceImpl(MasterRepository masterRepository, TimetableRepository timetableRepository) {
        this.masterRepository = masterRepository;
        this.timetableRepository = timetableRepository;
    }

    @Override
    public List<Master> getAllMasters() {
        return masterRepository.findAllByActiveTrue();
    }

    @Override
    @Transactional
    public void deactivateMaster(Long id) {
        Master master = masterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Master not found with id: " + id));

        boolean hasFutureAppointments = timetableRepository.existsByMasterIdAndStatusAndAppointmentTimeAfter(
                id,
                BookingStatus.BOOKED,
                LocalDateTime.now()
        );

        if (hasFutureAppointments) {
            throw new IllegalStateException("Нельзя деактивировать мастера, у которого есть будущие активные записи. Сначала отмените их.");
        }

        master.setActive(false);
        masterRepository.save(master);
    }
    @Override
    @Transactional
    public Master addMaster(Master master) {
        master.setActive(true);
        return masterRepository.save(master);
    }
}
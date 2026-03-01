package com.barbershop.repository;

import com.barbershop.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByMasterIdAndDayOfWeek(Long masterId, DayOfWeek dayOfWeek);
    List<WorkSchedule> findAllByOrderByMasterIdAsc();
    long countByMasterId(Long masterId);
}

package com.barbershop.repository;

import com.barbershop.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r.appointment.service.id, r.appointment.service.name, AVG(r.rating) FROM Review r " +
            "WHERE r.appointment IS NOT NULL " +
            "AND r.appointment.status = com.barbershop.model.BookingStatus.COMPLETED " +
            "AND (:startDate IS NULL OR r.appointment.appointmentTime >= :startDate) " +
            "AND (:endDate IS NULL OR r.appointment.appointmentTime <= :endDate) " +
            "AND (:serviceIds IS NULL OR r.appointment.service.id IN :serviceIds) " +
            "AND (:masterIds IS NULL OR r.appointment.master.id IN :masterIds)" +
            "GROUP BY r.appointment.service.id, r.appointment.service.name")
    List<Object[]> findAverageRatingByServiceWithinPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("serviceIds") List<Long> serviceIds,
            @Param("masterIds") List<Long> masterIds
    );

    @Query("SELECT r.appointment.master.id, m.name, AVG(r.rating) FROM Review r JOIN r.appointment a JOIN a.master m " +
            "WHERE a IS NOT NULL AND m IS NOT NULL " +
            "AND a.status = com.barbershop.model.BookingStatus.COMPLETED " +
            "AND (:startDate IS NULL OR a.appointmentTime >= :startDate) " +
            "AND (:endDate IS NULL OR a.appointmentTime <= :endDate) " +
            "AND (:masterIds IS NULL OR m.id IN :masterIds) " +
            "AND (:serviceIds IS NULL OR a.service.id IN :serviceIds)" +
            "GROUP BY r.appointment.master.id, m.name")
    List<Object[]> findAverageRatingByMasterWithinPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("masterIds") List<Long> masterIds,
            @Param("serviceIds") List<Long> serviceIds
    );

    @Override
    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.service",
            "appointment.master",
            "appointment.bookedBy"
    })
    List<Review> findAll();
    @Modifying
    @Query("DELETE FROM Review r WHERE r.appointment.id = :appointmentId")
    void deleteByAppointmentId(@Param("appointmentId") Long appointmentId);
}
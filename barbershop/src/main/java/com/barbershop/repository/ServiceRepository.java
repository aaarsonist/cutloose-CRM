package com.barbershop.repository;

import com.barbershop.model.ServiceEntity;
import com.barbershop.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    @Query("SELECT s FROM ServiceEntity s WHERE s.type = :type AND s.name NOT LIKE '[НЕАКТИВНА]%'")
    List<ServiceEntity> findActiveByType(@Param("type") ServiceType type);
    @Query("SELECT s FROM ServiceEntity s WHERE s.name NOT LIKE '[НЕАКТИВНА]%'")
    List<ServiceEntity> findAllActiveServices();
}

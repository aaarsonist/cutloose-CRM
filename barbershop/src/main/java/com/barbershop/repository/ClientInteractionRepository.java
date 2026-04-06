package com.barbershop.repository;

import com.barbershop.model.ClientInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClientInteractionRepository extends JpaRepository<ClientInteraction, Long> {
    List<ClientInteraction> findAllByOrderByInteractionDateDesc();
}
package com.barbershop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDto {
    private Long id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;

    private String clientName;
    private String clientEmail;
    private String serviceName;
    private String masterName;
    private LocalDateTime createdAt;
}
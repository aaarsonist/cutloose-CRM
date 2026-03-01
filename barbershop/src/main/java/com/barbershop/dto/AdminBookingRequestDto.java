package com.barbershop.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AdminBookingRequestDto {
    private String clientName;
    private String clientEmail;

    private Long masterId;
    private Long serviceId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime appointmentTime;
}
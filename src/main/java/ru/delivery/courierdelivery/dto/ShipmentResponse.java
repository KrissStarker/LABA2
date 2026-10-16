package ru.delivery.courierdelivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        UserResponse sender,
        String address,
        BigDecimal weight,
        String status,
        LocalDateTime createdAt
) {
}

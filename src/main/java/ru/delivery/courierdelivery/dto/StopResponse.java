package ru.delivery.courierdelivery.dto;

import java.time.LocalDateTime;

public record StopResponse(
        Long id,
        Long routeId,
        String address,
        Integer sequenceNumber,
        LocalDateTime createdAt
) {
}

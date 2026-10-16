package ru.delivery.courierdelivery.dto;

import java.time.LocalDateTime;

public record ShipmentAssignmentResponse(
        Long id,
        Long shipmentId,
        Long stopId,
        String status,
        LocalDateTime createdAt
) {
}

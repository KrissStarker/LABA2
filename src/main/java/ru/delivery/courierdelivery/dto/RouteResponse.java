package ru.delivery.courierdelivery.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RouteResponse(
        Long id,
        LocalDate routeDate,
        BigDecimal maxWeight,
        BigDecimal currentWeight,
        BigDecimal remainingWeight,
        String status,
        LocalDateTime createdAt
) {
}

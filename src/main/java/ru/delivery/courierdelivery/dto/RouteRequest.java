package ru.delivery.courierdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RouteRequest(
        @NotNull(message = "Дата маршрута обязательна") LocalDate routeDate,
        @NotNull(message = "Лимит массы обязателен") @Positive(message = "Лимит массы должен быть больше нуля") BigDecimal maxWeight,
        @NotBlank(message = "Статус обязателен") String status
) {
}

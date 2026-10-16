package ru.delivery.courierdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StopRequest(
        @NotNull(message = "Маршрут обязателен") Long routeId,
        @NotBlank(message = "Адрес обязателен") String address,
        @NotNull(message = "Порядковый номер обязателен") @Positive(message = "Порядковый номер должен быть больше нуля") Integer sequenceNumber
) {
}

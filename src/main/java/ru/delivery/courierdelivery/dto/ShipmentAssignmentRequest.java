package ru.delivery.courierdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShipmentAssignmentRequest(
        @NotNull(message = "Отправление обязательно") Long shipmentId,
        @NotNull(message = "Остановка обязательна") Long stopId,
        @NotBlank(message = "Статус обязателен") String status
) {
}

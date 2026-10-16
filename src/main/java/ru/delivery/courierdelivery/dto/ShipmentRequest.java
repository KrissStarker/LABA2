package ru.delivery.courierdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ShipmentRequest(
        @NotNull(message = "Отправитель обязателен") Long senderId,
        @NotBlank(message = "Адрес обязателен") String address,
        @NotNull(message = "Масса обязательна") @PositiveOrZero(message = "Масса не может быть отрицательной") BigDecimal weight,
        @NotBlank(message = "Статус обязателен") String status
) {
}

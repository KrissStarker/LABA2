package ru.delivery.courierdelivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "Имя обязательно") String name,
        @Email(message = "Некорректный email") @NotBlank(message = "Email обязателен") String email,
        @NotBlank(message = "Роль обязательна") String role
) {
}

package com.natwest.orders.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "product is required")
        String product,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "price is required")
        @Positive(message = "price must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price
) {
}

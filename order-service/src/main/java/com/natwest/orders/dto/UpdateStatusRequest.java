package com.natwest.orders.dto;

import com.natwest.orders.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "status is required")
        OrderStatus status
) {
}

package com.natwest.orders.dto;

import com.natwest.orders.model.Order;
import com.natwest.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String customerId,
        String product,
        Integer quantity,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getProduct(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}

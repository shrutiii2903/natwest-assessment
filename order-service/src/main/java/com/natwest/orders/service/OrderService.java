package com.natwest.orders.service;

import com.natwest.orders.dto.CreateOrderRequest;
import com.natwest.orders.dto.OrderResponse;
import com.natwest.orders.exception.OrderNotFoundException;
import com.natwest.orders.model.Order;
import com.natwest.orders.model.OrderStatus;
import com.natwest.orders.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(
                request.customerId(),
                request.product(),
                request.quantity(),
                request.price());
        return OrderResponse.from(repository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return repository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.changeStatus(status);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return repository.findAll(Sort.by("id")).stream()
                .map(OrderResponse::from)
                .toList();
    }
}

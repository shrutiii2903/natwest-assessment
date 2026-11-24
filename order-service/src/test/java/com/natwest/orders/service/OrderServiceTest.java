package com.natwest.orders.service;

import com.natwest.orders.dto.CreateOrderRequest;
import com.natwest.orders.dto.OrderResponse;
import com.natwest.orders.exception.OrderNotFoundException;
import com.natwest.orders.model.Order;
import com.natwest.orders.model.OrderStatus;
import com.natwest.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @InjectMocks
    private OrderService service;

    private Order existingOrder;

    @BeforeEach
    void setUp() {
        existingOrder = new Order("12345", "Laptop", 1, new BigDecimal("1200"));
    }

    @Test
    void createOrderPersistsAndReturnsNewOrderInCreatedStatus() {
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.createOrder(
                new CreateOrderRequest("12345", "Laptop", 1, new BigDecimal("1200")));

        assertThat(response.customerId()).isEqualTo("12345");
        assertThat(response.product()).isEqualTo("Laptop");
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.price()).isEqualByComparingTo("1200");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        verify(repository).save(any(Order.class));
    }

    @Test
    void getOrderReturnsOrderWhenItExists() {
        when(repository.findById(7L)).thenReturn(Optional.of(existingOrder));

        OrderResponse response = service.getOrder(7L);

        assertThat(response.product()).isEqualTo("Laptop");
    }

    @Test
    void getOrderThrowsNotFoundWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found with id: 99");
    }

    @Test
    void updateStatusChangesStatusOfExistingOrder() {
        when(repository.findById(7L)).thenReturn(Optional.of(existingOrder));

        OrderResponse response = service.updateStatus(7L, OrderStatus.SHIPPED);

        assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void updateStatusThrowsNotFoundWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(99L, OrderStatus.SHIPPED))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void listOrdersMapsAllOrders() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(existingOrder));

        List<OrderResponse> orders = service.listOrders();

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).product()).isEqualTo("Laptop");
    }
}

package com.natwest.orders.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequestHasNoViolations() {
        CreateOrderRequest request = new CreateOrderRequest("12345", "Laptop", 1, new BigDecimal("1200"));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void missingCustomerIdIsRejectedWithMeaningfulMessage() {
        CreateOrderRequest request = new CreateOrderRequest(null, "Laptop", 1, new BigDecimal("1200"));

        assertThat(messagesFor(request)).containsExactly("customerId is required");
    }

    @Test
    void blankCustomerIdIsRejected() {
        CreateOrderRequest request = new CreateOrderRequest("   ", "Laptop", 1, new BigDecimal("1200"));

        assertThat(messagesFor(request)).contains("customerId is required");
    }

    @Test
    void zeroQuantityIsRejected() {
        CreateOrderRequest request = new CreateOrderRequest("12345", "Laptop", 0, new BigDecimal("1200"));

        assertThat(messagesFor(request)).containsExactly("quantity must be greater than 0");
    }

    @Test
    void zeroPriceIsRejected() {
        CreateOrderRequest request = new CreateOrderRequest("12345", "Laptop", 1, BigDecimal.ZERO);

        assertThat(messagesFor(request)).containsExactly("price must be greater than 0");
    }

    @Test
    void negativePriceIsRejected() {
        CreateOrderRequest request = new CreateOrderRequest("12345", "Laptop", 1, new BigDecimal("-5"));

        assertThat(messagesFor(request)).contains("price must be greater than 0");
    }

    @Test
    void missingProductIsRejected() {
        CreateOrderRequest request = new CreateOrderRequest("12345", null, 1, new BigDecimal("1200"));

        assertThat(messagesFor(request)).containsExactly("product is required");
    }

    private Set<String> messagesFor(CreateOrderRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}

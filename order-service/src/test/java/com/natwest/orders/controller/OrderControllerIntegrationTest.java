package com.natwest.orders.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    private static final String VALID_ORDER = """
            {
              "customerId": "12345",
              "product": "Laptop",
              "quantity": 1,
              "price": 1200
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrderReturns201WithLocationAndCreatedStatus() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/orders/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value("12345"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void retrieveOrderReturnsCreatedOrder() throws Exception {
        String location = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void updateStatusChangesOrderStatus() throws Exception {
        String location = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(put(location + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(get(location))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void listOrdersReturnsArray() throws Exception {
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ORDER));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void createOrderWithMissingCustomerIdReturns400WithMessage() throws Exception {
        String body = """
                {"product": "Laptop", "quantity": 1, "price": 1200}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.customerId").value("customerId is required"));
    }

    @Test
    void createOrderWithZeroQuantityAndPriceReturns400WithBothErrors() throws Exception {
        String body = """
                {"customerId": "12345", "product": "Laptop", "quantity": 0, "price": 0}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").value("quantity must be greater than 0"))
                .andExpect(jsonPath("$.errors.price").value("price must be greater than 0"));
    }

    @Test
    void retrieveUnknownOrderReturns404() throws Exception {
        mockMvc.perform(get("/orders/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999999"));
    }

    @Test
    void invalidStatusValueReturns400() throws Exception {
        String location = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(put(location + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOST_IN_SPACE\"}"))
                .andExpect(status().isBadRequest());
    }
}

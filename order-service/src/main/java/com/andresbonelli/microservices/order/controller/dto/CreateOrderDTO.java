package com.andresbonelli.microservices.order.controller.dto;

import org.apache.catalina.User;

import java.math.BigDecimal;

public record CreateOrderDTO(
        String skuCode,
        BigDecimal price,
        Integer quantity,
        UserDetails userDetails
) {
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {
    }
}

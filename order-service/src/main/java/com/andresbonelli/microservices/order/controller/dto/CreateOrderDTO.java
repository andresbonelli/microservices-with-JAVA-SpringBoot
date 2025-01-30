package com.andresbonelli.microservices.order.controller.dto;

import java.math.BigDecimal;

public record CreateOrderDTO(
        String skuCode,
        BigDecimal price,
        Integer quantity
) {
}

package com.andresbonelli.microservices.order.controller.dto;

import java.math.BigDecimal;

public record UpdateOrderDTO(
        Long id,
        String skuCode,
        BigDecimal price,
        Integer quantity
) {
}

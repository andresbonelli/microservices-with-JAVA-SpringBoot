package com.andresbonelli.microservices.order.controller.dto;

import java.math.BigDecimal;

public record ResponseOrderDTO(
        Long id,
        String orderCode,
        String skuCode,
        BigDecimal price,
        Integer quantity
) {
}

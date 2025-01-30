package com.andresbonelli.microservices.product.controller.dto;

import java.math.BigDecimal;

public record CreateProductDTO(
        String name,
        String description,
        BigDecimal price
) {
}

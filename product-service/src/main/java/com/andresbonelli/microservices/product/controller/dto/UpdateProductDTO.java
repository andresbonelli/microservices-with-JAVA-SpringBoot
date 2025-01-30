package com.andresbonelli.microservices.product.controller.dto;

import java.math.BigDecimal;

public record UpdateProductDTO(
        String id,
        String name,
        String description,
        BigDecimal price
) {
}


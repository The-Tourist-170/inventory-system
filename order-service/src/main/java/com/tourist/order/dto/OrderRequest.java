package com.tourist.order.dto;

import java.math.BigDecimal;

public record OrderRequest(
    Long id,
    String order_no,
    String skuCode,
    BigDecimal price,
    Integer quantity
) {}

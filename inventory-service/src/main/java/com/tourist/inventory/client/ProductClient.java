package com.tourist.inventory.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/product")
public interface ProductClient {
    @GetExchange("/{skuCode}/exists")
    boolean existsBySkuCode(@PathVariable String skuCode);
}

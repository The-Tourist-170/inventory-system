package com.tourist.order.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/inventory")
public interface InventoryClient {
    @GetExchange
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);

    @PostExchange("/{skuCode}/deduct")
    void deductStock(@PathVariable String skuCode, @RequestParam Integer quantity);
}

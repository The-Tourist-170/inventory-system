package com.tourist.order.Service;

import com.tourist.order.client.InventoryClient;
import com.tourist.order.dto.OrderRequest;
import com.tourist.order.model.Order;
import com.tourist.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @CircuitBreaker(name = "inventory", fallbackMethod = "inventoryFallback")
    public void placeOrder(OrderRequest orderRequest) {
        inventoryClient.deductStock(orderRequest.skuCode(), orderRequest.quantity());

        Order order = new Order();
        order.setOrder_no(UUID.randomUUID().toString());
        order.setPrice(orderRequest.price());
        order.setQuantity(orderRequest.quantity());
        order.setSkuCode(orderRequest.skuCode());

        orderRepository.save(order);
    }

    private void inventoryFallback(OrderRequest orderRequest, Throwable t) {
        throw new RuntimeException("Inventory service is temporarily unavailable");
    }
}

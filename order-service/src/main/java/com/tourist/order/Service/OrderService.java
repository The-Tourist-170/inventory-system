package com.tourist.order.Service;

import com.tourist.order.client.InventoryClient;
import com.tourist.order.dto.OrderRequest;
import com.tourist.order.model.Order;
import com.tourist.order.repository.OrderRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest) {
        try {
            inventoryClient.deductStock(orderRequest.skuCode(), orderRequest.quantity());
        } catch (Exception e) {
            throw new RuntimeException(
                "Cannot place order — " + e.getMessage(), e);
        }

        Order order = new Order();
        order.setOrder_no(UUID.randomUUID().toString());
        order.setPrice(orderRequest.price());
        order.setQuantity(orderRequest.quantity());
        order.setSkuCode(orderRequest.skuCode());

        orderRepository.save(order);
    }
}

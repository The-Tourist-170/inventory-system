package com.tourist.order.Service;

import com.tourist.order.client.InventoryClient;
import com.tourist.order.dto.OrderRequest;
import com.tourist.order.model.Order;
import com.tourist.order.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest) {
        boolean isInStock = inventoryClient.isInStock(
            orderRequest.skuCode(),
            orderRequest.quantity()
        );
        if (!isInStock) {
            throw new RuntimeException(
                orderRequest.skuCode() + " is not in stock"
            );
        } else {
            Order order = new Order();
            order.setOrder_no(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setQuantity(orderRequest.quantity());
            order.setSkuCode(orderRequest.skuCode());

            orderRepository.save(order);
        }
    }
}

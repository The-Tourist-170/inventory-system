package com.tourist.inventory.service;

import com.tourist.inventory.client.ProductClient;
import com.tourist.inventory.model.Inventory;
import com.tourist.inventory.repository.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductClient productClient;

    public boolean isInStock(String skuCode, Integer quantity) {
        if (!productClient.existsBySkuCode(skuCode)) {
            throw new RuntimeException("No such product exists: " + skuCode);
        }
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(
            skuCode,
            quantity
        );
    }

    public Inventory restockInventory(String skuCode, Integer amount) {
        if (!productClient.existsBySkuCode(skuCode)) {
            throw new RuntimeException("No such product exists: " + skuCode);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be positive");
        }
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setSkuCode(skuCode);
                    newInv.setQuantity(0);
                    return newInv;
                });
        inventory.setQuantity(inventory.getQuantity() + amount);
        return inventoryRepository.save(inventory);
    }

    public Inventory deductStock(String skuCode, Integer quantity) {
        if (!productClient.existsBySkuCode(skuCode)) {
            throw new RuntimeException("No such product exists: " + skuCode);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Deduction quantity must be positive");
        }
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException(
                        "Insufficient stock: no inventory record for " + skuCode));
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for SKU: " + skuCode);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
}

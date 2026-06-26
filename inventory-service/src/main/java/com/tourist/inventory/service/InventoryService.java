package com.tourist.inventory.service;

import com.tourist.inventory.model.Inventory;
import com.tourist.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(
            skuCode,
            quantity
        );
    }

    public Inventory createInventory(String skuCode, Integer quantity) {
        if (inventoryRepository.existsBySkuCode(skuCode)) {
            throw new IllegalArgumentException("SKU already exists: " + skuCode);
        }
        Inventory inventory = new Inventory();
        inventory.setSkuCode(skuCode);
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory restockInventory(String skuCode, Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be positive");
        }
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
            .orElseThrow(() -> new IllegalArgumentException("SKU not found: " + skuCode));
        inventory.setQuantity(inventory.getQuantity() + amount);
        return inventoryRepository.save(inventory);
    }
}

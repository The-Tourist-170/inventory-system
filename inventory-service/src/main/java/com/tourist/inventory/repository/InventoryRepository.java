package com.tourist.inventory.repository;

import com.tourist.inventory.model.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCodeAndQuantityIsGreaterThanEqual(
        String skuCode,
        Integer quantity
    );

    boolean existsBySkuCode(String skuCode);

    Optional<Inventory> findBySkuCode(String skuCode);
}

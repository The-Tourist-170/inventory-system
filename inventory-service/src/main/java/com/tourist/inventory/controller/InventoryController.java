package com.tourist.inventory.controller;

import com.tourist.inventory.dto.RestockRequest;
import com.tourist.inventory.model.Inventory;
import com.tourist.inventory.service.InventoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isInStock(
        @RequestParam String skuCode,
        @RequestParam Integer quantity
    ) {
        boolean result = inventoryService.isInStock(skuCode, quantity);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{skuCode}/restock")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Inventory> restockInventory(
        @PathVariable String skuCode,
        @RequestBody RestockRequest request
    ) {
        Inventory result = inventoryService.restockInventory(
            skuCode,
            request.quantity()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{skuCode}/deduct")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Inventory> deductStock(
        @PathVariable String skuCode,
        @RequestParam Integer quantity
    ) {
        Inventory result = inventoryService.deductStock(skuCode, quantity);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
}

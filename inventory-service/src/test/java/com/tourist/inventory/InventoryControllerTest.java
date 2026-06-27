package com.tourist.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourist.inventory.client.ProductClient;
import com.tourist.inventory.dto.RestockRequest;
import com.tourist.inventory.model.Inventory;
import com.tourist.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @MockitoBean
    private ProductClient productClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        when(productClient.existsBySkuCode("valid_sku")).thenReturn(true);
        when(productClient.existsBySkuCode("restock_sku")).thenReturn(true);
        when(productClient.existsBySkuCode("neg_restock_sku")).thenReturn(true);
        when(productClient.existsBySkuCode("new_sku")).thenReturn(true);
        when(productClient.existsBySkuCode("deduct_sku")).thenReturn(true);
        when(productClient.existsBySkuCode("nonexistent")).thenReturn(false);
    }

    private Inventory seedInventory(String skuCode, int quantity) {
        Inventory inv = new Inventory();
        inv.setSkuCode(skuCode);
        inv.setQuantity(quantity);
        return inventoryRepository.save(inv);
    }

    @Test
    void shouldCheckStock() throws Exception {
        seedInventory("valid_sku", 30);

        mockMvc.perform(get("/api/inventory")
                .param("skuCode", "valid_sku")
                .param("quantity", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));

        mockMvc.perform(get("/api/inventory")
                .param("skuCode", "valid_sku")
                .param("quantity", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(false));
    }

    @Test
    void shouldRejectStockCheckForUnknownProduct() throws Exception {
        seedInventory("nonexistent", 10);

        mockMvc.perform(get("/api/inventory")
                .param("skuCode", "nonexistent")
                .param("quantity", "5"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No such product exists: nonexistent"));
    }

    @Test
    void shouldRestockInventory() throws Exception {
        seedInventory("restock_sku", 100);

        RestockRequest restock = new RestockRequest(50);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", "restock_sku")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(150));
    }

    @Test
    void shouldRejectRestockForUnknownProduct() throws Exception {
        RestockRequest restock = new RestockRequest(10);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No such product exists: nonexistent"));
    }

    @Test
    void shouldRejectNegativeRestockAmount() throws Exception {
        seedInventory("neg_restock_sku", 10);

        RestockRequest restock = new RestockRequest(-5);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", "neg_restock_sku")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAutoCreateInventoryOnFirstRestock() throws Exception {
        RestockRequest restock = new RestockRequest(200);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", "new_sku")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.skuCode").value("new_sku"))
            .andExpect(jsonPath("$.quantity").value(200));
    }

    @Test
    void shouldDeductStock() throws Exception {
        seedInventory("deduct_sku", 100);

        mockMvc.perform(post("/api/inventory/{skuCode}/deduct", "deduct_sku")
                .param("quantity", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.skuCode").value("deduct_sku"))
            .andExpect(jsonPath("$.quantity").value(70));
    }

    @Test
    void shouldRejectDeductWhenInsufficientStock() throws Exception {
        seedInventory("deduct_sku", 10);

        mockMvc.perform(post("/api/inventory/{skuCode}/deduct", "deduct_sku")
                .param("quantity", "50"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Insufficient stock for SKU: deduct_sku"));
    }

    @Test
    void shouldRejectDeductForUnknownProduct() throws Exception {
        mockMvc.perform(post("/api/inventory/{skuCode}/deduct", "nonexistent")
                .param("quantity", "5"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No such product exists: nonexistent"));
    }

    @Test
    void shouldListAllInventory() throws Exception {
        seedInventory("sku_a", 100);
        seedInventory("sku_b", 50);
        seedInventory("sku_c", 0);

        mockMvc.perform(get("/api/inventory/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].skuCode").value("sku_a"))
            .andExpect(jsonPath("$[0].quantity").value(100))
            .andExpect(jsonPath("$[1].skuCode").value("sku_b"))
            .andExpect(jsonPath("$[2].skuCode").value("sku_c"));
    }
}

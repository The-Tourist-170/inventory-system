package com.tourist.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourist.inventory.dto.InventoryRequest;
import com.tourist.inventory.dto.RestockRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateInventory() throws Exception {
        InventoryRequest request = new InventoryRequest("test_sku", 50);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.skuCode").value("test_sku"))
            .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void shouldRejectDuplicateSku() throws Exception {
        InventoryRequest request = new InventoryRequest("dupe_sku", 10);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldCheckStock() throws Exception {
        String sku = "stock_check_sku";
        InventoryRequest request = new InventoryRequest(sku, 30);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/inventory")
                .param("skuCode", sku)
                .param("quantity", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));

        mockMvc.perform(get("/api/inventory")
                .param("skuCode", sku)
                .param("quantity", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(false));
    }

    @Test
    void shouldRestockInventory() throws Exception {
        String sku = "restock_sku";
        InventoryRequest create = new InventoryRequest(sku, 100);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andExpect(status().isCreated());

        RestockRequest restock = new RestockRequest(50);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", sku)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(150));
    }

    @Test
    void shouldRejectRestockForUnknownSku() throws Exception {
        RestockRequest restock = new RestockRequest(10);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectNegativeRestockAmount() throws Exception {
        String sku = "neg_restock_sku";
        InventoryRequest create = new InventoryRequest(sku, 10);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andExpect(status().isCreated());

        RestockRequest restock = new RestockRequest(-5);

        mockMvc.perform(put("/api/inventory/{skuCode}/restock", sku)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restock)))
            .andExpect(status().isBadRequest());
    }
}

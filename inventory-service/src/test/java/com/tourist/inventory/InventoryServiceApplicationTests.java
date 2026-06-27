package com.tourist.inventory;

import com.tourist.inventory.client.ProductClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class InventoryServiceApplicationTests {

    @MockitoBean
    private ProductClient productClient;

    @Test
    void contextLoads() {
    }

}

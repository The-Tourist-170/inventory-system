package com.tourist.microservices.product;

import com.tourist.microservices.product.model.Product;
import com.tourist.microservices.product.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import java.math.BigDecimal;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProductServiceApplicationTests {

	@Autowired
	private ProductRepository productRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateProduct() {
		Product product = Product.builder()
				.name("iPhone 15")
				.description("iPhone 15")
				.price(BigDecimal.valueOf(1000))
				.build();
		productRepository.save(product);
		Assertions.assertNotNull(product.getId());
	}

}

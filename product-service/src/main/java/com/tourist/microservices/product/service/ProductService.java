package com.tourist.microservices.product.service;

import com.tourist.microservices.product.dto.ProductRequest;
import com.tourist.microservices.product.dto.ProductResponse;
import com.tourist.microservices.product.model.Product;
import com.tourist.microservices.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
            .name(productRequest.name())
            .description(productRequest.description())
            .price(productRequest.price())
            .build();
        productRepository.save(product);
        log.info("Product created: {}", product);
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice()
        );
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository
            .findAll()
            .stream()
            .map(product ->
                new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice()
                )
            )
            .toList();
    }
}

package com.tourist.order.config;

import com.tourist.order.client.InventoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class InventoryClientConfig {

    @Value("${inventory.service.url:http://localhost:8082}")
    private String inventoryServiceUrl;

    @Bean
    public InventoryClient inventoryClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException(
                            "Inventory service error: " + res.getStatusCode() + " " +
                            new String(res.getBody().readAllBytes()));
                })
                .build();
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(InventoryClient.class);
    }
}

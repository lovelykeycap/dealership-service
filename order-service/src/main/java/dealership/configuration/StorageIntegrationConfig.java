package dealership.configuration;

import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.infrastructure.integration.storage.StorageCatalogHttpGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StorageIntegrationConfig {

    @Bean
    public RestClient storageRestClient(
            @Value("${storage-service.base-url:http://localhost:8082}") String storageServiceBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(storageServiceBaseUrl)
                .build();
    }

    @Bean
    public StorageCatalogGateway storageCatalogGateway(RestClient storageRestClient) {
        return new StorageCatalogHttpGateway(storageRestClient);
    }
}

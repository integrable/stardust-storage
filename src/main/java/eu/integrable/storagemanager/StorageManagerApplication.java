package eu.integrable.storagemanager;

import eu.integrable.storagemanager.property.CredentialsProperty;
import eu.integrable.storagemanager.property.StorageProperty;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(value = {
        CredentialsProperty.class,
        StorageProperty.class
})
public class StorageManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageManagerApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                .addSecuritySchemes("bearer", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                .addSecuritySchemes("basic", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}

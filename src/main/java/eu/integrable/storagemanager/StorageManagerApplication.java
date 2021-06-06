package eu.integrable.storagemanager;

import eu.integrable.storagemanager.property.CredentialsProperty;
import eu.integrable.storagemanager.property.StorageProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {
        CredentialsProperty.class,
        StorageProperty.class
})
public class StorageManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageManagerApplication.class, args);
    }

}

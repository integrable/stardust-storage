package eu.integrable.starduststorage.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Storage manager API",
                version = "1.0",
                description = "",
                contact = @Contact(
                        name = "Integrable",
                        url = "https://integrable.eu",
                        email = "kamil.szewc@integrable.eu"
                ),
                license = @License(
                        name = "Integrable License",
                        url = "https://integrable.eu/licenses"
                )
        ),
        servers = @Server(url = "http://localhost:8082")
)
public class OpenApiConfig {
}

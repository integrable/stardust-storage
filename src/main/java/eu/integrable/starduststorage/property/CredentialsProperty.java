package eu.integrable.starduststorage.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "credentials")
public class CredentialsProperty {

    private String jwtSecret;
}

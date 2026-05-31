package com.elcom.adminconsolebackend.config.keycloak;

import lombok.Data;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakProviderConfig {
    private String realm;
    private String clientId;
    private String username;
    private String password;
    private String serverUrl;
    private String clientSecret;

    private static Keycloak keycloakAdmin = null;

    @Bean
    public synchronized Keycloak getKeycloakClientWithAdmin() {
        if (keycloakAdmin == null) {
            keycloakAdmin = KeycloakBuilder.builder()
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
//                    .username(username)
//                    .password(password)
                    .serverUrl(serverUrl)
                    .build();
        }

        return keycloakAdmin;
    }

    @Bean
    public RealmResource getRealmResource() {
        return getKeycloakClientWithAdmin().realm(realm);
    }
}

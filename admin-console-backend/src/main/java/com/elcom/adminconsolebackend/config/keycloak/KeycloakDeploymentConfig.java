package com.elcom.adminconsolebackend.config.keycloak;

import lombok.Data;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak.deployment")
public class KeycloakDeploymentConfig {
    private String realm;
    private String authServerUrl;
    private String resource;

    @Bean
    public KeycloakDeployment createKeycloakDeployment(){
        AdapterConfig adapterConfig = new AdapterConfig();
        adapterConfig.setAuthServerUrl(authServerUrl);
        adapterConfig.setRealm(realm);
        adapterConfig.setResource(resource);

        return KeycloakDeploymentBuilder.build(adapterConfig);
    }
}

package com.elcom.adminconsolebackend.config.authz;

import com.authz.sdk.client.AuthzClient;
import com.authz.sdk.exceptions.AuthzException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "authz.server")
@Slf4j
public class AuthzConfig {
    private String url;
    private String host;
    private Integer port;
    private String clientId;
    private String clientSecret;
    private String username;
    private String password;

    private AuthzClient client;
    private String token;

    private String clearCacheUrl;

    @Bean
    public AuthzClient initAuthzClient() throws AuthzException {
        client = new AuthzClient(host, port);
        client.initialize();
        getTokenClearCache();
        return client;
    }

    @Scheduled(fixedRate = 5 * 60 + 55, timeUnit = TimeUnit.MINUTES)
    public void refreshToken() throws AuthzException {
        client.authenticate(clientId, clientSecret);
        getTokenClearCache();
    }

    private void getTokenClearCache(){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        ResponseEntity<TokenDto> response = restTemplate.postForEntity(url, body, TokenDto.class);
        token = response.getBody().getToken();
    }
}

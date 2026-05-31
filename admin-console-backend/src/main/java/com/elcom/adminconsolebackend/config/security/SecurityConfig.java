package com.elcom.adminconsolebackend.config.security;

import com.authz.sdk.client.AuthzClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationEntryPoint authEntryPoint;

    @Bean
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(httpSecurityCorsConfigurer -> {
            httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
        });
        http.authorizeHttpRequests(request ->  request.requestMatchers(HttpMethod.GET, "/actuator/prometheus", "/actuator/prometheus/**", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/monitors/**").permitAll()
                .anyRequest()
                .authenticated());
        http.oauth2ResourceServer(oauth2 -> {
            oauth2.jwt(Customizer.withDefaults());
        });

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(authEntryPoint));

//        http.addFilterAfter(new AuthorizationFilter(authzClient), AuthorizationFilter.class);

        //TODO: convert các field của jwt sang 1 DTO để quản lý sử dụng hàm jwtAuthenticationConverter
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return configurationSource;
    }
}

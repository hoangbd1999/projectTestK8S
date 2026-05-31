package com.elcom.adminconsolebackend.config.httpclient;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Value("${service.communication.key}")
    private String internalCommunicationKey;

    @Bean
    public PageJacksonModule pageJacksonModule() {
        return new PageJacksonModule();
    }

    @Bean
    public SortJacksonModule sortJacksonModule() {
        return new SortJacksonModule();
    }

    @Bean
    public RequestInterceptor communicationKeyPopulatingRequestInterceptor() {
        return new CommunicationKeyPopulatingRequestInterceptor(internalCommunicationKey);
    }
}

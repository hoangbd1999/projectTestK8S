package com.elcom.adminconsolebackend.config.httpclient;

import com.elcom.adminconsolebackend.util.SecurityUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

public class CommunicationKeyPopulatingRequestInterceptor implements RequestInterceptor {

    private static final String INTERNAL_COMMUNICATION_KEY_HEADERS = "Internal-Communication-Key";

    private final String internalCommunicationKey;

    public CommunicationKeyPopulatingRequestInterceptor(String internalCommunicationKey) {
        Assert.notNull(internalCommunicationKey, "Internal communication key mustn't be null");
        Assert.isTrue(!internalCommunicationKey.isBlank(), "Internal communication key mustn't be blank");
        this.internalCommunicationKey = internalCommunicationKey.trim();
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(INTERNAL_COMMUNICATION_KEY_HEADERS, this.internalCommunicationKey);
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityUtils.getAuthToken() != null) {
            template.header("Authorization", SecurityUtils.getAuthToken());
        }
    }

}

package com.elcom.adminconsolebackend.config.security;

import com.authz.sdk.client.AuthzClient;
import com.authz.sdk.grpc.Authz;
import com.elcom.adminconsolebackend.dto.authorization.ApiMapping;
import com.elcom.adminconsolebackend.exception.AuthorizationException;
import com.elcom.adminconsolebackend.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elcom.adminconsolebackend.config.api.RequestMappingConfig.API_MAPPING;

@Slf4j
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    @Autowired
    private AuthzClient authzClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        if (request.getRequestURI().equals("/error") || request.getRequestURI().contains("/user-manager/accessible")
                || request.getRequestURI().equals("/user") || request.getRequestURI().contains("/user/update-password")
                || request.getRequestURI().contains("/actuator/prometheus") || request.getRequestURI().contains("/monitors")
                || request.getRequestURI().equals("/history") || request.getRequestURI().equals("/history/filter/user")
                || request.getRequestURI().contains("/ip-access")) {
            return true;
        }

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        Map<String, ApiMapping> apiMappings = API_MAPPING.get(request.getRequestURI().split("/")[1]);

        if (apiMappings == null || apiMappings.isEmpty()) {
            throw new ResourceNotFoundException("Service không tồn tại.");
        }

        ApiMapping currentUriMapping = apiMappings.get(request.getMethod() + path);

        if (currentUriMapping == null) {
            //TODO: đổi thành message khác khi ở product
            throw new ResourceNotFoundException("Request không tồn tại.");
        }

        // bypass service account
        Optional<Boolean> isServiceAccountOptional = Optional.ofNullable(
                (Boolean) ((Jwt) SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal())
                        .getClaims()
                        .get("is-service-account"));

        if (isServiceAccountOptional.isPresent() && isServiceAccountOptional.get()) {
            return true;
        }

        String currentUsername = (String) ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaims().get("preferred_username");

        List<Authz.Check> checkRequests = currentUriMapping.getMappingResources().parallelStream()
                .map(mappingResource -> Authz.Check.newBuilder()
                        .setPrincipal("authz-user-" + currentUsername)
                        .setResourceKind(mappingResource.getResourceKindMapping())
                        .setResourceValue(mappingResource.getResourceValueMapping())
                        .setAction(mappingResource.getActionMapping())
                        .build())
                .collect(Collectors.toList());

        Authz.CheckResponse checkResponse = authzClient.checkPermision(checkRequests);

        if (!checkResponse.getChecksList().stream().anyMatch(Authz.CheckAnswer::getIsAllowed)) {
            throw new AuthorizationException("Bạn không có quyền truy cập tài nguyên này.");
        } else {
            return true;
        }
    }
}

package com.elcom.adminconsolebackend.util;

import com.elcom.adminconsolebackend.dto.user.CustomUserDetail;
import com.elcom.adminconsolebackend.dto.user.UserDTO;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@UtilityClass
public class SecurityUtils {

    /**
     * TODO Implements when authentication logic is implemented
     *
     * @return user id that working in current session
     */
    public static String getCurrentUserId() {
        return getAuthenticatedUserDTO().getUuid();
    }

    public static String getCurrentUserName() {
        return getAuthenticatedUserDTO().getUsername();
    }

    public static UserDTO getAuthenticatedUserDTO() {
        UserDTO userDTO = new UserDTO();
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        userDTO.setUsername(jwt.getClaim("preferred_username"));
        userDTO.setUuid(jwt.getClaim("sub"));
        return userDTO;
    }

    public static String getAuthToken() {
        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null)
            return null;
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }
}

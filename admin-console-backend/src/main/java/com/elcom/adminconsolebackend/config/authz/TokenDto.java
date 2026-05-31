package com.elcom.adminconsolebackend.config.authz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenDto {
    @JsonProperty("access_token")
    private String token;
    @JsonProperty("token_type")
    private String type;
}

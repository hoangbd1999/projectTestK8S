package com.elcom.adminconsolebackend.dto.user.request;

import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResetUserRequest {
    @NotNull
    @NotEmpty
    private String password;

    private String username;
}

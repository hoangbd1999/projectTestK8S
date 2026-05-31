package com.elcom.adminconsolebackend.dto.group;

import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateGroupRequest {
    @NotNull
    @NotEmpty
    private String name;
    private String email;
    private String phoneNumber;
    private String description;
    @NotNull
    private Boolean enable = true;
}

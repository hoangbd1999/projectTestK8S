package com.elcom.adminconsolebackend.dto.user.request;

import com.elcom.adminconsolebackend.contant.ApplicationType;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class UpdateUserRequest {
    @NotNull
    @NotEmpty
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private Boolean enable = true;
    private Map<String, String> attributes;
    private List<String> roleIds;
    private List<String> authzGroupIds;
    private List<ApplicationType> application;

    private List<String> dataSource;

    @NotNull
    @NotEmpty
    private String groupId;
}

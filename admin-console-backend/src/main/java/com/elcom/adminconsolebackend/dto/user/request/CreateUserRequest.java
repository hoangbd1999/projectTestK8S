package com.elcom.adminconsolebackend.dto.user.request;

import com.elcom.adminconsolebackend.contant.ApplicationType;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class CreateUserRequest {
    @NotNull
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9äöüÄÖÜ.\\-_]*$", message = "Tên tài khoản chỉ được nhập chữ, số và các kí tự “.”, “-”,“_”.")
    private String username;

    @NotNull
    @NotEmpty
    private String password;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private Boolean enable = true;
    private List<ApplicationType> application;
    @NotNull
    @NotEmpty
    private List<String> dataSource;
    private Map<String, String> attributes;

    @NotNull
    @NotEmpty
    private String groupId;

    private List<String> roleIds;
    private List<String> authzGroupIds;
}

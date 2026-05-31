package com.elcom.adminconsolebackend.dto.user;

import com.elcom.adminconsolebackend.dto.authz.RoleDTO;
import com.elcom.adminconsolebackend.entity.management.Group;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class UserDetailDTO {
    private String id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private Boolean enable;
    private Group group;
    private List<RoleDTO> roles;
    private List<String> application;
    private List<String> dataSource;
    private Map<String, String> attributes;
}

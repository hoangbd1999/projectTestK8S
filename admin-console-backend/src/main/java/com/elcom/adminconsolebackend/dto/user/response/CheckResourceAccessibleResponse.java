package com.elcom.adminconsolebackend.dto.user.response;

import com.elcom.adminconsolebackend.dto.authz.GroupDTO;
import com.elcom.adminconsolebackend.dto.authz.ResourceDTO;
import com.elcom.adminconsolebackend.dto.authz.RoleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CheckResourceAccessibleResponse {
    private List<GroupDTO> groups;
    private List<RoleDTO> roles;
    private List<ResourceDTO> resourceDTOS;
}

package com.elcom.adminconsolebackend.dto.user;

import com.elcom.adminconsolebackend.dto.authz.GroupDTO;
import com.elcom.adminconsolebackend.dto.authz.RoleDTO;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class UserCompactDTO {
    private String id;
    private String username;
    private List<GroupDTO> authzGroups;
    private List<RoleDTO> roles;
    private Boolean enable;
    private String group;

    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime createdTimestamp;
}

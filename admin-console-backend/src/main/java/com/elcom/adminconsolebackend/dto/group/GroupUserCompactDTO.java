package com.elcom.adminconsolebackend.dto.group;

import com.elcom.adminconsolebackend.entity.management.GroupMapping;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class GroupUserCompactDTO {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String description;
    private Boolean enable;
    private LocalDateTime createdDate;
    private Boolean isLocked;

    private List<GroupMapping> members;
}

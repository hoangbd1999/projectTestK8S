package com.elcom.adminconsolebackend.dto.group;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeleteGroupResponse {
    private Integer numDeletedSuccess;
    private Integer numDeletedFail;
}

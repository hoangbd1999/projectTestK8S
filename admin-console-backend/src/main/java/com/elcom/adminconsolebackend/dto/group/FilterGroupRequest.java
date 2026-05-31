package com.elcom.adminconsolebackend.dto.group;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FilterGroupRequest {
    private String search;
}

package com.elcom.adminconsolebackend.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ObjectNameDTO {
    private String name;
    private String id;
    private Boolean enabled;
    private List<String> groups;
}

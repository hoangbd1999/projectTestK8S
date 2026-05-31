package com.elcom.adminconsolebackend.dto.authz;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResourceDTO {
    private String name;
    private String kind;
    private String value;
}

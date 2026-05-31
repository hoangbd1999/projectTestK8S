package com.elcom.adminconsolebackend.dto.authz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GroupDTO {
    private String id;
    private String name;

    @JsonIgnore
    private List<String> policies;
}

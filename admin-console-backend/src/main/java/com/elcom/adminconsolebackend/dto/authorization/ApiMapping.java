package com.elcom.adminconsolebackend.dto.authorization;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class ApiMapping {
    private String uri;
    private String method;
    private List<MappingResource> mappingResources;

    @Data
    @Accessors(chain = true)
    public static class MappingResource implements Serializable {
        private String resourceKindMapping;
        private String resourceValueMapping;
        private String actionMapping;
    }
}

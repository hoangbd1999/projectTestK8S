package com.elcom.adminconsolebackend.dto;

import com.elcom.adminconsolebackend.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class WarningSettingGeneralDTO {


    private String id;

    private String warningChannel;

    @ColumnTransformer(write = "?::jsonb")
    private String warningObjects;

    private List<String> warningObjectNames;

    @ColumnTransformer(write = "?::jsonb")
    private String resourceTypes;

    @ColumnTransformer(write = "?::jsonb")
    private String warningLevels;

    private String emailSend;

    private String passwordEmailSend;

    private int status;

    @CreationTimestamp
    private LocalDateTime modifiedAt;

    private String modifiedBy;

    public List<String> getWarningObjects() {
        if (this.warningObjects == null) return Collections.emptyList();
        List<String> typeRetriever = new ArrayList<>();
        return JsonUtils.fromJson(this.warningObjects, typeRetriever.getClass());
    }

    public List<String> getResourceTypes() {
        List<String> typeRetriever = new ArrayList<>();
        return JsonUtils.fromJson(this.resourceTypes, typeRetriever.getClass());
    }

    public void setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = JsonUtils.toJson(resourceTypes);
    }


    public List<String> getWarningLevels() {
        if (this.warningLevels == null) return Collections.emptyList();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Object> rawList = mapper.readValue(this.warningLevels, List.class);
            return rawList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}

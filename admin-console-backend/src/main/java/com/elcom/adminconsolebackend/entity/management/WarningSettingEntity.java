package com.elcom.adminconsolebackend.entity.management;

import com.elcom.adminconsolebackend.util.JsonUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "warning_setting", schema = "admin_console")
public class WarningSettingEntity implements Serializable {

    @Id
    private String id;

    private String warningChannel;

    @ColumnTransformer(write = "?::jsonb")
    private String warningObjects;

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


    @Transient
    public List<String> getWarningObjects() {
        List<String> typeRetriever = new ArrayList<>();
        return JsonUtils.fromJson(this.warningObjects, typeRetriever.getClass());
    }

    public void setWarningObjects(List<String> warningObjects) {
        this.warningObjects = JsonUtils.toJson(warningObjects);
    }

    @Transient
    public List<String> getResourceTypes() {
        List<String> typeRetriever = new ArrayList<>();
        return JsonUtils.fromJson(this.resourceTypes, typeRetriever.getClass());
    }

    public void setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = JsonUtils.toJson(resourceTypes);
    }

    @Transient
    public List<String> getWarningLevels() {
        List<String> typeRetriever = new ArrayList<>();
        return JsonUtils.fromJson(this.warningLevels, typeRetriever.getClass());
    }

    public void setWarningLevels(List<String> warningLevels) {
        this.warningLevels = JsonUtils.toJson(warningLevels);
    }

}

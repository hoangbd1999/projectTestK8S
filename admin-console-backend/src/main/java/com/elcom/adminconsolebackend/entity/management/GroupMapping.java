package com.elcom.adminconsolebackend.entity.management;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "group_mapping", schema = "admin_console")
@IdClass(GroupMapping.GroupMappingPK.class)
public class GroupMapping {
    @Id
    private String groupId;

    @Id
    private String userId;

    private String username;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @Data
    @Accessors(chain = true)
    public static class GroupMappingPK implements Serializable {
        private String groupId;
        private String userId;
    }
}

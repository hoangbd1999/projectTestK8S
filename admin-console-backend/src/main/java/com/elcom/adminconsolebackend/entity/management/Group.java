package com.elcom.adminconsolebackend.entity.management;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "group", schema = "admin_console")
public class Group {
    @Id
    private String id;

    private String name;

    private String email;

    private String phoneNumber;

    private String description;

    private Boolean enable;

    private Boolean isLocked = false;

    @CreationTimestamp
    private LocalDateTime createdDate;
}

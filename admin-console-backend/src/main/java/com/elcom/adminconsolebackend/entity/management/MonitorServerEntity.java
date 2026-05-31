package com.elcom.adminconsolebackend.entity.management;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "monitor_server", schema = "admin_console")
public class MonitorServerEntity implements Serializable {
    @Id
    private String id;

    private String name;

    private String ip;

    private String cpu;

    private String ram;

    private String disk;

    private int status;

    private String osType;

    private String domain;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String createdBy;
}

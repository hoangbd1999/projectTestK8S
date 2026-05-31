package com.elcom.adminconsolebackend.entity.management;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "history", schema = "admin_console")
public class History {
    @Id
    private String id;

    private String action;

    private String appName;

    private String username;

    private String resource;

    private String value;

    @Enumerated(EnumType.STRING)
    private HistoryStatus status;

    @CreationTimestamp
    private LocalDateTime createdDate;

    private String ipAddress;
}

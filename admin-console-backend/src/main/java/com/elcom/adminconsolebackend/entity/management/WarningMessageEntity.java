package com.elcom.adminconsolebackend.entity.management;

import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "warning_message_test", schema = "admin_console")
public class WarningMessageEntity implements Serializable {
    @Id
    private String id;

    private String application;

    private int warningLevel;

    private String description;

    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    private LocalDateTime warningTime;

    private String instance;

    private String resourceType;

}

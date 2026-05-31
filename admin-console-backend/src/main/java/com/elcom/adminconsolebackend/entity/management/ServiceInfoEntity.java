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
@Table(name = "service_info", schema = "admin_console")
public class ServiceInfoEntity implements Serializable {
    @Id
    private String serviceName;

    private String instance;

    private int type;

}

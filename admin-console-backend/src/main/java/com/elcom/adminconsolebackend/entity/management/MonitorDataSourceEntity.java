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
@Table(name = "monitor_data_source", schema = "admin_console")
public class MonitorDataSourceEntity implements Serializable {
    @Id
    private String id;

    private String dataSource;

    private int numberRecord;

    private String size;

    private int status;

    @CreationTimestamp
    private LocalDateTime timeIngest;

}

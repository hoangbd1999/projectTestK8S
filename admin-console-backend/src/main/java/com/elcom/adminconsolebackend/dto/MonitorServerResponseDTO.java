
package com.elcom.adminconsolebackend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MonitorServerResponseDTO implements Serializable {

    private String id;

    private String name;

    private String ip;

    private String cpu;

    private String ram;

    private String disk;

    private int status;

    private String description;

    private String cpuUsage;

    private String ramUsage;

    private String diskUsage;

    private String osType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String createdBy;

}

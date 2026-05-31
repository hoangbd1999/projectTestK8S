
package com.elcom.adminconsolebackend.dto;

import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class AlertResultDTO implements Serializable {

    private String id;

    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime warningTime ;

    private String description;

    private String warningLevel;

    private String application;

    private String instance;

    private String resourceType;

}

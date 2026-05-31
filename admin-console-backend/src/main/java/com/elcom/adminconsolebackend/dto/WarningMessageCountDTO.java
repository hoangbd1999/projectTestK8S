package com.elcom.adminconsolebackend.dto;

import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WarningMessageCountDTO {

    private String id;

    private String description;

    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    private LocalDateTime warningTime;

    private int countUnread;

}

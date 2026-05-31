package com.elcom.adminconsolebackend.dto;

import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class MonitorDataSourceGeneralDTO {

    private String id;

    private String dataSource;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date ingestDate;

    private Short status;

    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    private LocalDateTime latestTimeIngest;

    private Long numberRecord;

    private BigDecimal size;

    private Long rowCount;

}

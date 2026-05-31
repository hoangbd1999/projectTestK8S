package com.elcom.adminconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class IpAccessDTO {

    private Long id;
    private String ipAddress;
    private String description;
    private List<AccountLimitDTO> accounts;
    private String createdBy;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    private Integer status;

}

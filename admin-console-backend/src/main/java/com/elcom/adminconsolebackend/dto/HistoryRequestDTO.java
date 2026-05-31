package com.elcom.adminconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HistoryRequestDTO {
    @NotBlank(message = "Action not null")
    private String action;
    private String appName;
    private String userName;
    @NotBlank(message = "Resource not null")
    private String resource;
    private String value;
    @NotBlank(message = "Status not null")
    private String status;
}

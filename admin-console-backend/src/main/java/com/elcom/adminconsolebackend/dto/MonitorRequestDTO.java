package com.elcom.adminconsolebackend.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MonitorRequestDTO {
    private String search;
}

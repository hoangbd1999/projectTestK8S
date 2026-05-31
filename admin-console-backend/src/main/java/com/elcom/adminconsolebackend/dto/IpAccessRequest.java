package com.elcom.adminconsolebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IpAccessRequest {

    private Long id;
    private String ipAddress;
    private String description;
    private Integer isAllowed; // B - W
    private List<AccountLimitDTO> accounts;

}

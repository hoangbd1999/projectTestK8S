package com.elcom.adminconsolebackend.dto.user.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserEventRequest {
    private List<String> type;
    private String client;
    private String user;
    private String dateFrom;
    private String dateTo;
    private String ipAddress;
    private Integer first;
    private Integer max;
}

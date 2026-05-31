package com.elcom.adminconsolebackend.dto.dashboard.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserStatisticResponse {
    private Integer userTotal;
    private Integer userEnable;
}

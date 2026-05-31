package com.elcom.adminconsolebackend.dto.dashboard.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GroupStatisticResponse {
    private Long groupTotal;
    private Long groupEnable;
}

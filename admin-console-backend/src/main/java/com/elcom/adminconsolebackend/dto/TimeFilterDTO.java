package com.elcom.adminconsolebackend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TimeFilterDTO {

    private String startTime;

    private String endTime;

    private String dataSource;

}

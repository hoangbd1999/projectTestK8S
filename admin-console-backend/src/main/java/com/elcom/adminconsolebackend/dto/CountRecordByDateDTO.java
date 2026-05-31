package com.elcom.adminconsolebackend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class CountRecordByDateDTO {

    private Date day;

    private Long total;

    private Long undefined;

    private Long message;

    private Long position;

}

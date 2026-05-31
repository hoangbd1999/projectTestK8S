package com.elcom.adminconsolebackend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class DecodeLevelResponseDTO {

    private DecodeLevelDTO message;

    private DecodeLevelDTO undefined;

    private DecodeLevelDTO position;

}

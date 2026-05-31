package com.elcom.adminconsolebackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServerDeleteRequestDTO {

    private List<String> ips;

}

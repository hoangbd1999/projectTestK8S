package com.elcom.adminconsolebackend.dto.user.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FilterUserRequest {
    private String username;
}

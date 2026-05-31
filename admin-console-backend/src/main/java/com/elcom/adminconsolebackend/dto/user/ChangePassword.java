package com.elcom.adminconsolebackend.dto.user;

import lombok.Data;

@Data
public class ChangePassword {
    private String oldPassword;
    private String newPassword;
}

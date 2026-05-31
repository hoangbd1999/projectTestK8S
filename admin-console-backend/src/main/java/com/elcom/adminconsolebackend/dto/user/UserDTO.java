package com.elcom.adminconsolebackend.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private String uuid;

    private String username;


    private String email;

    private String mobile;


    private String fullName;

    @JsonIgnore
    private String password;

    private Integer status;

    private Integer emailVerify;

    private Integer mobileVerify;

    private String skype;

    private String facebook;

    private String avatar;

    private String address;

    private String birthDay;

    private Integer gender;

    @JsonIgnore
    private Timestamp createdAt;

    @JsonIgnore
    private Timestamp lastUpdate;

    private String loginIp;

    @JsonIgnore
    private Timestamp lastLogin;

    private int signupType;

    private String fbId;

    private String ggId;

    private String appleId;

    private Integer isDelete;

    private Integer setPassword;

    private Timestamp profileUpdate;

    private Timestamp avatarUpdate;

    private String otp;

    private Timestamp otpTime;

    private String otpMobile;

    private String otpPassword;

    private Timestamp otpPasswordTime;

    private String policeRank;

    private String position;

    @JsonIgnore
    public static final Integer STATUS_ACTIVE = 1;
    @JsonIgnore
    public static final Integer STATUS_LOCK = -1;

    @JsonIgnore
    private String matchingPassword;

    private String app;

    private Boolean isAdmin;

    private Boolean isServiceAccount;
}


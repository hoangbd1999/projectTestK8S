package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.dto.user.ChangePassword;
import com.elcom.adminconsolebackend.dto.user.request.*;
import com.elcom.adminconsolebackend.service.UserService;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserService userService;


    @GetMapping("")
    public ResponseEntity<ResponseMessage> getUserDetail() {
        return ResponseEntity.ok(ResponseMessage.success(userService.getUserInfo()));
    }


    @PutMapping("/update-password")
    public ResponseEntity<ResponseMessage> updatePassword(@RequestBody ChangePassword request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, true));
    }

    @PutMapping("")
    public ResponseEntity<ResponseMessage> updateInfo(@RequestBody @Valid UpdateUserRequest request) {
        userService.updateUser(request);
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, true));
    }
}

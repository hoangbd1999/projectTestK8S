package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @PostMapping("/user/avatar")
    public ResponseEntity<ResponseMessage> uploadAvatar() {
        return null;
    }
}

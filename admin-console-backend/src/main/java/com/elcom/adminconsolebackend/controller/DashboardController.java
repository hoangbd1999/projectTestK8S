package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/user")
    public ResponseEntity<ResponseMessage> getUserStatistic() {
        return ResponseEntity.ok(ResponseMessage.success(dashboardService.calculateUserStatistic()));
    }

    @GetMapping("/group")
    public ResponseEntity<ResponseMessage> getGroupStatistic() {
        return ResponseEntity.ok(ResponseMessage.success(dashboardService.calculateGroupStatistic()));
    }

    @GetMapping("/login")
    public ResponseEntity<ResponseMessage> getLoginStatistic() {
        return ResponseEntity.ok(ResponseMessage.success(dashboardService.getLoginStatistic()));
    }
}

package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.HistoryRequestDTO;
import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.service.HistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;


    @PostMapping("/filter")
    public ResponseEntity<ResponseMessage> filter(@RequestBody List<FilterCondition> conditions, Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.success(historyService.filter(conditions, pageable)));
    }

    @PostMapping("/filter/user")
    public ResponseEntity<ResponseMessage> filterUser(@RequestBody List<FilterCondition> conditions, Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.success(historyService.filterUser(conditions, pageable)));
    }

    @PostMapping("/filter/group/{groupId}")
    public ResponseEntity<ResponseMessage> filterGroup(@PathVariable String groupId, @RequestBody List<FilterCondition> conditions, Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, historyService.filterGroup(groupId, conditions, pageable)));
    }

    @PostMapping("")
    public ResponseEntity<ResponseMessage> create(HttpServletRequest req, @RequestBody @Valid HistoryRequestDTO request) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, historyService.save(req, request)));
    }

    @GetMapping("/report/user")
    public ResponseEntity<ResponseMessage> getUserReport() {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, historyService.getUserReport()));
    }

    @GetMapping("/report/resource")
    public ResponseEntity<ResponseMessage> getResourceReport() {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, historyService.getResourceReport()));
    }
}

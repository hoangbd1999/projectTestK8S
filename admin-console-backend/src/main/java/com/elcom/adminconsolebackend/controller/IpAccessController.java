package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.IpAccessRequest;
import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.service.IpAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ip-access")
public class IpAccessController {

    @Autowired
    IpAccessService ipAccessService;

    @PostMapping("/filter")
    public ResponseEntity<ResponseMessage> filterIpAccess(@RequestBody List<FilterCondition> conditions, Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.success(ipAccessService.filter(conditions, pageable)));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage> create(@RequestBody IpAccessRequest ipAccessRequest) {
        ipAccessService.create(ipAccessRequest);
        return ResponseEntity.ok(ResponseMessage.success(null));
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseMessage> delete(@RequestBody List<Long> ids) {
        ipAccessService.delete(ids);
        return ResponseEntity.ok(ResponseMessage.success(null));
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseMessage> update(@RequestBody IpAccessRequest ipAccessRequest) {
        ipAccessService.update(ipAccessRequest);
        return ResponseEntity.ok(ResponseMessage.success(null));
    }
}

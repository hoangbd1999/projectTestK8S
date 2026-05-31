package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.group.CreateGroupRequest;
import com.elcom.adminconsolebackend.dto.group.FilterGroupRequest;
import com.elcom.adminconsolebackend.dto.group.UpdateGroupRequest;
import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Path;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping("/{groupId}")
    public ResponseEntity<ResponseMessage> getGroupDetail(@PathVariable String groupId) {
        return ResponseEntity.ok(ResponseMessage.success(groupService.getGroupDetail(groupId)));
    }

    @PostMapping("/filter")
    public ResponseEntity<ResponseMessage> filterGroups(Pageable pageable, @RequestBody(required = false) FilterGroupRequest request) {
        return ResponseEntity.ok(ResponseMessage.success(groupService.filterGroups(pageable, request.getSearch())));
    }

    @GetMapping("/available")
    public ResponseEntity<ResponseMessage> getGroupsAvailable() {
        return ResponseEntity.ok(ResponseMessage.success(groupService.getAllGroupsAvailable()));
    }

    @GetMapping("/available/task/assigned")
    public ResponseEntity<ResponseMessage> getGroupsAvailableForUserTaskAssigned() {
        return ResponseEntity.ok(ResponseMessage.success(groupService.getAllGroupsAvailableAssigned()));
    }

    @PostMapping
    public ResponseEntity<ResponseMessage> createGroup(@RequestBody @Valid CreateGroupRequest request) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.CREATED, groupService.createGroup(request)));
    }

    @DeleteMapping
    public ResponseEntity<ResponseMessage> deleteGroups(@RequestParam List<String> groupIds) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, groupService.deleteGroups(groupIds)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ResponseMessage> updateGroup(@PathVariable String groupId, @RequestBody @Valid UpdateGroupRequest request) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, groupService.updateGroup(groupId, request)));
    }

    @PutMapping("/{groupId}/status")
    public ResponseEntity<ResponseMessage> updateGroupStatus(@PathVariable String groupId, @RequestParam Boolean enable) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, groupService.getGroupAndUpdateStatus(groupId, enable)));
    }

    @GetMapping("/parse-to-group-name-and-username")
    public ResponseEntity<ResponseMessage> getGroupNameAndUsernameFromIds(@RequestParam List<String> ids) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, groupService.parseUuidToUsername(ids)));
    }

    @GetMapping("/parse-to-username")
    public ResponseEntity<ResponseMessage> getUsernameFromIds(@RequestParam List<String> ids) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, groupService.parseToUsername(ids)));
    }

    @GetMapping("/get-group-by-user")
    public ResponseEntity<ResponseMessage> getGroupByUserId(@RequestParam String userId) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, groupService.getGroupIdFromUserId(userId)));
    }
}

package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.dto.user.request.*;
import com.elcom.adminconsolebackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user-manager")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/accessible")
    public ResponseEntity<ResponseMessage> getMenuOfCurrentUser() {
        return ResponseEntity.ok(ResponseMessage.success(userService.getMenuOfCurrentUser()));
    }

    @GetMapping("/filter")
    public ResponseEntity<ResponseMessage> filterUser(@Valid FilterUserRequest request) {
        return ResponseEntity.ok(ResponseMessage.success(userService.filterUser(request)));
    }

    @GetMapping("/filter/task/assigned")
    public ResponseEntity<ResponseMessage> filterUserTask(@Valid FilterUserRequest request) {
        return ResponseEntity.ok(ResponseMessage.success(userService.filterUserTask(request)));
    }


    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage> getUserDetail(@PathVariable String userId) {
        return ResponseEntity.ok(ResponseMessage.success(userService.getUserInfo(userId)));
    }

    @PostMapping("")
    public ResponseEntity<ResponseMessage> createUser(@RequestBody @Valid CreateUserRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.CREATED, null));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ResponseMessage> updateUser(@RequestBody @Valid UpdateUserRequest request, @PathVariable String userId) {
        userService.updateUser(request, userId);
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.NO_CONTENT, null));
    }

    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<ResponseMessage> resetUserPassword(@PathVariable String userId, @RequestBody ResetUserRequest request) {
        userService.resetUserPassword(userId, request);
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.NO_CONTENT, null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseMessage> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.NO_CONTENT, null));
    }

    @GetMapping("/resources")
    public ResponseEntity<ResponseMessage> getResourceAccessible(@RequestParam(name = "groupIds", defaultValue = "") List<String> groupIds,
                                                                 @RequestParam(name = "roleIds", defaultValue = "") List<String> roleIds) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, userService.getResourceAccessible(groupIds, roleIds)));
    }

    @GetMapping("/event")
    public ResponseEntity<ResponseMessage> getEventForUser(@Valid UserEventRequest filter) {
        return ResponseEntity.ok(ResponseMessage.success(HttpStatus.OK, userService.getEventForUser(filter.getType(), filter.getClient(), filter.getUser(), filter.getDateFrom(), filter.getDateTo(), filter.getIpAddress(), filter.getFirst(), filter.getMax())));
    }
}

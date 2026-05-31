package com.elcom.adminconsolebackend.service;

import com.elcom.adminconsolebackend.dto.ObjectNameDTO;
import com.elcom.adminconsolebackend.dto.group.*;
import com.elcom.adminconsolebackend.entity.management.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GroupService {
    Page<GroupUserCompactDTO> filterGroups(Pageable pageable, String search);

    GroupUserDetailDTO getGroupDetail(String groupId);

    Group createGroup(CreateGroupRequest request);

    @Transactional
    DeleteGroupResponse deleteGroups(List<String> groupIds);

    @Transactional
    Group updateGroup(String groupId, UpdateGroupRequest request);

    @Transactional
    Group getGroupAndUpdateStatus(String groupId, Boolean enable);

    List<ObjectNameDTO> parseUuidToUsername(List<String> ids);

    List<ObjectNameDTO> parseToUsername(List<String> ids);

    List<ObjectNameDTO> findToUsername(List<String> userNames);

    List<GroupUserCompactDTO> getAllGroupsAvailable();

    String getGroupIdFromUserId(String userId);

    List<GroupUserCompactDTO> getAllGroupsAvailableAssigned();
}

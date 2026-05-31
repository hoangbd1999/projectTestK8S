package com.elcom.adminconsolebackend.service.impl;

import com.authz.sdk.client.AuthzClient;
import com.authz.sdk.exceptions.AuthzException;
import com.authz.sdk.grpc.Authz;
import com.elcom.adminconsolebackend.dto.ObjectNameDTO;
import com.elcom.adminconsolebackend.dto.group.*;
import com.elcom.adminconsolebackend.dto.user.UserCompactDTO;
import com.elcom.adminconsolebackend.entity.management.Group;
import com.elcom.adminconsolebackend.entity.management.GroupMapping;
import com.elcom.adminconsolebackend.exception.ResourceConstraintException;
import com.elcom.adminconsolebackend.exception.ResourceNotFoundException;
import com.elcom.adminconsolebackend.repository.management.GroupMappingRepository;
import com.elcom.adminconsolebackend.repository.management.GroupRepository;
import com.elcom.adminconsolebackend.service.GroupService;
import com.elcom.adminconsolebackend.service.UserService;
import com.elcom.adminconsolebackend.util.SearchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final GroupMappingRepository groupMappingRepository;

    private final UserService userService;

    private final RealmResource realmResource;

    private final ObjectMapper objectMapper;

    private final AuthzClient authzClient;

    @Override
    public Page<GroupUserCompactDTO> filterGroups(Pageable pageable, String search) {
        String searchStr = SearchUtils.createSearchPatternFrom(search);
        if (pageable.getSort().isUnsorted()) {
            pageable = ((PageRequest) pageable).withSort(Sort.by(Sort.Order.desc("createdDate")));
        }
        Page<Group> groups = groupRepository.searchByNameLikeOrEmailLikeOrPhoneNumberLike(searchStr, searchStr, searchStr, pageable);
        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdIn(groups.stream().map(Group::getId).collect(Collectors.toList()));
        Map<String, List<GroupMapping>> groupMemberCount = groupMappings.stream().collect(Collectors.groupingBy(GroupMapping::getGroupId));
        Page<GroupUserCompactDTO> rs = groups.map(group -> {
            GroupUserCompactDTO groupUserCompactDTO = objectMapper.convertValue(group, GroupUserCompactDTO.class);
            List<GroupMapping> memberMappings = groupMemberCount.getOrDefault(group.getId(), new ArrayList<>());
            groupUserCompactDTO.setMembers(memberMappings);
            return groupUserCompactDTO;
        });
        return rs;
    }

    @Override
    public GroupUserDetailDTO getGroupDetail(String groupId) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            throw new ResourceNotFoundException("Group không tồn tại.");
        }

        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdEquals(groupId);
        List<UserRepresentation> usersInKeycloak = groupMappings.stream()
                .map(groupMapping -> realmResource.users().get(groupMapping.getUserId()).toRepresentation())
                .toList();

        List<UserCompactDTO> userCompactDTOS = userService.generateUserCompactDTOSFromUserRepresentations(usersInKeycloak);

        GroupUserDetailDTO groupUserDetailDTO = objectMapper.convertValue(groupOptional.get(), GroupUserDetailDTO.class);
        groupUserDetailDTO.setMembers(userCompactDTOS);
        return groupUserDetailDTO;
    }

    @Override
    @Transactional
    public Group createGroup(CreateGroupRequest request) {
        Group newGroup = objectMapper.convertValue(request, Group.class);
        newGroup.setId(UUID.randomUUID().toString());
        newGroup.setEnable(true);
        newGroup.setIsLocked(false);
        return groupRepository.save(newGroup);
    }

    @Override
    @Transactional
    public DeleteGroupResponse deleteGroups(List<String> groupIds) {
        List<Group> groups = groupRepository.findAllById(groupIds);

        List<String> groupsToDelete = groups.stream()
                .filter(group -> !group.getIsLocked())
                .map(Group::getId)
                .toList();

        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdIn(groupsToDelete);

        Group groupDefault = groupRepository.getGroupDefault();

        groupRepository.deleteAllById(groupsToDelete);

        //Chuyển người dùng về nhóm mặc định.
        groupMappingRepository.deleteAllInBatch(groupMappings);

        List<GroupMapping> newGroupMappings = groupMappings.stream().map(groupMapping -> {
            return new GroupMapping().setGroupId(groupDefault.getId())
                    .setUserId(groupMapping.getUserId())
                    .setUsername(groupMapping.getUsername());
        }).toList();

        groupMappingRepository.saveAll(newGroupMappings);

        return new DeleteGroupResponse()
                .setNumDeletedFail(groupIds.size() - groupsToDelete.size())
                .setNumDeletedSuccess(groupsToDelete.size());
    }

    @Override
    @Transactional
    public Group updateGroup(String groupId, UpdateGroupRequest request) {
        Group group = getGroupAndUpdateStatus(groupId, request.getEnable());
        BeanUtils.copyProperties(request, group);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public Group getGroupAndUpdateStatus(String groupId, Boolean enable) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            throw new ResourceNotFoundException("Group không tồn tại.");
        }

        Group group = groupOptional.get();

        if (group.getIsLocked()) {
            throw new ResourceConstraintException("Không thể sửa nhóm mặc định.");
        }

        if (!group.getEnable().equals(enable)) {
            group.setEnable(enable);
            List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdEquals(groupId);
            List<String> userIds = groupMappings.stream().map(GroupMapping::getUserId).toList();
            userIds.stream()
                    .forEach(userId -> {
                        UserResource userResource = realmResource.users().get(userId);
                        UserRepresentation userRepresentation = userResource.toRepresentation();
                        userRepresentation.setEnabled(enable);
                        userResource.update(userRepresentation);
                        if (!enable) {
                            userResource.logout();
                        }
                    });
        }

        return group;
    }

    @Override
    public List<ObjectNameDTO> parseUuidToUsername(List<String> ids) {
        List<Group> groups = groupRepository.findAllById(ids);
        List<String> groupIds = groups.stream().map(Group::getId).toList();
        List<UserRepresentation> users = ids.stream().filter(id -> !groupIds.contains(id))
                .map(userId -> {
                    try {
                        return realmResource.users().get(userId).toRepresentation();
                    } catch (NotFoundException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return Stream.concat(
                        groups.stream()
                                .map(group -> new ObjectNameDTO()
                                        .setName(group.getName())
                                        .setId(group.getId())
                                        .setGroups(Collections.singletonList(group.getId()))),
                        users.stream()
                                .map(user -> new ObjectNameDTO()
                                        .setId(user.getId())
                                        .setName(user.getUsername())
                                        .setEnabled(user.isEnabled())
                                        .setGroups(user.getGroups())))
                .distinct()
                .toList();
    }

    @Override
    public List<ObjectNameDTO> parseToUsername(List<String> ids) {
        List<Group> groups = groupRepository.findAllById(ids);
        List<String> groupIds = groups.stream().map(Group::getId).toList();

        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdIn(groupIds);

        List<UserRepresentation> users = ids.stream().filter(id -> !groupIds.contains(id))
                .map(userId -> {
                    try {
                        return realmResource.users().get(userId).toRepresentation();
                    } catch (NotFoundException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return Stream.concat(
                        groupMappings.stream()
                                .map(groupMapping -> new ObjectNameDTO()
                                        .setName(groupMapping.getUsername())
                                        .setId(groupMapping.getUserId())
                                        .setGroups(Collections.singletonList(groupMapping.getGroupId()))),
                        users.stream()
                                .map(user -> new ObjectNameDTO()
                                        .setId(user.getId())
                                        .setName(user.getUsername())
                                        .setEnabled(user.isEnabled())
                                        .setGroups(user.getGroups())))
                .distinct()
                .toList();
    }

    @Override
    public List<ObjectNameDTO> findToUsername(List<String> userNames) {
        List<GroupMapping> groupMappings = groupMappingRepository.findByUsernameIn(userNames);
        return groupMappings.stream()
                .map(groupMapping -> new ObjectNameDTO()
                        .setName(groupMapping.getUsername())
                        .setId(groupMapping.getUserId())
                        .setGroups(Collections.singletonList(groupMapping.getGroupId())))
                .distinct()
                .toList();
    }

    @Override
    public List<GroupUserCompactDTO> getAllGroupsAvailable() {
        List<Group> groups = groupRepository.getAllGroupsAvailable();
        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdIn(groups.stream().map(Group::getId).collect(Collectors.toList()));
        List<String> listUserEnable = realmResource.users().search(false, 0, 1000, true, false)
                .stream().map(UserRepresentation::getUsername).toList();
        Map<String, List<GroupMapping>> groupMemberCount = groupMappings.stream()
                .filter(item -> listUserEnable.contains(item.getUsername()))
                .collect(Collectors.groupingBy(GroupMapping::getGroupId));
        return groups.stream()
                .map(group -> {
                    GroupUserCompactDTO groupUserCompactDTO = objectMapper.convertValue(group, GroupUserCompactDTO.class);
                    List<GroupMapping> memberMappings = groupMemberCount.getOrDefault(group.getId(), new ArrayList<>());
                    groupUserCompactDTO.setMembers(memberMappings);
                    return groupUserCompactDTO;
                })
                .filter(item -> !item.getMembers().isEmpty())
                .toList();
    }

    @Override
    public String getGroupIdFromUserId(String userId) {
        return groupMappingRepository.findByUserIdEquals(userId).getGroupId();
    }

    @Override
    public List<GroupUserCompactDTO> getAllGroupsAvailableAssigned() {
        List<Group> groups = groupRepository.getAllGroupsAvailable();
        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdIn(groups.stream().map(Group::getId).collect(Collectors.toList()));
        Set<String> userNames = realmResource.users().search(false, 0, 1000, true, false)
                .stream()
                .map(UserRepresentation::getUsername)
                .filter(username -> username.length() < 30)
                .collect(Collectors.toSet());
        List<String> userAccess = getUserAccess(userNames);
        Map<String, List<GroupMapping>> groupMemberCount = groupMappings.stream().filter((groupMapping -> userAccess.contains(groupMapping.getUsername()))).collect(Collectors.groupingBy(GroupMapping::getGroupId));
        return groups.stream().map(group -> {
                    GroupUserCompactDTO groupUserCompactDTO = objectMapper.convertValue(group, GroupUserCompactDTO.class);
                    List<GroupMapping> memberMappings = groupMemberCount.getOrDefault(group.getId(), new ArrayList<>());
                    groupUserCompactDTO.setMembers(memberMappings);
                    return groupUserCompactDTO;
                })
                .filter(item -> !item.getMembers().isEmpty())
                .toList();
    }

    private List<String> getUserAccess(Set<String> userNames) {
        List<String> userAccess;
        try {
            List<Authz.Check> checkRequests = userNames.parallelStream()
                    .map(user -> Authz.Check.newBuilder()
                            .setPrincipal("authz-user-" + user)
                            .setResourceKind("metaint.metacen.task")
                            .setResourceValue("*")
                            .setAction("update")
                            .build())
                    .collect(Collectors.toList());

            Authz.CheckResponse checkResponse = authzClient.checkPermision(checkRequests);
            userAccess = checkResponse.getChecksList().stream().filter(Authz.CheckAnswer::getIsAllowed).map((item) -> item.getPrincipal().substring(item.getPrincipal().lastIndexOf("authz-user-") + 11)).collect(Collectors.toList());
            return userAccess;
        } catch (AuthzException e) {
            throw new RuntimeException(e);
        }
    }
}

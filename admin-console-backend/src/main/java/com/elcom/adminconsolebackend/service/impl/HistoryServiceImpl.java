package com.elcom.adminconsolebackend.service.impl;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.FilterOperator;
import com.elcom.adminconsolebackend.dto.HistoryRequestDTO;
import com.elcom.adminconsolebackend.dto.report.ResourceReport;
import com.elcom.adminconsolebackend.dto.report.UserReport;
import com.elcom.adminconsolebackend.dto.user.UserCompactDTO;
import com.elcom.adminconsolebackend.entity.management.GroupMapping;
import com.elcom.adminconsolebackend.entity.management.History;
import com.elcom.adminconsolebackend.repository.management.GroupMappingRepository;
import com.elcom.adminconsolebackend.repository.management.GroupRepository;
import com.elcom.adminconsolebackend.repository.management.HistoryFilterRepository;
import com.elcom.adminconsolebackend.repository.management.HistoryRepository;
import com.elcom.adminconsolebackend.service.HistoryService;
import com.elcom.adminconsolebackend.service.UserService;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryFilterRepository filterRepository;
    private final ModelMapper modelMapper;

    private final HistoryRepository historyRepository;
    private final GroupRepository groupRepository;

    private final GroupMappingRepository groupMappingRepository;

    private final UserService userService;

    private final RealmResource realmResource;

    @Override
    public Page<History> filter(List<FilterCondition> conditions, Pageable pageable) {
        return filterRepository.filter(conditions, pageable);
    }

    @Override
    public Page<History> filterUser(List<FilterCondition> conditions, Pageable pageable) {
        FilterCondition filterUserName = FilterCondition.from("username", FilterOperator.EQUALS, List.of(SecurityUtils.getCurrentUserName()));
        conditions.add(filterUserName);
        return filterRepository.filter(conditions, pageable);
    }

    @Override
    public Page<History> filterGroup(String groupId, List<FilterCondition> conditions, Pageable pageable) {
        List<GroupMapping> groupMappings = groupMappingRepository.findByGroupIdEquals(groupId);
        List<UserRepresentation> usersInKeycloak = groupMappings.stream()
                .map(groupMapping -> realmResource.users().get(groupMapping.getUserId()).toRepresentation())
                .toList();

        List<UserCompactDTO> userCompactDTOS = userService.generateUserCompactDTOSFromUserRepresentations(usersInKeycloak);
        List<Object> userNames = userCompactDTOS.stream().map(t -> t.getUsername()).collect(Collectors.toList());
        FilterCondition filterUserName = FilterCondition.from("username", FilterOperator.EQUALS, userNames);
        conditions.add(filterUserName);
        return filterRepository.filter(conditions, pageable);
    }

    @Override
    public History save(HttpServletRequest request, HistoryRequestDTO req) {
        History eventEntity = modelMapper.map(req, History.class);
        eventEntity.setId(UUID.randomUUID().toString());
        eventEntity.setUsername(SecurityUtils.getCurrentUserName());
        eventEntity.setIpAddress(getClientIpAddr(request));
        historyRepository.save(eventEntity);
        return eventEntity;
    }

    private String getClientIpAddr(HttpServletRequest request) {
        List<String> headers = Arrays.asList("X-FORWARDED-FOR", "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR", "X-Real-IP");
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // Trường hợp có nhiều IP, lấy IP đầu tiên
                return ip.split(",")[0];
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    public List<ResourceReport> getResourceReport() {
        return historyRepository.getResourceReport();
    }

    @Override
    public List<UserReport> getUserReport() {
        return historyRepository.getUserReport();
    }
}

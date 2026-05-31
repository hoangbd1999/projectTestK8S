package com.elcom.adminconsolebackend.service.impl;


import com.elcom.adminconsolebackend.config.MessageLanguage;
import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.entity.management.WarningSettingEntity;
import com.elcom.adminconsolebackend.exception.ResourceNotFoundException;
import com.elcom.adminconsolebackend.repository.management.WarningSettingFilterRepository;
import com.elcom.adminconsolebackend.repository.management.WarningSettingRepository;
import com.elcom.adminconsolebackend.service.GroupService;
import com.elcom.adminconsolebackend.service.WarningSettingService;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class WarningSettingServiceImpl implements WarningSettingService {

    private final ModelMapper modelMapper;

    private final MessageLanguage messageLanguage;

    private final WarningSettingRepository warningSettingRepository;

    private final WarningSettingFilterRepository warningSettingFilterRepository;

    private final GroupService groupService;

    @Override
    public Page<WarningSettingGeneralDTO> filterWarningSetting(List<FilterCondition> conditions, Pageable pageable) throws JsonProcessingException {
        conditions = addConditions(conditions);

        Page<WarningSettingGeneralDTO> warningPage = warningSettingFilterRepository
                .filterWarningSetting(conditions, pageable)
                .map(entity -> modelMapper.map(entity, WarningSettingGeneralDTO.class));

        return fetchWarningObjects(warningPage);
    }

    private List<FilterCondition> addConditions(List<FilterCondition> conditions) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Tìm điều kiện warning_objects (nếu có)
        Optional<FilterCondition> warningConditionOpt = conditions.stream()
                .filter(cond -> "warning_objects".equals(cond.getAttrCode()))
                .findFirst();

        // Nếu không có điều kiện warning_objects thì không cần xử lý thêm
        if (warningConditionOpt.isEmpty()) {
            return conditions;
        }

        FilterCondition warningCondition = warningConditionOpt.get();

        // Parse 'data' để lấy danh sách username
        JsonNode dataNode = mapper.readTree(warningCondition.getData());
        List<String> usernames = new ArrayList<>();
        if (dataNode.has("value")) {
            usernames = mapper.convertValue(dataNode.get("value"), new TypeReference<List<String>>() {});
        }

        // Gọi service để lấy userIds từ usernames
        List<ObjectNameDTO> users = groupService.findToUsername(usernames);
        List<String> userIds = users.stream()
                .map(ObjectNameDTO::getId)
                .collect(Collectors.toList());


        if(warningCondition.getOperator().equals(FilterOperator.MULTI_SELECTION_CONTAINS)) {
            userIds.addAll(usernames);
        }
        // Tạo điều kiện mới
        FilterCondition newCond = new FilterCondition();
        newCond.setAttrCode("warning_objects");
        newCond.setOperator(warningCondition.getOperator());

        Map<String, List<String>> data = new HashMap<>();
        data.put("value", userIds);
        newCond.setData(mapper.writeValueAsString(data));

        if(!userIds.isEmpty()) {
            // Xoá điều kiện cũ
            conditions.removeIf(item -> "warning_objects".equals(item.getAttrCode()));
            // Thêm lại vào danh sách
            conditions.add(newCond);
        }

        return conditions;
    }


    @Override
    public WarningSettingEntity save(WarningSettingRequestDTO req) {

        WarningSettingEntity warningSettingEntity = modelMapper.map(req, WarningSettingEntity.class);
        warningSettingEntity.setId(UUID.randomUUID().toString());
        List<String> warningObjects = CollectionUtils.isEmpty(req.getWarningObjects()) ? null : req.getWarningObjects();
        List<String> resourceTypes = CollectionUtils.isEmpty(req.getResourceTypes()) ? null : req.getResourceTypes();
        List<String> warningLevels = CollectionUtils.isEmpty(req.getWarningLevels()) ? null : req.getWarningLevels();
        warningSettingEntity.setWarningObjects(warningObjects);
        warningSettingEntity.setResourceTypes(resourceTypes);
        warningSettingEntity.setWarningLevels(warningLevels);
        warningSettingEntity.setModifiedBy(SecurityUtils.getCurrentUserName());

        warningSettingRepository.save(warningSettingEntity);

        return warningSettingEntity;
    }

    @Override
    public WarningSettingEntity update(WarningSettingRequestDTO req) {
        WarningSettingEntity updatedWarningSetting = warningSettingRepository.findById(req.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Setting not exist with id = " + req.getId()));

        modelMapper.map(req, updatedWarningSetting);

        List<String> warningObjects = !CollectionUtils.isEmpty(req.getWarningObjects())
                ? req.getWarningObjects()
                : updatedWarningSetting.getWarningObjects();

        List<String> resourceTypes = !CollectionUtils.isEmpty(req.getResourceTypes())
                ? req.getResourceTypes()
                : updatedWarningSetting.getResourceTypes();

        List<String> warningLevels = !CollectionUtils.isEmpty(req.getWarningLevels())
                ? req.getWarningLevels()
                : updatedWarningSetting.getWarningLevels();

        updatedWarningSetting.setWarningObjects(warningObjects);
        updatedWarningSetting.setResourceTypes(resourceTypes);
        updatedWarningSetting.setWarningLevels(warningLevels);
        updatedWarningSetting.setModifiedBy(SecurityUtils.getCurrentUserName());
        updatedWarningSetting.setModifiedAt(DateUtils.convertToLocalDateTime(new Date()));

        updatedWarningSetting = warningSettingRepository.save(updatedWarningSetting);

        return updatedWarningSetting;
    }

    @Override
    public WarningSettingEntity getDetailWarningSetting(String id) {
        WarningSettingEntity updatedWarningSetting = warningSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not exist with id = " + id));

        return updatedWarningSetting;
    }

    public Page<WarningSettingGeneralDTO> fetchWarningObjects(Page<WarningSettingGeneralDTO> warningPage) {
        Set<String> userAndGroupIds = warningPage.getContent().stream()
                .flatMap(w -> Optional.ofNullable(w.getWarningObjects()).stream().flatMap(Collection::stream))
                .collect(Collectors.toSet());
        List<ObjectNameDTO> usersAndGroups = groupService.parseUuidToUsername(new ArrayList<>(userAndGroupIds));
        Map<String, String> mapNameById = usersAndGroups
                .stream()
                .filter(item -> item.getEnabled() == null || item.getEnabled())
                .collect(Collectors.toMap(ObjectNameDTO::getId, ObjectNameDTO::getName));

        return warningPage.map(job -> {
            if(job.getWarningChannel().equals("In-App")) {
            List<String> warningObjectNames = job.getWarningObjects().stream().map(mapNameById::get)
                    .collect(Collectors.toList());
                job.setWarningObjectNames(warningObjectNames);
            }
            return job;
        });
    }

}

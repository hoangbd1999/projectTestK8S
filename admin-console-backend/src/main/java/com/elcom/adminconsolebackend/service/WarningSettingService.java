package com.elcom.adminconsolebackend.service;


import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.entity.management.WarningSettingEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarningSettingService {

    WarningSettingEntity save(WarningSettingRequestDTO req);

    WarningSettingEntity update(WarningSettingRequestDTO req);

    WarningSettingEntity getDetailWarningSetting(String id);

    Page<WarningSettingGeneralDTO> filterWarningSetting(List<FilterCondition> conditions, Pageable pageable) throws JsonProcessingException;

}

package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.entity.management.WarningSettingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarningSettingFilterRepository {

    Page<WarningSettingEntity> filterWarningSetting(List<FilterCondition> conditions, Pageable pageable);

}

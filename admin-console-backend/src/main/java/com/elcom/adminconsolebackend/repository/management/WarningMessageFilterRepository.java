package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.WarningMessageGeneralDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarningMessageFilterRepository {

    Page<WarningMessageGeneralDTO> filterWarning(List<FilterCondition> conditions, Pageable pageable, String userId);

}

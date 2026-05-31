package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.MonitorDataSourceGeneralDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonitorDataSourceFilterRepository {

    Page<MonitorDataSourceGeneralDTO> filterMonitorDataSource(List<FilterCondition> conditions, Pageable pageable);

}

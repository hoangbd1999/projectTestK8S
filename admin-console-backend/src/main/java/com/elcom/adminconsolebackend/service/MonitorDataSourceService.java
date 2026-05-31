package com.elcom.adminconsolebackend.service;


import com.elcom.adminconsolebackend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonitorDataSourceService {

    Page<MonitorDataSourceGeneralDTO> filterMonitorDataSource(List<FilterCondition> conditions, Pageable pageable);

    Long countRecordDataSource(TimeFilterDTO timeFilterDTO);

    Float countSizeDataSource(TimeFilterDTO timeFilterDTO);

    DecodeLevelResponseDTO countDecodeLevel(TimeFilterDTO timeFilterDTO);

    List<CountStationResponseDTO> countStation(TimeFilterDTO timeFilterDTO);

    List<CountRecordByDateDTO> countRecordByDate(TimeFilterDTO timeFilterDTO);

    Integer getStatusDataSource(String dataSource);

}

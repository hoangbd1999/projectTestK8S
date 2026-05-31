package com.elcom.adminconsolebackend.service;


import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.entity.management.MonitorServerEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonitorService {

    MonitorServerEntity save(MonitorServerRequestDTO req);

    MonitorServerEntity update(MonitorServerRequestDTO req);

    void delete(List<String> ips);

    List<MonitorServerResponseDTO> getList(String search);

    MonitorActiveResponseDTO getMonitorActive() throws JsonProcessingException;

//    Page<AlertResultDTO> getAlertServerMonitor(AlertRequestDTO req, Pageable pageable);

//    Page<AlertResultDTO> getAlertServiceMonitor(AlertRequestDTO req, Pageable pageable);

    int checkStatus(String ip);

    String getServiceInfo(String serverName);

    List<String> getNameServer();

    ResourceDTO getMonitorResource();

    List<String> getMonitorResourceType();

    void changeRead(String id);

    void changeReadAll();

    Page<WarningMessageGeneralDTO> filterWarning(List<FilterCondition> conditions, Pageable pageable);

    void saveWarningMonitor();

    int countWarningUnreadByUser();

    WarningMessageCountResponseDTO countWarningByUser(String status);

    List<WarningMessageGeneralDTO> getAllWarningByUser(String status);


//    IgnoreRequestDTO updateIgnore(IgnoreRequestDTO req, int checkReopens) throws JsonProcessingException;


}

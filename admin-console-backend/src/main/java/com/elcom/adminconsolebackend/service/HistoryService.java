package com.elcom.adminconsolebackend.service;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.HistoryRequestDTO;
import com.elcom.adminconsolebackend.dto.report.ResourceReport;
import com.elcom.adminconsolebackend.dto.report.UserReport;
import com.elcom.adminconsolebackend.entity.management.History;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HistoryService {
    Page<History> filter(List<FilterCondition> conditions, Pageable pageable);

    Page<History> filterUser(List<FilterCondition> conditions, Pageable pageable);

    Page<History> filterGroup(String groupId, List<FilterCondition> conditions, Pageable pageable);

    History save(HttpServletRequest request, HistoryRequestDTO req);

    List<ResourceReport> getResourceReport();

    List<UserReport> getUserReport();
}

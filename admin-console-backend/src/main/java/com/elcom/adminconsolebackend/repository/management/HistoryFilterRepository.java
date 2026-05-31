package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.entity.management.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HistoryFilterRepository {
    Page<History> filter(List<FilterCondition> conditions, Pageable pageable);
}

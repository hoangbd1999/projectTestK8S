package com.elcom.adminconsolebackend.service;

import com.elcom.adminconsolebackend.dto.dashboard.response.GroupStatisticResponse;
import com.elcom.adminconsolebackend.dto.dashboard.response.UserStatisticResponse;

import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {
    UserStatisticResponse calculateUserStatistic();

    GroupStatisticResponse calculateGroupStatistic();

    Map<LocalDate, Long> getLoginStatistic();
}

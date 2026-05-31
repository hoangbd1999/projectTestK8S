package com.elcom.adminconsolebackend.service.impl;

import com.elcom.adminconsolebackend.dto.dashboard.response.GroupStatisticResponse;
import com.elcom.adminconsolebackend.dto.dashboard.response.UserStatisticResponse;
import com.elcom.adminconsolebackend.repository.management.GroupRepository;
import com.elcom.adminconsolebackend.service.DashboardService;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final RealmResource realmResource;

    private final GroupRepository groupRepository;

    @Override
    public UserStatisticResponse calculateUserStatistic() {
        List<UserRepresentation> allUsersInKc = realmResource.users().list();
        Long numUsersEnable = allUsersInKc.stream()
                .filter(UserRepresentation::isEnabled)
                .count();
        return new UserStatisticResponse()
                .setUserTotal(allUsersInKc.size())
                .setUserEnable(numUsersEnable.intValue());
    }

    @Override
    public GroupStatisticResponse calculateGroupStatistic() {
        Long numGroupsTotal = groupRepository.count();
        Long numGroupsEnable = groupRepository.countNumGroupsEnable();
        return new GroupStatisticResponse()
                .setGroupTotal(numGroupsTotal)
                .setGroupEnable(numGroupsEnable);
    }

    @Override
    public Map<LocalDate, Long> getLoginStatistic() {
        String dateTo = DateUtils.format(LocalDate.now().plusDays(1), null);
        String dateFrom = DateUtils.format(LocalDate.now().minusDays(6), null);

        List<EventRepresentation> loginEvents = realmResource.getEvents(List.of("LOGIN"), null, null, dateFrom, dateTo, null, 0, Integer.MAX_VALUE);
        Map<LocalDate, Long> realLoginEvents = loginEvents.stream().collect(groupingBy(event -> DateUtils.getDateFromLong(event.getTime()).toLocalDate(), counting()));
        return LocalDate.now().minusDays(6).datesUntil(LocalDate.now().plusDays(1))
                .map(date -> {
                    Long numLoginOfDate = realLoginEvents.get(date);
                    return numLoginOfDate == null ? Map.entry(date, 0L) : Map.entry(date, numLoginOfDate);
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

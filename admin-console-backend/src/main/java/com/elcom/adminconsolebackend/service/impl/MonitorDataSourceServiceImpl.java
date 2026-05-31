package com.elcom.adminconsolebackend.service.impl;


import com.elcom.adminconsolebackend.config.MessageLanguage;
import com.elcom.adminconsolebackend.contant.TimeZoneUTC;
import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.entity.management.MonitorDataSourceEntity;
import com.elcom.adminconsolebackend.repository.clickhouse.MediaReportCustomizeRepository;
import com.elcom.adminconsolebackend.repository.management.MonitorDataSourceFilterRepository;
import com.elcom.adminconsolebackend.repository.management.MonitorDataSourceRepository;
import com.elcom.adminconsolebackend.repository.management.MonitorRepositoryCustomize;
import com.elcom.adminconsolebackend.service.MonitorDataSourceService;

import com.elcom.adminconsolebackend.util.StreamUtils;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorDataSourceServiceImpl implements MonitorDataSourceService {

    private final ModelMapper modelMapper;

    private final MessageLanguage messageLanguage;

    private final MonitorDataSourceFilterRepository monitorDataSourceFilterRepository;

    private final MediaReportCustomizeRepository mediaReportCustomizeRepository;

    private final MonitorRepositoryCustomize monitorRepositoryCustomize;

    private final MonitorDataSourceRepository monitorDataSourceRepository;


    @Override
    public Long countRecordDataSource(TimeFilterDTO timeFilterDTO) {
        String year = null;
        String monthStart = null;
        String dayStart = null;
        String monthEnd = null;
//        String startTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
//        String endTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";

        LocalDateTime startDateTime = DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00")
                .plusHours(DateUtils.GLOBAL_GMT);
        String startTime = DateUtils.getLongFromDateTimeLocal(startDateTime) * 1000000 + "";

        year = String.valueOf(startDateTime.getYear());
        monthStart = String.valueOf(startDateTime.getMonthValue());
        dayStart = String.valueOf(startDateTime.getDayOfMonth());

        LocalDateTime endDateTime = DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59")
                .plusHours(DateUtils.GLOBAL_GMT);
        String endTime = DateUtils.getLongFromDateTimeLocal(endDateTime) * 1000000 + "";

        monthEnd = String.valueOf(endDateTime.getMonthValue());

        return mediaReportCustomizeRepository.countNumberRecordInDay(startTime,endTime,year,monthStart,dayStart,monthEnd);
    }

    @Override
    public Float countSizeDataSource(TimeFilterDTO timeFilterDTO) {
        String startTime = timeFilterDTO.getStartTime() + " 00:00:00";
        String endTime = timeFilterDTO.getEndTime() + " 23:59:59";

        return monitorRepositoryCustomize.countSizeDataSource(startTime,endTime);
    }

    @Override
    public DecodeLevelResponseDTO countDecodeLevel(TimeFilterDTO timeFilterDTO) {
        try {
//            String startTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
//            String endTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
            String year = null;
            String monthStart = null;
            String monthEnd = null;

            LocalDateTime startDateTime = DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00")
                    .plusHours(DateUtils.GLOBAL_GMT);
            String startTime = DateUtils.getLongFromDateTimeLocal(startDateTime) * 1000000 + "";

            year = String.valueOf(startDateTime.getYear());
            monthStart = String.valueOf(startDateTime.getMonthValue());
         //   dayStart = String.valueOf(startDateTime.getDayOfMonth());

            LocalDateTime endDateTime = DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59")
                    .plusHours(DateUtils.GLOBAL_GMT);
            String endTime = DateUtils.getLongFromDateTimeLocal(endDateTime) * 1000000 + "";

            monthEnd = String.valueOf(endDateTime.getMonthValue());
          //  dayEnd = String.valueOf(endDateTime.getDayOfMonth());

            CountDecodeLevelDTO countDecodeLevelDTO = mediaReportCustomizeRepository.countDecodeLevel(startTime, endTime,year,monthStart,monthEnd);

            DecodeLevelResponseDTO response = new DecodeLevelResponseDTO();
            long total = countDecodeLevelDTO.getTotal() != null ? countDecodeLevelDTO.getTotal() : 0;

            DecodeLevelDTO undefined = new DecodeLevelDTO();
            undefined.setCountRecord(countDecodeLevelDTO.getUndefined());
            undefined.setPercent(total > 0 ? Math.round((countDecodeLevelDTO.getUndefined() * 100.0 / total) * 100.0) / 100.0 : 0.0);
            response.setUndefined(undefined);

            DecodeLevelDTO message = new DecodeLevelDTO();
            message.setCountRecord(countDecodeLevelDTO.getMessage());
            message.setPercent(total > 0 ? Math.round((countDecodeLevelDTO.getMessage() * 100.0 / total) * 100.0) / 100.0 : 0.0);
            response.setMessage(message);

            DecodeLevelDTO position = new DecodeLevelDTO();
            position.setCountRecord(countDecodeLevelDTO.getPosition());
            position.setPercent(total > 0 ? Math.round((countDecodeLevelDTO.getPosition() * 100.0 / total) * 100.0) / 100.0 : 0.0);
            response.setPosition(position);

            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CountStationResponseDTO> countStation(TimeFilterDTO timeFilterDTO) {
        try {
//            String startTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
//            String endTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
            String year = null;
            String monthStart = null;
            String monthEnd = null;

            LocalDateTime startDateTime = DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00")
                    .plusHours(DateUtils.GLOBAL_GMT);
            String startTime = DateUtils.getLongFromDateTimeLocal(startDateTime) * 1000000 + "";

            year = String.valueOf(startDateTime.getYear());
            monthStart = String.valueOf(startDateTime.getMonthValue());
        //    dayStart = String.valueOf(startDateTime.getDayOfMonth());

            LocalDateTime endDateTime = DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59")
                    .plusHours(DateUtils.GLOBAL_GMT);
            String endTime = DateUtils.getLongFromDateTimeLocal(endDateTime) * 1000000 + "";

            monthEnd = String.valueOf(endDateTime.getMonthValue());
         //   dayEnd = String.valueOf(endDateTime.getDayOfMonth());

            List<CountStationResponseDTO> countStation = mediaReportCustomizeRepository.countStation(startTime,endTime, year,monthStart,monthEnd);
            return countStation;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CountRecordByDateDTO> countRecordByDate(TimeFilterDTO timeFilterDTO) {
        try {
//            String startTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
//            String endTime = DateUtils.getLongFromDateTimeLocal(DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59").plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";

            String year = null;
            String monthStart = null;
            String monthEnd = null;

            LocalDateTime startDateTime = DateUtils.parse(timeFilterDTO.getStartTime() + " 00:00:00")
                    .plusHours(DateUtils.GLOBAL_GMT);
            String startTime = DateUtils.getLongFromDateTimeLocal(startDateTime) * 1000000 + "";

            year = String.valueOf(startDateTime.getYear());
            monthStart = String.valueOf(startDateTime.getMonthValue());
        //    dayStart = String.valueOf(startDateTime.getDayOfMonth());

            LocalDateTime endDateTime = DateUtils.parse(timeFilterDTO.getEndTime() + " 23:59:59")
                    .plusHours(DateUtils.GLOBAL_GMT);
            String endTime = DateUtils.getLongFromDateTimeLocal(endDateTime) * 1000000 + "";

            monthEnd = String.valueOf(endDateTime.getMonthValue());
        //    dayEnd = String.valueOf(endDateTime.getDayOfMonth());

            List<CountRecordByDateDTO> countRecordByDate = mediaReportCustomizeRepository.countRecordByDate(startTime,endTime, year,monthStart,monthEnd);
            return countRecordByDate;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer getStatusDataSource(String dataSource) {
        MonitorDataSourceEntity monitorDataSourceEntity = monitorDataSourceRepository.findFirstByDataSource(dataSource);
        return monitorDataSourceEntity.getStatus();
    }

    @Override
    public Page<MonitorDataSourceGeneralDTO> filterMonitorDataSource(List<FilterCondition> conditions, Pageable pageable) {
        boolean hasTimeIngestCond = conditions.stream().anyMatch(cond -> "time_ingest".equals(cond.getAttrCode()));
        if(!hasTimeIngestCond) {
            conditions = addConditions(conditions);
        }
        Page<MonitorDataSourceGeneralDTO> monitorDataSourcePage =  monitorDataSourceFilterRepository.filterMonitorDataSource(conditions, pageable);
        return fetchMonitorDataSource(conditions,monitorDataSourcePage, pageable);
    }

    private static List<FilterCondition> addConditions(List<FilterCondition> conditions) {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endTime = today.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);
        String startTimeFinal =  DateUtils.convertDateStringBetweenTimeZone(DateUtils.formatDate(startTime.plusHours(DateUtils.GLOBAL_GMT)),
                TimeZoneUTC.mapTimeZoneUTC.get(DateUtils.GLOBAL_GMT), TimeZoneUTC.UTC_0);
        String endTimeFinal =  DateUtils.convertDateStringBetweenTimeZone(DateUtils.formatDate(endTime.plusHours(DateUtils.GLOBAL_GMT)),
                TimeZoneUTC.mapTimeZoneUTC.get(DateUtils.GLOBAL_GMT), TimeZoneUTC.UTC_0);
        List<String> timeIngest = new ArrayList<>(Arrays.asList(startTimeFinal, endTimeFinal));
        ObjectMapper mapper = new ObjectMapper();
        FilterCondition condition = new FilterCondition();
        condition.setAttrCode("time_ingest");
        condition.setOperator(FilterOperator.IN_RANGE);
        Map<String, List<String>> data = new HashMap<>();
            data.put("value", timeIngest);
            try {
                condition.setData(mapper.writeValueAsString(data));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            conditions.add(condition);
        return conditions;
    }

    private PageImpl<MonitorDataSourceGeneralDTO> fetchMonitorDataSource(List<FilterCondition> conditions,Page<MonitorDataSourceGeneralDTO> monitorDataSourcePage, Pageable pageable) {
        String startTime = null;
        String endTime = null;
        Long numberRecord = 0L;
        String year = null;
        String month = null;
        String day = null;
        List<FilterCondition> timeConds = StreamUtils.filterThenToList(conditions, cond -> "time_ingest".equals(cond.getAttrCode()));
        try {
            FilterCondition mainTimeCond = timeConds.get(0);
            JSONObject timeRange = new JSONObject(mainTimeCond.getData());
            if (timeRange.has("value") && !timeRange.isNull("value")) {
                JSONArray valueArray = timeRange.getJSONArray("value");
//            startTime = valueArray.isNull(0) ? null : DateUtils.getLongFromDateTimeLocal(DateUtils.parse(valueArray.getString(0)).plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
//            endTime = valueArray.isNull(1) ? null : DateUtils.getLongFromDateTimeLocal(DateUtils.parse(valueArray.getString(1)).plusHours(DateUtils.GLOBAL_GMT)) * 1000000 + "";
                if (!valueArray.isNull(0)) {
                    LocalDateTime startDateTime = DateUtils.parse(valueArray.getString(0))
                            .plusHours(DateUtils.GLOBAL_GMT);
                    startTime = DateUtils.getLongFromDateTimeLocal(startDateTime) * 1000000 + "";

                    // 👉 Tách năm/tháng/ngày từ start
                    year = String.valueOf(startDateTime.getYear());
                    month = String.valueOf(startDateTime.getMonthValue());
                    day = String.valueOf(startDateTime.getDayOfMonth());
                }

                if (!valueArray.isNull(1)) {
                    LocalDateTime endDateTime = DateUtils.parse(valueArray.getString(1))
                            .plusHours(DateUtils.GLOBAL_GMT);
                    endTime = DateUtils.getLongFromDateTimeLocal(endDateTime) * 1000000 + "";
                }
                numberRecord = mediaReportCustomizeRepository.countNumberRecordInDay(startTime, endTime, year, month, day, null);

            }
        } catch (Exception e) {
          log.error(e.toString());
        }
        List<MonitorDataSourceGeneralDTO> monitorDataSource = new ArrayList<>(monitorDataSourcePage.getContent());
        try {
            if (monitorDataSource.isEmpty()) {
                MonitorDataSourceGeneralDTO dto = new MonitorDataSourceGeneralDTO();
                dto.setId(UUID.randomUUID().toString());
                dto.setDataSource("BEIDOU");
                dto.setStatus(null);
                dto.setLatestTimeIngest(LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
                dto.setIngestDate(new Date());
                dto.setSize(BigDecimal.ZERO);
                dto.setRowCount(0L);
                dto.setNumberRecord(0L);
                monitorDataSource.add(dto);
            }
        } catch (Exception e) {
            log.error("Error while creating default MonitorDataSourceGeneralDTO", e);
        }
        try {
            for (MonitorDataSourceGeneralDTO item : monitorDataSource) {
                if (numberRecord != null) {
                    item.setNumberRecord(numberRecord);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new PageImpl<>(monitorDataSource, pageable, monitorDataSourcePage.getTotalElements());
    }



}

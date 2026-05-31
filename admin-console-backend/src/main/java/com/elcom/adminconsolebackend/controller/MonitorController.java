package com.elcom.adminconsolebackend.controller;

import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.entity.management.MonitorServerEntity;
import com.elcom.adminconsolebackend.entity.management.WarningSettingEntity;
import com.elcom.adminconsolebackend.service.MonitorDataSourceService;
import com.elcom.adminconsolebackend.service.MonitorService;
import com.elcom.adminconsolebackend.service.WarningSettingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/monitors")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    private final WarningSettingService warningSettingService;

    private final MonitorDataSourceService monitorDataSourceService;

    @PostMapping
    public ResponseEntity<ResponseMessage> create(@RequestBody @Valid MonitorServerRequestDTO req) {
        MonitorServerEntity result = monitorService.save(req);
        if (result == null)
            return ResponseEntity.internalServerError().body(ResponseMessage.withDetails(HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "Created successful", result));
    }

    @PutMapping
    public ResponseEntity<ResponseMessage> update(@RequestBody @Valid MonitorServerRequestDTO req) {
        MonitorServerEntity result = monitorService.update(req);
        if (result == null)
            return ResponseEntity.internalServerError().body(ResponseMessage.withDetails(HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "updated successful", result));
    }

    @DeleteMapping
    public ResponseEntity<ResponseMessage> delete(@RequestBody ServerDeleteRequestDTO dto) {
        monitorService.delete(dto.getIps());
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "deleted successful"));
    }

    @PostMapping("/get-list-server")
    public ResponseEntity<ResponseMessage> getListMonitor(@RequestBody MonitorRequestDTO req) {
        return ResponseEntity.ok(ResponseMessage.success(monitorService.getList(req.getSearch())));
    }

    @GetMapping("/dashboard-monitor-active")
    public ResponseEntity<ResponseMessage> getMonitorActive() throws JsonProcessingException {
        return ResponseEntity.ok(ResponseMessage.success(monitorService.getMonitorActive()));
    }

    @GetMapping("/check-status-server/{ip}")
    public ResponseEntity<ResponseMessage> checkStatus(@PathVariable String ip) {
        int result = monitorService.checkStatus(ip);
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

//    @PostMapping("/get-alert-server")
//    public ResponseEntity<ResponseMessage> getAlertServerMonitor(@RequestBody AlertRequestDTO req, Pageable pageable) {
//        return ResponseEntity.ok(ResponseMessage.success(monitorService.getAlertServerMonitor(req,pageable)));
//    }
//
//    @PostMapping("/get-alert-service")
//    public ResponseEntity<ResponseMessage> getAlertServiceMonitor(@RequestBody AlertRequestDTO req, Pageable pageable) {
//        return ResponseEntity.ok(ResponseMessage.success(monitorService.getAlertServiceMonitor(req,pageable)));
//    }

    @GetMapping("/service-info/{serviceName}")
    public ResponseEntity<ResponseMessage> getServerInfo(@PathVariable String serviceName) {
        String result = monitorService.getServiceInfo(serviceName);
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @GetMapping("/list-name-service")
    public ResponseEntity<ResponseMessage> getNameServer() {
        List<String> result = monitorService.getNameServer();
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @PostMapping("/filter-warning")
    public ResponseEntity<ResponseMessage> filter(@RequestBody List<FilterCondition> conditions, Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.success(monitorService.filterWarning(conditions, pageable)));
    }

    @GetMapping("/list-resource")
    public ResponseEntity<ResponseMessage> getMonitorResource() {
        ResourceDTO result = monitorService.getMonitorResource();
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @GetMapping("/list-resource-type")
    public ResponseEntity<ResponseMessage> getMonitorResourceType() {
        List<String> result = monitorService.getMonitorResourceType();
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @PostMapping("/change-read/{id}")
    public ResponseEntity<ResponseMessage> changeRead(@PathVariable String id) {
        monitorService.changeRead(id);
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "change success", null));
    }

    @PostMapping("/change-read-all")
    public ResponseEntity<ResponseMessage> changeReadAll() {
        monitorService.changeReadAll();
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "change all success", null));
    }


    @PostMapping("/created-warning-setting")
    public ResponseEntity<ResponseMessage> createWarningSetting(@RequestBody @Valid WarningSettingRequestDTO req) {
        WarningSettingEntity result = warningSettingService.save(req);
        if (result == null)
            return ResponseEntity.internalServerError().body(ResponseMessage.withDetails(HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "Created successful", result));
    }

    @PutMapping("/updated-warning-setting")
    public ResponseEntity<ResponseMessage> updateWarningSetting(@RequestBody @Valid WarningSettingRequestDTO req) {
        WarningSettingEntity result = warningSettingService.update(req);
        if (result == null)
            return ResponseEntity.internalServerError().body(ResponseMessage.withDetails(HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        return ResponseEntity.ok().body(ResponseMessage.withDetails(HttpStatus.OK, "updated successful", result));
    }

    @GetMapping("/get-detail-warning-setting/{id}")
    public ResponseEntity<ResponseMessage> getDetailWarningSetting(@PathVariable String id) {
        WarningSettingEntity result = warningSettingService.getDetailWarningSetting(id);
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @PostMapping("/filter-warning-setting")
    public ResponseEntity<ResponseMessage> filterWarningSetting(@RequestBody List<FilterCondition> conditions, Pageable pageable) throws JsonProcessingException {
        return ResponseEntity.ok(ResponseMessage.success(warningSettingService.filterWarningSetting(conditions, pageable)));
    }

    @GetMapping("/get-all-warning-by-user")
    public ResponseEntity<ResponseMessage> getAllWarningByUser(@RequestParam String status) {
        List<WarningMessageGeneralDTO> result = monitorService.getAllWarningByUser(status);
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @GetMapping("/count-warning-by-user")
    public ResponseEntity<ResponseMessage> countWarningByUser(@RequestParam String status) {
        WarningMessageCountResponseDTO result = monitorService.countWarningByUser(status);
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @GetMapping("/count-all-unread-by-user")
    public ResponseEntity<ResponseMessage> countWarningUnreadByUser() {
        int result = monitorService.countWarningUnreadByUser();
        return ResponseEntity.ok(ResponseMessage.success(result));
    }

    @PostMapping("/filter-monitor-data-source")
    public ResponseEntity<ResponseMessage> filterMonitorDataSource(@RequestBody List<FilterCondition> conditions, Pageable pageable) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.filterMonitorDataSource(conditions, pageable)));
    }

    @PostMapping("/count-record-data-source")
    public ResponseEntity<ResponseMessage> countRecordDataSource(@RequestBody TimeFilterDTO timeFilterDTO) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.countRecordDataSource(timeFilterDTO)));
    }

    @PostMapping("/count-size-data-source")
    public ResponseEntity<ResponseMessage> countSizeDataSource(@RequestBody TimeFilterDTO timeFilterDTO) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.countSizeDataSource(timeFilterDTO)));
    }

    @PostMapping("/count-decode-level")
    public ResponseEntity<ResponseMessage> countDecodeLevel(@RequestBody TimeFilterDTO timeFilterDTO) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.countDecodeLevel(timeFilterDTO)));
    }

    @PostMapping("/count-station")
    public ResponseEntity<ResponseMessage> countStation(@RequestBody TimeFilterDTO timeFilterDTO) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.countStation(timeFilterDTO)));
    }

    @PostMapping("/count-record-by-date")
    public ResponseEntity<ResponseMessage> countRecordByDate(@RequestBody TimeFilterDTO timeFilterDTO) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.countRecordByDate(timeFilterDTO)));
    }
    @GetMapping("/get-status-data-source")
    public ResponseEntity<ResponseMessage> getStatusDataSource(@RequestParam String dataSource) {
        return ResponseEntity.ok(ResponseMessage.success(monitorDataSourceService.getStatusDataSource(dataSource)));
    }


//    @PostMapping("/ignore-warning")
//    public ResponseEntity<ResponseMessage> updateIgnore(@RequestParam(required = false) int checkReopens, @RequestBody IgnoreRequestDTO request) throws JsonProcessingException {
//        IgnoreRequestDTO result = monitorService.updateIgnore(request,checkReopens);
//        return ResponseEntity.ok(ResponseMessage.success(result));
//    }

}

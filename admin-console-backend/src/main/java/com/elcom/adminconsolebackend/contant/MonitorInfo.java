package com.elcom.adminconsolebackend.contant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonitorInfo {
    Redis("Redis","Redis"),
    Postgres("Postgres","Postgres"),
    Clickhouse("Clickhouse","Clickhouse"),
    Satprobe_BD_01("Satprobe-BD-01","192.168.6.43:8770"),
    Satprobe_BD_02("Satprobe-BD-02","192.168.6.46:8770"),
    Satprobe_BD_03("Satprobe-BD-03","192.168.6.47:8770"),
    Satprobe_VSAT_01("Satprobe-VSAT-01","192.168.13.13:8770"),
    Dispatcher_VSAT_01("Dispatcher-VSAT-01","192.168.6.50:8771"),
    Decoder_VSAT_01("Decoder-VSAT-01","192.168.6.50:8770"),
    gateway_service("gateway-service","192.168.10.69:8081"),
    sso_service("sso-service","192.168.10.69:8081"),
    abac_service("abac-service","192.168.10.69:8081"),
    discovery_service("discovery-service","192.168.10.69:5002"),
    metaasset_asset_service("metaasset-asset-service","192.168.10.69:8081"),
    metacen_position_service("metacen-position-service","192.168.10.69:8081"),
    metacen_media_service("metacen-media-service","192.168.10.69:8081"),
    metacen_rule_service("metacen-rule-service","192.168.10.69:8081"),
    metacen_asset_service("metacen-asset-service","192.168.10.69:8081"),
    metacen_event_service("metacen-event-service","192.168.10.69:8081"),
    metacen_task_service("metacen-task-service","192.168.10.69:8081"),
    upload_service("upload-service","192.168.10.69:8081"),
    metacen_socket_service("metacen-socket-service","192.168.10.69:9997"),
    metacen_process_media("metacen-process-media","192.168.10.69:8444"),
    metacen_event_process_service("metacen-event-process-service","192.168.10.69:8445"),
    metacen_data_processor_service("metacen-data-processor-service","192.168.10.69:8446"),
    metacen_data_collector_vsat("metacen-data-collector-vsat","192.168.10.69:8447"),
    metacen_jobs_service("metacen-jobs-service","192.168.10.69:8448"),
    adminconsole_backend("adminconsole-backend","192.168.10.69:6001");

    private final String description;
    private final String ip;
}

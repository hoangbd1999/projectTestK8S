package com.elcom.adminconsolebackend.service.impl;


import com.elcom.adminconsolebackend.config.MessageLanguage;
import com.elcom.adminconsolebackend.contant.ResourceType;
import com.elcom.adminconsolebackend.contant.WarningLevel;
import com.elcom.adminconsolebackend.dto.AlertResultDTO;
import com.elcom.adminconsolebackend.util.StringUtil;
import com.elcom.adminconsolebackend.util.StringUtils;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PrometheusService {

    ZonedDateTime serverTime = ZonedDateTime.now();
    ZoneOffset offset = serverTime.getOffset();

    private final MessageLanguage messageLanguage;

    @Value("${PROMETHEUS_URL}")
    private String PROMETHEUS_URL;
   // metrix linux
    public Double getCpuCoreLinux(String ip) {
        String query = "count(count(node_cpu_seconds_total{instance=\"" + ip + ":9100\",job=\"node_exporter\"}) by (cpu))";
        return fetchMetric(query);
    }

    public Double getMemoryTotalLinux(String ip) {
        String query = "node_memory_MemTotal_bytes{instance=\"" + ip + ":9100\",job=\"node_exporter\"}";
        return fetchMetric(query);
    }

    public Double getDiskSpaceLinux(String ip) {
        String query = "node_filesystem_size_bytes{instance=\"" + ip + ":9100\", job=\"node_exporter\"}";
        return fetchMetric(query);
    }

    // metrix windown

    public Double getCpuCoreWindows(String ip) {
        String query = "windows_cs_logical_processors{instance=\"" + ip + ":9182\",job=\"node_exporter\"}";
        return fetchMetric(query);
    }

    public Double getMemoryTotalWindows(String ip) {
        String query = "windows_cs_physical_memory_bytes{instance=\"" + ip + ":9182\",job=\"node_exporter\"}";
        return fetchMetric(query);
    }

    public Double getDiskSpaceWindows(String ip) {
        String query = "sum(windows_logical_disk_size_bytes{instance =\""+ ip + ":9182\", volume!=\"\"}) ";
        return fetchMetric(query);
    }


    // metrix linux
    public Double getCpuUsage(String ip) {
        String query = "100 * (1 - avg(rate(node_cpu_seconds_total{instance =\""+ ip + ":9100\", mode=\"idle\"}[5m]))) ";
        return fetchMetric(query);
    }

    public Double getMemoryUsage(String ip) {
        String query = "(1 - (node_memory_MemAvailable_bytes{instance = \""+ ip + ":9100\"} / node_memory_MemTotal_bytes{instance = \"" + ip + ":9100\"}) ) * 100";
        return fetchMetric(query);
    }

    public Double getDiskUsage(String ip) {
        String query = "max((1 - (node_filesystem_avail_bytes{fstype!~\"tmpfs|overlay\", instance =\""+ ip + ":9100\"} / node_filesystem_size_bytes{fstype!~\"tmpfs|overlay\", instance =\"" + ip + ":9100\"})) * 100)";
        return fetchMetric(query);
    }

    // metrix windown

    public Double getCpuUsageWindows(String ip) {
        String query = "100 * (1 - avg(rate((windows_cpu_time_total{instance =\""+ ip + ":9182\", mode=\"idle\"}[5m])))) ";
        return fetchMetric(query);
    }

    public Double getMemoryUsageWindows(String ip) {
        String query = "(1 - (windows_memory_available_bytes{instance = \""+ ip + ":9182\"} / windows_cs_physical_memory_bytes{instance = \"" + ip + ":9182\"}) ) * 100";
        return fetchMetric(query);
    }

    public Double getDiskUsageWindows(String ip) {
        String query = "( sum(windows_logical_disk_size_bytes{instance =\"" + ip + ":9182\"})- sum(windows_logical_disk_free_bytes{instance =\"" + ip + ":9182\"}))/sum(windows_logical_disk_size_bytes{instance =\"" + ip +":9182\"})  * 100";
        return fetchMetric(query);
    }

    public String getServiceActive() {
        String query = "available_state{application=~\"Satprobe-BD-01|Satprobe-BD-02|Satprobe-BD-03|Satprobe-VSAT-01|Decoder-VSAT-01|Dispatcher-VSAT-01\"}\n" +
                "OR\n" +
                "up{application!~\"Satprobe-BD-01|Satprobe-BD-02|Satprobe-BD-03|Satprobe-VSAT-01|Decoder-VSAT-01|Dispatcher-VSAT-01\",job =~ \"MetaInt Extended Services|Clickhouse|PostgreSQL|Kafka|Redis\"}";
        return fetchServiceActive(query);
    }


    public List<AlertResultDTO> getAlertServerNotify(String name, String ip) {
        String queryHistoryAlert = "ALERTS{alertstate=\"firing\"}[3m]";
        JsonNode resultNodeHistory = fetchAlertNotifyMetric(queryHistoryAlert);
        List<AlertResultDTO> alertResult = new ArrayList<>();
        try {
            if(resultNodeHistory != null && resultNodeHistory.isArray()) {
                for (JsonNode node : resultNodeHistory) {
                    JsonNode metricNode = node.path("metric");
                    String alertName = metricNode.path("alertname").asText();
                    String severity = metricNode.path("severity").asText();
                    String instance =  metricNode.path("instance").asText();
                    String application = metricNode.path("application").asText();
                    JsonNode valuesNode = node.path("values");
                    if(StringUtils.isNullOrEmpty(application) && instance.contains(ip)) {
                        if (valuesNode.isArray()) {
                            for (JsonNode valueNode : valuesNode) {
                                AlertResultDTO alert = new AlertResultDTO();
                                alert.setId(UUID.randomUUID().toString());
                                if (offset.equals(ZoneOffset.UTC)) {
                                    alert.setWarningTime(DateUtils.getDateFromLongTime(valueNode.get(0).asText()).plusHours(7));
                                } else {
                                    alert.setWarningTime(DateUtils.getDateFromLongTime(valueNode.get(0).asText()));
                                }
                                alert.setWarningLevel(severity);
                                alert.setInstance(instance);
                                alert.setApplication(name);
                                if(alertName.equalsIgnoreCase(WarningLevel.serverDown.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertServerDown",name));
                                    alert.setResourceType(ResourceType.Server.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.LowCPUUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowCpu",name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.WarningCPUUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningCpu",name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.HighCPUUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighCpu",name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.LowMemoryUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowRam",name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.WarningMemoryUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningRam",name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.HighMemoryUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighRam",name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.LowDiskUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowDisk",name));
                                    alert.setResourceType(ResourceType.Disk.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.WarningDiskUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningDisk",name));
                                    alert.setResourceType(ResourceType.Disk.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.HighDiskUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighDisk",name));
                                    alert.setResourceType(ResourceType.Disk.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.LowTemperature.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowTemperatureServer",name));
                                    alert.setResourceType(ResourceType.Temperature.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.WarningTemperature.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningTemperatureServer",name));
                                    alert.setResourceType(ResourceType.Temperature.name());
                                } else if(alertName.equalsIgnoreCase(WarningLevel.HighTemperature.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighTemperatureServer",name));
                                    alert.setResourceType(ResourceType.Temperature.name());
                                }

                                // Add the alert to the result list
                                alertResult.add(alert);
                            }

                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return alertResult;
    }

    public List<AlertResultDTO> getAlertServiceNotify(String name, String ip) {
        String queryHistoryAlert = "ALERTS{alertstate=\"firing\"}[3m]";
        JsonNode resultNodeHistory = fetchAlertNotifyMetric(queryHistoryAlert);
        List<AlertResultDTO> alertResult = new ArrayList<>();
        try {
            if (resultNodeHistory != null && resultNodeHistory.isArray()) {
                for (JsonNode node : resultNodeHistory) {
                    JsonNode metricNode = node.path("metric");
                    String alertName = metricNode.path("alertname").asText();
                    String severity = metricNode.path("severity").asText();
                    String instance = metricNode.path("instance").asText();
                    String application = metricNode.path("application").asText();
                    JsonNode valuesNode = node.path("values");
                    if ((!StringUtils.isNullOrEmpty(application) && application.contains(name)) && instance.contains(ip)) {
                        if (valuesNode.isArray()) {
                            for (JsonNode valueNode : valuesNode) {
                                AlertResultDTO alert = new AlertResultDTO();
                                alert.setId(UUID.randomUUID().toString());
                                if (offset.equals(ZoneOffset.UTC)) {
                                    alert.setWarningTime(DateUtils.getDateFromLongTime(valueNode.get(0).asText()).plusHours(7));
                                } else {
                                    alert.setWarningTime(DateUtils.getDateFromLongTime(valueNode.get(0).asText()));
                                }
                                alert.setWarningLevel(severity);
                                alert.setApplication(application);
                                if (alertName.equalsIgnoreCase(WarningLevel.LowMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowMemoryService", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningMemoryService", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.HighMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighMemoryService", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowCPUServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowCpuService", name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningCPUServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningCpuService", name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.HighCPUServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighCpuService", name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowHeapMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowHeadMemoryService", name));
                                    alert.setResourceType("Heap Memory");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningHeapMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningHeadMemoryService", name));
                                    alert.setResourceType("Heap Memory");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.HighHeapMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighHeadMemoryService", name));
                                    alert.setResourceType("Heap Memory");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowNonHeapMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowNonHeadMemoryService", name));
                                    alert.setResourceType("Non Heap Memory");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningNonHeapMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningNonHeadMemoryService", name));
                                    alert.setResourceType("Non Heap Memory");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.HighNonHeapMemoryServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighNonHeadMemoryService", name));
                                    alert.setResourceType("Non Heap Memory");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowDurationServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowDurationService", name));
                                    alert.setResourceType(ResourceType.Duration.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningDurationServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningDurationService", name));
                                    alert.setResourceType(ResourceType.Duration.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.HighDurationServiceUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertHighDurationService", name));
                                    alert.setResourceType(ResourceType.Duration.name());
                                }
                                alertResult.add(alert);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return alertResult;
    }

    public List<AlertResultDTO> getAlertDataBaseNotify(String name) {
        String queryHistoryAlert = "ALERTS{alertstate=\"firing\"}[3m]";
        JsonNode resultNodeHistory = fetchAlertNotifyMetric(queryHistoryAlert);
        List<AlertResultDTO> alertResult = new ArrayList<>();
        try {
            if (resultNodeHistory != null && resultNodeHistory.isArray()) {
                for (JsonNode node : resultNodeHistory) {
                    JsonNode metricNode = node.path("metric");
                    String alertName = metricNode.path("alertname").asText();
                    String severity = metricNode.path("severity").asText();
                    String instance = metricNode.path("instance").asText();
                    String application = metricNode.path("application").asText();
                    JsonNode valuesNode = node.path("values");
                    if ((!StringUtils.isNullOrEmpty(application) && application.contains(name))) {
                        if (valuesNode.isArray()) {
                            for (JsonNode valueNode : valuesNode) {
                                AlertResultDTO alert = new AlertResultDTO();
                                alert.setId(UUID.randomUUID().toString());
                                if (offset.equals(ZoneOffset.UTC)) {
                                    alert.setWarningTime(DateUtils.getDateFromLongTime(valueNode.get(0).asText()).plusHours(7));
                                } else {
                                    alert.setWarningTime(DateUtils.getDateFromLongTime(valueNode.get(0).asText()));
                                }
                                alert.setWarningLevel(severity);
                                alert.setApplication(application);
                                if (alertName.equalsIgnoreCase(WarningLevel.LowMemoryClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowMemoryClickHouse", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningMemoryClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningMemoryClickHouse", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalMemoryClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalMemoryClickHouse", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowConnectionTCPClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowConnectionTCPClickHouse", name));
                                    alert.setResourceType("Connection TCP");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningConnectionTCPClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningConnectionTCPClickHouse", name));
                                    alert.setResourceType("Connection TCP");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalConnectionTCPClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalConnectionTCPClickHouse", name));
                                    alert.setResourceType("Connection TCP");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowConnectionHTTPClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowConnectionHTTPClickHouse", name));
                                    alert.setResourceType("Connection HTTP");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningConnectionHTTPClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningConnectionHTTPClickHouse", name));
                                    alert.setResourceType("Connection HTTP");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalConnectionHTTPClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalConnectionHTTPClickHouse", name));
                                    alert.setResourceType("Connection HTTP");
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowQueryClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowQueryClickHouse", name));
                                    alert.setResourceType(ResourceType.Query.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningQueryClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningQueryClickHouse", name));
                                    alert.setResourceType(ResourceType.Query.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalQueryClickHouseUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalQueryClickHouse", name));
                                    alert.setResourceType(ResourceType.Query.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowCpuPostgresUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowCpuPostgres", name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningCpuPostgresUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningCpuPostgres", name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalCpuPostgresUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalCpuPostgres", name));
                                    alert.setResourceType(ResourceType.CPU.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowMemoryPostgresUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowMemoryPostgres", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningMemoryPostgresUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningMemoryPostgres", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalMemoryPostgresUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalMemoryPostgres", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.LowMemoryRedisUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertLowMemoryRedis", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.WarningMemoryRedisUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertWarningMemoryRedis", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                } else if (alertName.equalsIgnoreCase(WarningLevel.CriticalMemoryRedisUsage.name())) {
                                    alert.setDescription(messageLanguage.getMessage("alertCriticalMemoryRedis", name));
                                    alert.setResourceType(ResourceType.RAM.name());
                                }
                                alertResult.add(alert);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return alertResult;
    }

    private JsonNode fetchAlertNotifyMetric(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String queryUrl = PROMETHEUS_URL + "?query=" + encodedQuery;
            URL url = new URL(queryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(reader);
            reader.close();

            JsonNode valueNode = rootNode.path("data").path("result");
            if (valueNode.isArray() && valueNode.size() > 1) {
                return valueNode;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Double fetchMetric(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String queryUrl = PROMETHEUS_URL + "?query=" + encodedQuery;
            URL url = new URL(queryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(reader);
            reader.close();

            JsonNode resultNode = rootNode.path("data").path("result");
            if (resultNode.isArray() && resultNode.size() > 0) {
                JsonNode valueNode = resultNode.get(0).path("value");
                if (valueNode.isArray() && valueNode.size() > 1) {
                    //    String timestamp = valueNode.get(0).asText();
                    String value = valueNode.get(1).asText();
                    if (!StringUtil.isNullOrEmpty(value)) {
                        return Double.parseDouble(value);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private String fetchServiceActive(String query) {
        int upCount = 0;
        int total = 0;
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String queryUrl = PROMETHEUS_URL + "?query=" + encodedQuery;
            URL url = new URL(queryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(reader);
            reader.close();

            JsonNode resultNode = rootNode.path("data").path("result");
            total = resultNode.size();
            if (resultNode.isArray()) {
                for (JsonNode node : resultNode) {
                    JsonNode valueNode = node.path("value");
                    if (valueNode.isArray() && valueNode.size() > 1) {
                        String value = valueNode.get(1).asText();
                        if ("1".equals(value)) {
                            upCount++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%d/%d", upCount, total);
    }

}

package com.elcom.adminconsolebackend.service.impl;



import com.elcom.adminconsolebackend.config.MessageLanguage;
import com.elcom.adminconsolebackend.contant.Constant;
import com.elcom.adminconsolebackend.contant.MonitorInfo;
import com.elcom.adminconsolebackend.contant.ResourceType;
import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.entity.management.*;
import com.elcom.adminconsolebackend.exception.ResourceExistException;
import com.elcom.adminconsolebackend.http.client.SocketClient;
import com.elcom.adminconsolebackend.repository.management.*;
import com.elcom.adminconsolebackend.service.MonitorService;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import com.elcom.adminconsolebackend.util.StringUtils;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final MessageLanguage messageLanguage;

    private final MonitorRepository monitorRepository;

    private final PrometheusService prometheusService;

    private final ServiceInfoRepository serviceInfoRepository;

    private final WarningMessageRepository warningMessageRepository;

    private final WarningMessageFilterRepository warningMessageFilterRepository;

    private final MonitorRepositoryCustomize monitorRepositoryCustomize;

    private final WarningSettingRepository warningSettingRepository;

    private final SocketClient socketClient;

    @Value("${Service_Monitor}")
    private String Service_Monitor;

    @Value("${Server_Monitor}")
    private String Server_Monitor;

    @Autowired
    private final RedisTemplate redisTemplate;

    private final Cache<String, Boolean> alertDescriptionCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(100000)
            .build();

    @Override
    public Page<WarningMessageGeneralDTO> filterWarning(List<FilterCondition> conditions, Pageable pageable) {
        return warningMessageFilterRepository.filterWarning(conditions, pageable, SecurityUtils.getCurrentUserId());
//        Page<WarningMessageEntity> elementsOnPages = warningMessageFilterRepository.filterWarning(conditions, pageable);
//        Page<WarningMessageGeneralDTO> resultWarnings;
//        List<String> viewedWarningIds = warningMessageRepository.findViewedWarningOfUser(SecurityUtils.getCurrentUserId());
//        resultWarnings = elementsOnPages.map(noti -> {
//            WarningMessageGeneralDTO warning = modelMapper.map(noti, WarningMessageGeneralDTO.class);
//            if(viewedWarningIds.contains(noti.getId())) {
//                warning.setStatus(1);
//            } else {
//                warning.setStatus(0);
//            }
//            return warning;
//        });
//        return resultWarnings;
    }

    @Override
    public MonitorServerEntity save(MonitorServerRequestDTO req) {
        MonitorServerEntity entity = modelMapper.map(req, MonitorServerEntity.class);
        MonitorServerEntity checkIp = monitorRepository.findByIp(req.getIp());
        if (checkIp != null) {
            throw new ResourceExistException(messageLanguage.getMessage("ipExist"));
        }
        entity.setId(UUID.randomUUID().toString());
        entity.setCreatedAt(DateUtils.convertToLocalDateTime(new Date()));
        entity.setCreatedBy(SecurityUtils.getCurrentUserName());
        monitorRepository.save(entity);
        return entity;
    }

    @Override
    public MonitorServerEntity update(MonitorServerRequestDTO req) {
        MonitorServerEntity entity = monitorRepository.findByIp(req.getIp());
        if(entity == null) {
            throw new ResourceExistException(messageLanguage.getMessage("ipNotExist"));
        }
        entity.setName(req.getName());
        entity.setDomain(req.getDomain());
        if(!StringUtils.isNullOrEmpty(req.getDescription())) {
            entity.setDescription(req.getDescription());
        }
        monitorRepository.save(entity);
        return entity;
    }

    @Override
    public void delete(List<String> ips) {
        try {
        monitorRepository.deleteServer(ips);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<MonitorServerResponseDTO> getList(String search) {
        List<MonitorServerEntity> listServer = monitorRepository.findAllBySearch(search);
        listServer.stream().map(server -> {
            String ip = server.getIp();
            String osType = server.getOsType();

            if ("linux".equalsIgnoreCase(osType)) {
                server.setCpu(prometheusService.getCpuCoreLinux(ip).toString());
                server.setRam(prometheusService.getMemoryTotalLinux(ip).toString());
                server.setDisk(prometheusService.getDiskSpaceLinux(ip).toString());
            } else if ("windown".equalsIgnoreCase(osType)) {
                server.setCpu(prometheusService.getCpuCoreWindows(ip).toString());
                server.setRam(prometheusService.getMemoryTotalWindows(ip).toString());
                server.setDisk(prometheusService.getDiskSpaceWindows(ip).toString());
            }
            server.setStatus(checkStatus(ip));
            return server;
        }).collect(Collectors.toList());

        List<MonitorServerResponseDTO> results = listServer.stream()
                .map(server -> modelMapper.map(server, MonitorServerResponseDTO.class))
                .collect(Collectors.toList());

        results.stream().map(serverResult -> {
            String ip = serverResult.getIp();
            String osType = serverResult.getOsType();
            if ("linux".equalsIgnoreCase(osType)) {
                serverResult.setCpuUsage(prometheusService.getCpuUsage(ip).toString());
                serverResult.setRamUsage(prometheusService.getMemoryUsage(ip).toString());
                serverResult.setDiskUsage(prometheusService.getDiskUsage(ip).toString());
            } else if ("windows".equalsIgnoreCase(osType)) {
                serverResult.setCpuUsage(prometheusService.getCpuUsageWindows(ip).toString());
                serverResult.setRamUsage(prometheusService.getMemoryUsageWindows(ip).toString());
                serverResult.setDiskUsage(prometheusService.getDiskUsageWindows(ip).toString());

            }
            return serverResult;
        }).collect(Collectors.toList());
        return results;
    }

    @Override
    public MonitorActiveResponseDTO getMonitorActive() throws JsonProcessingException {

        List<MonitorServerEntity> listServer = monitorRepository.findAll();
        int total = listServer.size();
        int upCount = 0;
        for (MonitorServerEntity item : listServer) {
            int up  = checkStatus(item.getIp());
            if (1 == up) {
                upCount++;
            }
        }

        MonitorActiveResponseDTO result = new MonitorActiveResponseDTO();
        List<SourceRequestDTO> dataSource = objectMapper.readValue(objectMapper
                .writeValueAsString(redisTemplate.opsForHash().values(Constant.REDIS_DATA_SOURCES_INFO)), new TypeReference<>() {});
        for (SourceRequestDTO item : dataSource) {
            if(item.getId().equalsIgnoreCase("BEIDOU") && item.getActiveStatus()) {
                result.setDataSourceActive("1/1");
            }
        }
        result.setServiceActive(prometheusService.getServiceActive());
        result.setServerActive(String.format("%d/%d", upCount, total));
        return result;
    }

//    @Override
//    public Page<AlertResultDTO> getAlertServerMonitor(AlertRequestDTO req, Pageable pageable) {
//        List<AlertResultDTO> result = prometheusService.getAlertServerNotify(req.getName(),req.getIp());
//        int total = result.size();
//        List<AlertResultDTO> content = getPage(result, pageable.getPageNumber() + 1, pageable.getPageSize());
//
//        return new PageImpl<>(content, pageable, total);
//    }

//    @Override
//    public Page<AlertResultDTO> getAlertServiceMonitor(AlertRequestDTO req, Pageable pageable) {
//        List<AlertResultDTO> result = prometheusService.getAlertServiceNotify(req.getName(),req.getIp());
//        int total = result.size();
//        List<AlertResultDTO> content = getPage(result, pageable.getPageNumber() + 1, pageable.getPageSize());
//
//        return new PageImpl<>(content, pageable, total);
//    }

    public static List<AlertResultDTO> getPage(List<AlertResultDTO> pages, int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1) {
            throw new IllegalArgumentException("pageNumber and pageSize must be greater than 0");
        }
        return pages.stream()
                .skip((long) (pageNumber - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }


    @Override
    public int checkStatus(String ip) {
        try {
            // Lệnh ping tùy theo hệ điều hành (Windows dùng "-n", Linux/Mac dùng "-c")
            String pingCommand = System.getProperty("os.name").toLowerCase().contains("win")
                    ? "ping -n 1 -w 1000 " + ip
                    : "ping -c 1 -W 1 " + ip;

            Process process = Runtime.getRuntime().exec(pingCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // Kiểm tra kết quả: Nếu có "TTL=" thì IP đang hoạt động
                if (line.toLowerCase().contains("ttl=")) {
                    return 1;
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String getServiceInfo(String serverName) {
        ServiceInfoEntity serviceInfoEntity = serviceInfoRepository.findByServiceName(serverName);
        return serviceInfoEntity.getInstance();
    }

    @Override
    public List<String> getNameServer() {
        List<ServiceInfoEntity> serviceInfoEntity = serviceInfoRepository.findAllByType(1);
        List<String> serviceNames = new ArrayList<>();
        serviceInfoEntity.forEach(obj -> serviceNames.add(obj.getServiceName()));
        return serviceNames;
    }

    @Override
    public ResourceDTO getMonitorResource() {
        ResourceDTO result = new ResourceDTO();
        List<String> resourcesServer = monitorRepository.findAllNameServer();
        result.setServer(resourcesServer);

        List<String> resourcesApplicationService = monitorRepository.findAllNameApplicationService();
        result.setApplicationService(resourcesApplicationService);

        List<String> resourcesPlatformService = monitorRepository.findAllNamePlatformService();
        result.setPlatformService(resourcesPlatformService);

        return result;
    }

    @Override
    public List<String> getMonitorResourceType() {
        List<String> resources = Arrays.stream(ResourceType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        resources.add("Heap Memory");
        resources.add("Non Heap Memory");
        resources.add("Connection TCP");
        resources.add("Connection HTTP");
        return resources;
    }

    @Override
    public void changeRead(String id) {
        try {
            Optional<WarningMessageEntity> message = warningMessageRepository.findById(id);
            if (message.isPresent()) {
                warningMessageRepository.insertWarningForUser(SecurityUtils.getCurrentUserId(), id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void changeReadAll() {
        List<WarningMessageCountDTO> warningUnread = monitorRepositoryCustomize.getWarningUnread(SecurityUtils.getCurrentUserId());
        List<WarningMessageReadEntity> entities = new ArrayList<>();
        for (WarningMessageCountDTO item : warningUnread) {
            WarningMessageReadEntity warningMessageReadEntity = new WarningMessageReadEntity();
            warningMessageReadEntity.setUserId(SecurityUtils.getCurrentUserId());
            warningMessageReadEntity.setWarningId(item.getId());
            entities.add(warningMessageReadEntity);
        }
        monitorRepositoryCustomize.insertWarningsBatch(entities);
    }


    @Override
    public void saveWarningMonitor() {
        List<AlertResultDTO> resultTotal = new ArrayList<>();
        // Monitor Servers
        monitorRepository.findAll().forEach(server ->
                resultTotal.addAll(prometheusService.getAlertServerNotify(server.getName(), server.getIp()))
        );

        // Monitor Databases
        Arrays.asList(MonitorInfo.Clickhouse, MonitorInfo.Postgres, MonitorInfo.Redis)
                .forEach(db ->
                        resultTotal.addAll(prometheusService.getAlertDataBaseNotify(db.name()))
                );

        // Monitor Services
        Arrays.asList(
                MonitorInfo.gateway_service,
                MonitorInfo.sso_service,
                MonitorInfo.abac_service,
                MonitorInfo.discovery_service,
                MonitorInfo.metaasset_asset_service,
                MonitorInfo.metacen_position_service,
                MonitorInfo.metacen_media_service,
                MonitorInfo.metacen_rule_service,
                MonitorInfo.metacen_asset_service,
                MonitorInfo.metacen_event_service,
                MonitorInfo.metacen_task_service,
                MonitorInfo.upload_service,
                MonitorInfo.metacen_socket_service,
                MonitorInfo.metacen_process_media,
                MonitorInfo.metacen_event_process_service,
                MonitorInfo.metacen_data_processor_service,
                MonitorInfo.metacen_data_collector_vsat,
                MonitorInfo.metacen_jobs_service,
                MonitorInfo.adminconsole_backend,
                MonitorInfo.Satprobe_BD_01,
                MonitorInfo.Satprobe_BD_02,
                MonitorInfo.Satprobe_BD_03,
                MonitorInfo.Satprobe_VSAT_01,
                MonitorInfo.Dispatcher_VSAT_01,
                MonitorInfo.Decoder_VSAT_01
        ).forEach(service ->
                resultTotal.addAll(prometheusService.getAlertServiceNotify(service.getDescription(), service.getIp()))
        );

        List<WarningMessageEntity> resultWarning = resultTotal.stream()
                .map(dto -> {
                    WarningMessageEntity entity = new WarningMessageEntity();
                    entity.setId(dto.getId());
                    entity.setApplication(dto.getApplication());
                    entity.setDescription(dto.getDescription());
                    entity.setWarningTime(dto.getWarningTime());
                    entity.setInstance(dto.getInstance());
                    entity.setResourceType(dto.getResourceType());
                    int level = switch (dto.getWarningLevel().toLowerCase()) {
                        case "low" -> 1;
                        case "warning" -> 2;
                        case "critical" -> 3;
                        default -> 0;
                    };
                    entity.setWarningLevel(level);
                    return entity;
                })
                .collect(Collectors.toList());

        warningMessageRepository.saveAll(resultWarning);
        System.out.println("insert warning success");
        // send quả chuông
        socketClient.sendMonitorWarningMessage(resultWarning);

        // send email
      //  sendWarningEmail(resultTotal);
    }

    public void sendWarningEmail(List<AlertResultDTO> resultWarning){
        try {
            List<AlertResultDTO> result = new ArrayList<>();
            WarningSettingEntity warningSettingEntity = warningSettingRepository.findByWarningChannel("Email");
            if (warningSettingEntity != null && warningSettingEntity.getStatus() == 0 &&
                    warningSettingEntity.getResourceTypes() != null &&
                    warningSettingEntity.getWarningLevels() != null) {

                Comparator<AlertResultDTO> distinctComparator = Comparator.comparing(i ->
                        (i.getWarningTime() != null ? i.getWarningTime() : "") + "|" +
                                (i.getDescription() != null ? i.getDescription().toLowerCase() : "") + "|" +
                                (i.getResourceType() != null ? i.getResourceType().toLowerCase() : "")
                );
                for (AlertResultDTO item : resultWarning) {
                    int level = switch (item.getWarningLevel().toLowerCase()) {
                        case "low" -> 1;
                        case "warning" -> 2;
                        case "critical" -> 3;
                        default -> 0;
                    };
                    item.setWarningLevel(String.valueOf(level));
                }

                result = resultWarning.stream()
                        .filter(i ->
                                warningSettingEntity.getResourceTypes().stream()
                                        .anyMatch(r -> r.equalsIgnoreCase(i.getResourceType())) &&
                                        warningSettingEntity.getWarningLevels().stream()
                                                .anyMatch(lvl -> lvl.equalsIgnoreCase(i.getWarningLevel()))
                        )
                        .filter(i -> {
                            String key = i.getDescription() != null ? i.getDescription().toLowerCase() : "";
                            if (alertDescriptionCache.getIfPresent(key) != null) {
                                System.out.println("đã bắn cache: " + key);
                                return false; // đã bắn trong 30 phút
                            }
                            alertDescriptionCache.put(key, true); // cache lại description
                            return true;
                        })
                        .collect(Collectors.collectingAndThen(
                                Collectors.toCollection(() -> new TreeSet<>(distinctComparator)),
                                ArrayList::new
                        ));
            }
            String emailSend = warningSettingEntity.getEmailSend();
            String passEmailSend = warningSettingEntity.getPasswordEmailSend();
            List<String> emailReceive = warningSettingEntity.getWarningObjects();

            sendEmail(result,emailSend,passEmailSend,emailReceive);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }


    public void sendEmail(List<AlertResultDTO> result, String emailSend, String passEmailSend, List<String> emailReceive) {
        List<String> serviceNames = new ArrayList<>();
        List<String> serverNames = new ArrayList<>();

        List<ServiceInfoEntity> serviceInfoEntities = serviceInfoRepository.findAll();
        serviceInfoEntities.forEach(obj -> serviceNames.add(obj.getServiceName()));

        List<MonitorServerEntity> monitorServerEntities = monitorRepository.findAll();
        monitorServerEntities.forEach(obj -> serverNames.add(obj.getName()));



        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailSend, passEmailSend);
                    }
                });

        for (AlertResultDTO alert : result) {
            try {
                String level = switch (alert.getWarningLevel()) {
                    case "1" -> "Thấp";
                    case "2" -> "Trung bình";
                    case "3" -> "Cao";
                    default -> "Unknown";
                };
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailSend));

                // Thêm người nhận
                InternetAddress[] recipientAddresses = emailReceive.stream()
                        .map(email -> {
                            try {
                                return new InternetAddress(email);
                            } catch (AddressException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toArray(InternetAddress[]::new);

                message.setRecipients(Message.RecipientType.TO, recipientAddresses);
                message.setSubject("Cảnh báo hệ thống - MetaINT");

                // Nội dung từng cảnh báo
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("<div style='font-family: Arial, sans-serif; font-size: 14px;'>");
                contentBuilder.append("<strong style='font-size: 16px;'>Cảnh báo hệ thống - MetaINT</strong><br><br>");
                contentBuilder.append("Mức độ cảnh báo: <strong>").append(level).append("</strong><br>");
                contentBuilder.append("Nội dung cảnh báo: <strong>").append(alert.getDescription()).append("</strong><br>");
                String warningTime = alert.getWarningTime().format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT));
                contentBuilder.append("Thời gian cảnh báo: <strong>").append(warningTime).append("</strong><br><br>");

                if(serviceNames.contains(alert.getApplication())) {
                    String link = Service_Monitor + alert.getApplication();
                    contentBuilder.append("<a href='").append(link).append("'>").append("Chi tiết").append("</a><br>");
                } else if(serverNames.contains(alert.getApplication())) {
                    monitorServerEntities.stream()
                            .filter(m -> alert.getApplication().equals(m.getName()))
                            .findFirst()
                            .ifPresent(monitor -> {
                                String link = Server_Monitor + monitor.getIp() + "?data=" + monitor.getDomain();
                                contentBuilder.append("<a href='").append(link).append("'>").append("Chi tiết").append("</a><br>");
                            });
                }
                message.setContent(contentBuilder.toString(), "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("Đã gửi cảnh báo: " + alert.getDescription());

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int countWarningUnreadByUser() {
        try {
            int count = warningMessageRepository.countWarningUnreadByUser(SecurityUtils.getCurrentUserId());
            if(count >= 100) {
                count = 100;
            }
            return count;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public WarningMessageCountResponseDTO countWarningByUser(String status) {
        WarningMessageCountResponseDTO warningMessageCountResponseDTO = new WarningMessageCountResponseDTO();
        int count = warningMessageRepository.countWarningUnreadByUser(SecurityUtils.getCurrentUserId());
        if(count >= 100) {
            count = 100;
        }
        WarningMessageCountDTO warningMessageCountDTO = monitorRepositoryCustomize.countWarningByUser(status,SecurityUtils.getCurrentUserId());

        if(warningMessageCountDTO != null && !status.equalsIgnoreCase("1")) {
            warningMessageCountDTO.setCountUnread(count);
        }
        warningMessageCountResponseDTO.setSystemWarning(warningMessageCountDTO);
        return warningMessageCountResponseDTO;
    }

    @Override
    public List<WarningMessageGeneralDTO> getAllWarningByUser(String status) {
        List<WarningMessageGeneralDTO> resultWarnings = monitorRepositoryCustomize.getAllWarningByUser(status, SecurityUtils.getCurrentUserId());
        List<String> viewedWarningIds =
                warningMessageRepository.findViewedWarningOfUser(SecurityUtils.getCurrentUserId());

        for (WarningMessageGeneralDTO warning : resultWarnings) {
            if (viewedWarningIds.contains(warning.getId())) {
                warning.setStatus(1);
            } else {
                warning.setStatus(0);
            }
        }
        return resultWarnings;
    }

//    @Override
//    public IgnoreRequestDTO updateIgnore(IgnoreRequestDTO req, int checkReopens) throws JsonProcessingException {
//        if(checkReopens == 0) {
//            String json = new ObjectMapper().writeValueAsString(req);
//            Object result = new ObjectMapper().readValue(json, Object.class);
//            //  redisTemplate.opsForHash().put(REDIS_DATA_SOURCES_INFO, req.getId(), a);
//
//            redisTemplate.opsForHash().put(Constant.REDIS_KEY_IGNORE_WARNING, req.getKey(), result);
//            return req;
//        } else {
//            redisTemplate.opsForHash().delete(Constant.REDIS_KEY_IGNORE_WARNING, req.getKey());
//            return req;
//        }
//    }


}

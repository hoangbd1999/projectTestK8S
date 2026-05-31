package com.elcom.adminconsolebackend.repository.management;


import com.elcom.adminconsolebackend.dto.WarningMessageCountDTO;
import com.elcom.adminconsolebackend.dto.WarningMessageGeneralDTO;
import com.elcom.adminconsolebackend.entity.management.WarningMessageReadEntity;
import com.elcom.adminconsolebackend.util.StreamUtils;
import jakarta.persistence.*;

import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;


@Repository
@Slf4j
public class MonitorRepositoryCustomize {

    @PersistenceContext
    private EntityManager entityManager;

//    public List<MonitorServerResponseDTO> findAllBySearch(String search) {
//        ObjectMapper mapper = new ObjectMapper();
//        String queryStr = " SELECT m.id as id, m.name as name, m.ip as ip, m.cpu as cpu, " +
//                " m.ram as ram, m.disk as disk, " +
//                " m.description as description, m.created_at as created_at," +
//                " m.created_by as created_by, m.status as status " +
//                " FROM admin_console.monitor_server m where 1=1 ";
//
//        if (StringUtils.hasText(search)) {
//            queryStr = queryStr + " AND name ILIKE '%' || COALESCE('"+ search +"', '') || '%' ";
//        }
//
//        Query compositeQuery = entityManager.createNativeQuery(queryStr, Tuple.class);
//        List<Tuple> result = compositeQuery.getResultList();
//
//
//        return StreamUtils.toList(result, row -> {
//            MonitorServerResponseDTO monitorServerDTO = new MonitorServerResponseDTO();
//            monitorServerDTO.setId(row.get("id", String.class));
//            monitorServerDTO.setName(row.get("name", String.class));
//            monitorServerDTO.setIp(row.get("ip", String.class));
//            monitorServerDTO.setCpu(row.get("cpu", String.class));
//            monitorServerDTO.setRam(row.get("ram", String.class));
//            monitorServerDTO.setDisk(row.get("disk", String.class));
//            monitorServerDTO.setDescription(row.get("description", String.class));
//            monitorServerDTO.setStatus(row.get("status", Short.class));
//
//            Timestamp createAt = row.get("created_at", Timestamp.class);
//            monitorServerDTO.setCreatedAt(createAt != null ? createAt.toLocalDateTime() : null);
//
//            monitorServerDTO.setCreatedBy(row.get("created_by", String.class));
//
//            return monitorServerDTO;
//        });
//    }

    public Float countSizeDataSource(String startTime, String endTime) {
        try {
            String queryStr = " SELECT " +
                    " SUM(size::BIGINT) AS total_size " +
                    " FROM admin_console.monitor_data_source " +
                    " WHERE time_ingest >= '" + startTime + "' and time_ingest <= '" + endTime + "' GROUP BY data_source ";

            Query query = entityManager.createNativeQuery(queryStr);
            Object result = query.getSingleResult();
            if (result != null) {
                return ((Number) result).floatValue();
            } else {
                return 0f;
            }
        } catch (NoResultException ex) {
            return null;
        }
    }


    public List<WarningMessageCountDTO> getWarningUnread(String userId) {
        try {
            String queryStr = " select wm.id, wm.description, wm.warning_time " +
                    "from admin_console.warning_message_test wm " +
                    "inner join " +
                    "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                    "where u.user_id = '" + userId + "' " +
                    "and u.warning_id not in (select r.warning_id from admin_console.warning_message_read r " +
                    "where r.user_id = '" + userId + "') ";

            Query compositeQuery = entityManager.createNativeQuery(queryStr, Tuple.class);
            List<Tuple> result = compositeQuery.getResultList();

            return StreamUtils.toList(result, row -> {
                WarningMessageCountDTO dto = new WarningMessageCountDTO();
                dto.setId(row.get("id", String.class));
                return dto;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public WarningMessageCountDTO countWarningByUser(String status, String userId) {
        try {
            String queryStr = "";
            if (status.equalsIgnoreCase("All")) {
                queryStr = " select wm.id, wm.description, wm.warning_time " +
                        "from admin_console.warning_message_test wm " +
                        "inner join " +
                        "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                        "where u.user_id = '" + userId + "'" +
                        "order by wm.warning_time desc limit 1";

            } else if (status.equalsIgnoreCase("1")) {
                queryStr = " select wm.id, wm.description, wm.warning_time " +
                        "from admin_console.warning_message_test wm " +
                        "inner join  " +
                        "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                        "where u.user_id = '" + userId + "' " +
                        "and u.warning_id in (select r.warning_id from admin_console.warning_message_read r " +
                        "where r.user_id = '" + userId + "') " +
                        "order by wm.warning_time desc limit 1";
            } else if (status.equalsIgnoreCase("0")) {
                queryStr = " select wm.id, wm.description, wm.warning_time " +
                        "from admin_console.warning_message_test wm " +
                        "inner join " +
                        "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                        "where u.user_id = '" + userId + "' " +
                        "and u.warning_id not in (select r.warning_id from admin_console.warning_message_read r " +
                        "where r.user_id = '" + userId + "') " +
                        "order by wm.warning_time desc limit 1";
            }

            Query compositeQuery = entityManager.createNativeQuery(queryStr, Tuple.class);
            List<Tuple> rows = compositeQuery.getResultList();
            if (rows.isEmpty()) {
                return null;
            }
            Tuple row = rows.get(0);

            WarningMessageCountDTO dto = new WarningMessageCountDTO();
            dto.setId(row.get("id", String.class));
            dto.setDescription(row.get("description", String.class));
            Timestamp createAt = row.get("warning_time", Timestamp.class);
            dto.setWarningTime(createAt != null ? createAt.toLocalDateTime() : null);

            return dto;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<WarningMessageGeneralDTO> getAllWarningByUser(String status, String userId) {
        try {
            String queryStr = "";
            if (status.equalsIgnoreCase("All")) {
                queryStr = " select wm.id, wm.description, wm.application, wm.instance, wm.resource_type, wm.warning_time, wm.warning_level " +
                        "from admin_console.warning_message_test wm " +
                        "inner join " +
                        "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                        "where u.user_id = '" + userId + "'" +
                        "order by wm.warning_time desc ";

            } else if (status.equalsIgnoreCase("1")) {
                queryStr = " select wm.id, wm.description, wm.application,wm.instance , wm.resource_type, wm.warning_time, wm.warning_level " +
                        "from admin_console.warning_message_test wm " +
                        "inner join  " +
                        "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                        "where u.user_id = '" + userId + "' " +
                        "and u.warning_id in (select r.warning_id from admin_console.warning_message_read r " +
                        "where r.user_id = '" + userId + "') " +
                        "order by wm.warning_time desc ";
            } else if (status.equalsIgnoreCase("0")) {
                queryStr = " select wm.id, wm.description, wm.application, wm.instance, wm.resource_type, wm.warning_time, wm.warning_level " +
                        "from admin_console.warning_message_test wm " +
                        "inner join " +
                        "admin_console.warning_message_user u on (wm.id = u.warning_id) " +
                        "where u.user_id = '" + userId + "' " +
                        "and u.warning_id not in (select r.warning_id from admin_console.warning_message_read r " +
                        "where r.user_id = '" + userId + "') " +
                        "order by wm.warning_time desc ";
            }

            Query compositeQuery = entityManager.createNativeQuery(queryStr, Tuple.class);
            List<Tuple> result = compositeQuery.getResultList();

            return StreamUtils.toList(result, row -> {
                WarningMessageGeneralDTO dto = new WarningMessageGeneralDTO();
                dto.setId(row.get("id", String.class));
                dto.setDescription(row.get("description", String.class));
                dto.setApplication(row.get("application", String.class));
                dto.setInstance(row.get("instance", String.class));
                dto.setResourceType(row.get("resource_type", String.class));
                dto.setWarningLevel(row.get("warning_level", Short.class));
                Timestamp createAt = row.get("warning_time", Timestamp.class);
                dto.setWarningTime(createAt != null ? createAt.toLocalDateTime() : null);

                return dto;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional
    public void insertWarningsBatch(List<WarningMessageReadEntity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));
            if (i % 50 == 0) { // flush mỗi 50 bản ghi
                entityManager.flush();
                entityManager.clear();
            }
        }
    }


}
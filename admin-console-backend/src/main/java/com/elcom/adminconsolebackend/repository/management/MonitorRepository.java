package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.MonitorServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface MonitorRepository extends JpaRepository<MonitorServerEntity, String> {

    @Query(nativeQuery = true, value = """
    SELECT * FROM admin_console.monitor_server 
    WHERE :search IS NULL OR name ILIKE '%' || COALESCE(:search, '') || '%'
""")
    List<MonitorServerEntity> findAllBySearch(@Param("search") String search);

    MonitorServerEntity findByIp(String ip);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM admin_console.monitor_server WHERE ip IN (:ips) ", nativeQuery = true)
    void deleteServer(@Param("ips") List<String> ips);

    @Query(nativeQuery = true, value = " SELECT name FROM admin_console.monitor_server")
    List<String> findAllNameServer();

    @Query(nativeQuery = true, value = "SELECT service_name AS name FROM admin_console.service_info where type != 2 ")
    List<String> findAllNameApplicationService();

    @Query(nativeQuery = true, value = "SELECT service_name AS name FROM admin_console.service_info where type = 2 ")
    List<String> findAllNamePlatformService();

}

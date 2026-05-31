package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.report.ResourceReport;
import com.elcom.adminconsolebackend.dto.report.UserReport;
import com.elcom.adminconsolebackend.entity.management.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HistoryRepository extends JpaRepository<History, String> {
    @Query(nativeQuery = true, value = "select resource ,app_name ,value, count(*) as count from admin_console.history group by resource,app_name,value  order by count desc limit 10")
    List<ResourceReport> getResourceReport();

    @Query(nativeQuery = true, value = "select username , count(*) as count from admin_console.history group by username  order by count desc limit 10")
    List<UserReport> getUserReport();
}

package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.MonitorDataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MonitorDataSourceRepository extends JpaRepository<MonitorDataSourceEntity, String> {

    MonitorDataSourceEntity findFirstByDataSource(String dataSource);

}

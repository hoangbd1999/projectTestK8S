package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.ServiceInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ServiceInfoRepository extends JpaRepository<ServiceInfoEntity, String> {

    ServiceInfoEntity findByServiceName(String serviceName);

    List<ServiceInfoEntity> findAllByType(int type);

}

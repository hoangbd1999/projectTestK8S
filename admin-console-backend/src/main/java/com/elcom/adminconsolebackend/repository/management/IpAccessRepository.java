package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.IpAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpAccessRepository extends JpaRepository<IpAccessEntity, Long> {

    IpAccessEntity findByIpAddress(String ipAddress);

}

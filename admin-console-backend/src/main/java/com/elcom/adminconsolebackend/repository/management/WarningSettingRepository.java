package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.WarningSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;



@Repository
public interface WarningSettingRepository extends JpaRepository<WarningSettingEntity, String> {

    WarningSettingEntity findByWarningChannel(String warningChannel);

}

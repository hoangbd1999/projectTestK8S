package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.WarningMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface WarningMessageRepository extends JpaRepository<WarningMessageEntity, String> {

    @Modifying
    @Transactional
    @Query(
            nativeQuery = true,
            value = "INSERT INTO admin_console.warning_message_read(user_id, warning_id) VALUES (:userId, :warningId)"
    )
    void insertWarningForUser(String userId, String warningId);

    @Query(
            nativeQuery = true,
            value = "SELECT warning_id FROM admin_console.warning_message_read WHERE user_id = :userId"
    )
    List<String> findViewedWarningOfUser(String userId);

    @Query(
            nativeQuery = true,
            value = "SELECT count(*) from admin_console.warning_message_user u where u.user_id = :userId and u.warning_id not in " +
                    "( select r.warning_id from admin_console.warning_message_read r " +
                    " where r.user_id = :userId )"
    )
    int countWarningUnreadByUser(String userId);

}

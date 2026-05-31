package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.group.GroupUserMapping;
import com.elcom.adminconsolebackend.entity.management.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
//    @Query(nativeQuery = true,
//            value = "select * from admin_console.group " +
//                    "where name like :name " +
//                    "or email like :email " +
//                    "or phone_number like :phoneNumber " +
//                    "limit :limit " +
//                    "offset :offset")
    Page<Group> searchByNameLikeOrEmailLikeOrPhoneNumberLike(String name, String email, String phoneNumber, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select g.name, gm.user_id as userId from admin_console.group g " +
                    "left join admin_console.group_mapping gm on g.id = gm.group_id " +
                    "where gm.user_id in :userIds ")
    List<GroupUserMapping> getGroupByUserIds(List<String> userIds);

    @Query(nativeQuery = true,
    value = "select g.* from admin_console.group g " +
            "left join admin_console.group_mapping gm on g.id = gm.group_id " +
            "where gm.user_id = :userId ")
    Group getGroupByUserId(String userId);

    @Query(nativeQuery = true,
    value = "select g.* " +
            "from admin_console.group g " +
            "         inner join admin_console.group_mapping gm on g.id = gm.group_id " +
            "where enable = true " +
            "group by g.id")
    List<Group> getAllGroupsAvailable();

    @Query(nativeQuery = true,
    value = "select count(id) " +
            "from admin_console.group g " +
            "where enable = true")
    Long countNumGroupsEnable();

    @Query(nativeQuery = true,
    value = "select * from admin_console.group g " +
            "where is_locked = true")
    Group getGroupDefault();

}

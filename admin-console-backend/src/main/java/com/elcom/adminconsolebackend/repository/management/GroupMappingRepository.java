package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.entity.management.GroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMappingRepository extends JpaRepository<GroupMapping, GroupMapping.GroupMappingPK> {
    List<GroupMapping> findByGroupIdIn(List<String> groupIds);

    List<GroupMapping> findByUsernameIn(List<String> userNames);

    List<GroupMapping> findByGroupIdEquals(String groupId);

    GroupMapping findByUserIdEquals(String userId);

}

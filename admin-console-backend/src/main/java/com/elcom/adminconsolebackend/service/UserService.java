package com.elcom.adminconsolebackend.service;

import com.elcom.adminconsolebackend.dto.authorization.ApiMapping;
import com.elcom.adminconsolebackend.dto.user.ChangePassword;
import com.elcom.adminconsolebackend.dto.user.UserCompactDTO;
import com.elcom.adminconsolebackend.dto.user.UserDetailDTO;
import com.elcom.adminconsolebackend.dto.user.request.CreateUserRequest;
import com.elcom.adminconsolebackend.dto.user.request.FilterUserRequest;
import com.elcom.adminconsolebackend.dto.user.request.ResetUserRequest;
import com.elcom.adminconsolebackend.dto.user.request.UpdateUserRequest;
import com.elcom.adminconsolebackend.dto.user.response.CheckResourceAccessibleResponse;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<UserCompactDTO> filterUser(FilterUserRequest request);
    List<UserCompactDTO> filterUserTask(FilterUserRequest request);

    List<EventRepresentation> getEventForUser(List<String> type, String client, String user, String dateFrom, String dateTo, String ipAddress, Integer first, Integer max);

    List<UserCompactDTO> generateUserCompactDTOSFromUserRepresentations(List<UserRepresentation> userRepresentations);

    UserDetailDTO getUserInfo(String userId);

    UserDetailDTO getUserInfo();

    Boolean changePassword(ChangePassword changePassword);

    CheckResourceAccessibleResponse getResourceAccessible(List<String> groupIds, List<String> roleIds);

    void createUser(CreateUserRequest request);

    void updateUser(UpdateUserRequest request, String userId);

    void updateUser(UpdateUserRequest request);

    void resetUserPassword(String userId, ResetUserRequest request);

    void deleteUser(String userId);
    List<Map<String,List<ApiMapping.MappingResource>>> getMenuOfCurrentUser();

}

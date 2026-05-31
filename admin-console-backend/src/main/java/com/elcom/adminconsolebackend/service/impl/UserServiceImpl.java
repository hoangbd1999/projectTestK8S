package com.elcom.adminconsolebackend.service.impl;

import com.authz.sdk.client.AuthzClient;
import com.authz.sdk.exceptions.AuthzException;
import com.authz.sdk.grpc.Authz;
import com.elcom.adminconsolebackend.config.MessageLanguage;
import com.elcom.adminconsolebackend.config.authz.AuthzConfig;
import com.elcom.adminconsolebackend.dto.authorization.ApiMapping;
import com.elcom.adminconsolebackend.contant.ApplicationType;
import com.elcom.adminconsolebackend.dto.authz.GroupDTO;
import com.elcom.adminconsolebackend.dto.authz.ResourceDTO;
import com.elcom.adminconsolebackend.dto.authz.RoleDTO;
import com.elcom.adminconsolebackend.dto.group.GroupUserMapping;
import com.elcom.adminconsolebackend.dto.user.ChangePassword;
import com.elcom.adminconsolebackend.dto.user.UserCompactDTO;
import com.elcom.adminconsolebackend.dto.user.UserDetailDTO;
import com.elcom.adminconsolebackend.dto.user.request.CreateUserRequest;
import com.elcom.adminconsolebackend.dto.user.request.FilterUserRequest;
import com.elcom.adminconsolebackend.dto.user.request.ResetUserRequest;
import com.elcom.adminconsolebackend.dto.user.request.UpdateUserRequest;
import com.elcom.adminconsolebackend.dto.user.response.CheckResourceAccessibleResponse;
import com.elcom.adminconsolebackend.entity.management.Group;
import com.elcom.adminconsolebackend.entity.management.GroupMapping;
import com.elcom.adminconsolebackend.exception.ResourceExistException;
import com.elcom.adminconsolebackend.exception.ResourceNotFoundException;
import com.elcom.adminconsolebackend.exception.ValidationAuthorException;
import com.elcom.adminconsolebackend.repository.management.GroupMappingRepository;
import com.elcom.adminconsolebackend.repository.management.GroupRepository;
import com.elcom.adminconsolebackend.service.UserService;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import com.elcom.adminconsolebackend.util.datetime.DateUtils;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elcom.adminconsolebackend.config.api.RequestMappingConfig.MENU_MAPPING;
import static java.util.stream.Collectors.toMap;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String AUTHZ_USER_PREFIX = "authz-user-";

    private final RealmResource realmResource;

    private final AuthzClient authzClient;

    private final GroupRepository groupRepository;

    private final GroupMappingRepository groupMappingRepository;

    private final AuthzConfig authz;

    private final com.elcom.adminconsolebackend.config.keycloak.KeycloakProviderConfig keycloakProviderConfig;

    private final MessageLanguage messageLanguage;

//    private final Cache<String, Authz.Principal> principleAuthzCache = Caffeine.newBuilder()
//            .expireAfterWrite(15, TimeUnit.MINUTES)
//            .maximumSize(200)
//            .build();

    @Override
    public List<UserCompactDTO> filterUser(FilterUserRequest request) {
        List<UserRepresentation> userRepresentations;
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            userRepresentations = realmResource.users().search(request.getUsername());
        } else {
            userRepresentations = realmResource.users().list();
        }

        List<UserCompactDTO> userCompactDTOS = generateUserCompactDTOSFromUserRepresentations(userRepresentations);

        List<String> userIds = userCompactDTOS.stream().map(UserCompactDTO::getId).toList();
        Map<String, String> userGroupMap = groupRepository.getGroupByUserIds(userIds).stream().collect(toMap(GroupUserMapping::getUserId, GroupUserMapping::getName));
        userCompactDTOS.forEach(userCompactDTO -> userCompactDTO.setGroup(userGroupMap.get(userCompactDTO.getId())));

        return userCompactDTOS;
    }

    @Override
    public List<UserCompactDTO> filterUserTask(FilterUserRequest request) {
        List<UserRepresentation> userRepresentations;
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            userRepresentations = realmResource.users().search(request.getUsername());
        } else {
            userRepresentations = realmResource.users().list();
        }
        List<String> userAccess = new ArrayList<>();
        try {
            List<Authz.Check> checkRequests = userRepresentations.parallelStream()
                    .map(user -> Authz.Check.newBuilder()
                            .setPrincipal("authz-user-" + user.getUsername())
                            .setResourceKind("metaint.metacen.task")
                            .setResourceValue("*")
                            .setAction("update")
                            .build())
                    .collect(Collectors.toList());

            Authz.CheckResponse checkResponse = authzClient.checkPermision(checkRequests);
            userAccess = checkResponse.getChecksList().stream().filter(Authz.CheckAnswer::getIsAllowed).map((item) -> item.getPrincipal().substring(item.getPrincipal().lastIndexOf("authz-user-") + 11)).collect(Collectors.toList());
//            else {
//                return true;
//            }
//            Authz.RoleResults results =  authzClient.roleList(Authz.GPaging.newBuilder().setPage(0).setSize(10000).build());
//            List<String> nguoigiamsat =
//                    Authz.PrincipalResults principalResults = authzClient.principalList(Authz.GPaging.newBuilder().setPage(0).setSize(10000).build());
//            System.out.println("Ad");
//            List<String> ids = principalResults.getPrincipalsList().stream().filter((principal -> principal.))
        } catch (AuthzException e) {
            throw new RuntimeException(e);
        }


        List<UserCompactDTO> userCompactDTOS = generateUserCompactDTOSFromUserRepresentations(userRepresentations);
        List<String> finalUserAccess = userAccess;
        userCompactDTOS = userCompactDTOS.stream().filter(user -> finalUserAccess.contains(user.getUsername())).collect(Collectors.toList());
        List<String> userIds = userCompactDTOS.stream().map(UserCompactDTO::getId).toList();
        Map<String, String> userGroupMap = groupRepository.getGroupByUserIds(userIds).stream().collect(toMap(GroupUserMapping::getUserId, GroupUserMapping::getName));
        userCompactDTOS.forEach(userCompactDTO -> userCompactDTO.setGroup(userGroupMap.get(userCompactDTO.getId())));

        return userCompactDTOS;
    }

    @Override
    public List<EventRepresentation> getEventForUser(List<String> type, String client, String user, String dateFrom, String dateTo, String ipAddress, Integer first, Integer max) {
        return realmResource.getEvents(type, client, user, dateFrom, dateTo, ipAddress, 0, max);
    }

    @Override
    public List<UserCompactDTO> generateUserCompactDTOSFromUserRepresentations(List<UserRepresentation> userRepresentations) {
        return userRepresentations.parallelStream()
                .sorted(Comparator.comparingLong(UserRepresentation::getCreatedTimestamp).reversed())
                .map(userRepresentation -> {
                    Authz.Principal principle = getPrincipleOfUser(userRepresentation.getUsername());

                    UserCompactDTO userCompactDTO = new UserCompactDTO();
                    userCompactDTO.setId(userRepresentation.getId());
                    userCompactDTO.setUsername(userRepresentation.getUsername());
                    userCompactDTO.setEnable(userRepresentation.isEnabled());
                    userCompactDTO.setAuthzGroups(getAuthzGroups(principle.getGroupsList()));
                    userCompactDTO.setRoles(getRoles(principle.getRolesList()));
                    userCompactDTO.setCreatedTimestamp(DateUtils.getDateFromLong(userRepresentation.getCreatedTimestamp()));

                    return userCompactDTO;
                })
                .collect(Collectors.toList());
    }

    private List<GroupDTO> getAuthzGroups(List<String> groupEntities) {
        return groupEntities.stream().map(groupId -> {
            Authz.GroupGetRequest groupGetRequest = Authz.GroupGetRequest.newBuilder()
                    .setId(groupId)
                    .build();
            try {
                Authz.GroupGetResponse groupGetResponse = authzClient.groupGet(groupGetRequest);
                return groupGetResponse.getGroup();
            } catch (AuthzException e) {
                throw new RuntimeException(e);
            }
        })
                .map(group -> new GroupDTO()
                        .setId(group.getId())
                        .setName(group.getName())
                        .setPolicies(group.getPoliciesList()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDetailDTO getUserInfo(String userId) {
        UserRepresentation userRepresentation = realmResource.users().get(userId).toRepresentation();

        Map<String, String> userAttributes = null;
        if (userRepresentation.getAttributes() != null) {
            userAttributes = userRepresentation.getAttributes().entrySet()
                    .stream()
                    .filter(attributeEntrySet ->
                            !attributeEntrySet.getKey().equals("avatar") && !attributeEntrySet.getKey().equals("phone")
                                    && !attributeEntrySet.getKey().equals("application"))
                    .collect(toMap(Map.Entry::getKey,
                            entry -> (entry.getValue() == null || entry.getValue().isEmpty()) ? "" : entry.getValue().get(0)));
        }

        UserDetailDTO userDetailDTO = new UserDetailDTO();
        userDetailDTO.setId(userRepresentation.getId())
                .setUsername(userRepresentation.getUsername())
                .setName(userRepresentation.getFirstName())
                .setEmail(userRepresentation.getEmail())
                .setEnable(userRepresentation.isEnabled())
                .setAttributes(userAttributes);

        if (userRepresentation.getAttributes() != null) {
            userDetailDTO.setAvatar(userRepresentation.getAttributes().get("avatar") != null ? userRepresentation.getAttributes().get("avatar").get(0) : null)
                    .setPhone(userRepresentation.getAttributes().get("phone") != null ? userRepresentation.getAttributes().get("phone").get(0) : null)
                    .setApplication(userRepresentation.getAttributes().get("application") != null ? userRepresentation.getAttributes().get("application") : null)
                    .setDataSource(userRepresentation.getAttributes().get("dataSource") != null ? userRepresentation.getAttributes().get("dataSource") : null);
        }

        Group group = groupRepository.getGroupByUserId(userId);
        userDetailDTO.setGroup(group);
        return userDetailDTO;
    }

    @Override
    public UserDetailDTO getUserInfo() {
        String userId = SecurityUtils.getCurrentUserId();
        UserRepresentation userRepresentation = realmResource.users().get(userId).toRepresentation();

        Map<String, String> userAttributes = null;
        if (userRepresentation.getAttributes() != null) {
            userAttributes = userRepresentation.getAttributes().entrySet()
                    .stream()
                    .filter(attributeEntrySet ->
                            !attributeEntrySet.getKey().equals("avatar") && !attributeEntrySet.getKey().equals("phone")
                                    && !attributeEntrySet.getKey().equals("application"))
                    .collect(toMap(Map.Entry::getKey,
                            entry -> (entry.getValue() == null || entry.getValue().isEmpty()) ? "" : entry.getValue().get(0)));
        }

        UserDetailDTO userDetailDTO = new UserDetailDTO();
        userDetailDTO.setId(userRepresentation.getId())
                .setUsername(userRepresentation.getUsername())
                .setName(userRepresentation.getFirstName())
                .setEmail(userRepresentation.getEmail())
                .setEnable(userRepresentation.isEnabled())
                .setAttributes(userAttributes);

        if (userRepresentation.getAttributes() != null) {
            userDetailDTO.setAvatar(userRepresentation.getAttributes().get("avatar") != null ? userRepresentation.getAttributes().get("avatar").get(0) : null)
                    .setPhone(userRepresentation.getAttributes().get("phone") != null ? userRepresentation.getAttributes().get("phone").get(0) : null)
                    .setApplication(userRepresentation.getAttributes().get("application") != null ? userRepresentation.getAttributes().get("application") : null)
                    .setDataSource(userRepresentation.getAttributes().get("dataSource") != null ? userRepresentation.getAttributes().get("dataSource") : null);
        }
        Authz.Principal principle = getPrincipleOfUser(userRepresentation.getUsername());
        Group group = groupRepository.getGroupByUserId(userId);
        userDetailDTO.setGroup(group);
        userDetailDTO.setRoles(getRoles(principle.getRolesList()));
        return userDetailDTO;
    }

    private boolean verifyOldPassword(String username, String oldPassword) {
        String tokenEndpoint = keycloakProviderConfig.getServerUrl() + "/realms/" + keycloakProviderConfig.getRealm() + "/protocol/openid-connect/token";

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", keycloakProviderConfig.getClientId());
        requestBody.add("client_secret", keycloakProviderConfig.getClientSecret());
        requestBody.add("grant_type", "password");
        requestBody.add("username", username);
        requestBody.add("password", oldPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(tokenEndpoint, request, String.class);
            return true; // Mật khẩu cũ đúng
        } catch (HttpClientErrorException e) {
            return false; // Mật khẩu cũ sai
        }
    }

    @Override
    public Boolean changePassword(ChangePassword changePassword) {
        if(SecurityUtils.getCurrentUserName().equalsIgnoreCase("admin")) {
            throw new ValidationAuthorException(messageLanguage.getMessage("user.not_permit"));
        }
        if (verifyOldPassword(SecurityUtils.getCurrentUserName(), changePassword.getOldPassword())) {
            UserResource userResource = realmResource.users().get(SecurityUtils.getAuthenticatedUserDTO().getUuid());
            if (userResource == null) {
                throw new RuntimeException(messageLanguage.getMessage("user.not_found"));
            }
            UserRepresentation userRepresentation = new UserRepresentation();
            CredentialRepresentation passwordCredential = new CredentialRepresentation();
            passwordCredential.setType(CredentialRepresentation.PASSWORD);
            passwordCredential.setValue(changePassword.getNewPassword());

            userRepresentation.setCredentials(List.of(passwordCredential));
            userResource.update(userRepresentation);
            userResource.logout();
            System.out.println("true");
        } else {
            throw new ResourceNotFoundException(messageLanguage.getMessage("passwordIncorrect"));
        }
        return true;
    }

    @Override
    public CheckResourceAccessibleResponse getResourceAccessible(List<String> groupIds, List<String> roleIds) {
        List<RoleDTO> roles = getRoles(roleIds);
        List<GroupDTO> groups = getAuthzGroups(groupIds);

        List<ResourceDTO> resources = Stream.concat(
                groups.stream().flatMap(groupDTO -> groupDTO.getPolicies().stream()),
                roles.stream().flatMap(roleDTO -> roleDTO.getPolicy().stream()))
                .distinct()
                .flatMap(policyId -> {
                    Authz.PolicyGetRequest request = Authz.PolicyGetRequest.newBuilder()
                            .setId(policyId)
                            .build();
                    try {
                        return authzClient.policyGet(request).getPolicy().getResourcesList().stream();
                    } catch (AuthzException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(resourceId -> {
                    Authz.ResourceGetRequest request = Authz.ResourceGetRequest.newBuilder()
                            .setId(resourceId)
                            .build();
                    try {
                        Authz.Resource resource = authzClient.resourceGet(request).getResource();
                        return new ResourceDTO().setName(resource.getId())
                                .setKind(resource.getKind())
                                .setValue(resource.getValue());
                    } catch (AuthzException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();

        CheckResourceAccessibleResponse response = new CheckResourceAccessibleResponse();
        response.setRoles(roles)
                .setGroups(groups)
                .setResourceDTOS(resources);
        return response;
    }

    private List<RoleDTO> getRoles(List<String> roleEntities) {
        return roleEntities.stream().map(roleId -> {
            Authz.RoleGetRequest roleGetRequest = Authz.RoleGetRequest.newBuilder()
                    .setId(roleId)
                    .build();
            try {
                Authz.RoleGetResponse roleGetResponse = authzClient.roleGet(roleGetRequest);
                return roleGetResponse.getRole();
            } catch (AuthzException e) {
                throw new RuntimeException(e);
            }
        })
                .map(role -> new RoleDTO()
                        .setId(role.getId())
                        .setName(role.getName())
                        .setPolicy(role.getPoliciesList()))
                .collect(Collectors.toList());
    }

    private Authz.Principal getPrincipleOfUser(String username) {
        Authz.Principal principle;
        Authz.PrincipalGetRequest principalGetRequest = Authz.PrincipalGetRequest.newBuilder()
                .setId(AUTHZ_USER_PREFIX + username)
                .build();
        try {
            Authz.PrincipalGetResponse principalGetResponse = authzClient.principalGet(principalGetRequest);
            principle = principalGetResponse.getPrincipal();
        } catch (AuthzException e) {
            if (e.getStatus().getCode().equals(Status.Code.NOT_FOUND)) {
                return createNewPrincipal(username, Collections.emptyList(), Collections.emptyList());
            } else {
                throw new RuntimeException(e);
            }
        }

        return principle;
    }

    private Authz.Principal createNewPrincipal(String username, List<String> groupIds, List<String> roleIds) {
        //TODO: add more properties to principle when create new principle
        Authz.PrincipalCreateRequest createRequest = Authz.PrincipalCreateRequest.newBuilder()
                .setId(AUTHZ_USER_PREFIX + username)
                .addAllGroups(groupIds)
                .addAllRoles(roleIds)
                .build();
        try {
            Authz.PrincipalCreateResponse principalCreateResponse = authzClient.principalCreate(createRequest);
//            principleAuthzCache.put(username, principalCreateResponse.getPrincipal());
            return principalCreateResponse.getPrincipal();
        } catch (AuthzException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Transactional
    public void createUser(CreateUserRequest request) {
        UserRepresentation userRepresentation = createBaseUserRepresentation(request.getUsername(), request.getName(), request.getEmail(), request.getEnable(), request.getAttributes(), request.getAvatar(), request.getPhone(), request.getApplication(), request.getDataSource());

        CredentialRepresentation passwordCredential = new CredentialRepresentation();
        passwordCredential.setType(CredentialRepresentation.PASSWORD);
        passwordCredential.setValue(request.getPassword());

        userRepresentation.setCredentials(List.of(passwordCredential));
        try (Response createUserResponse = realmResource.users().create(userRepresentation)) {
            if (createUserResponse.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new ResourceExistException("Trùng username.");
            } else if (createUserResponse.getStatus() != (Response.Status.CREATED.getStatusCode()))
                throw new RuntimeException();


            String userId = createUserResponse.getLocation().getPath().substring(createUserResponse.getLocation().getPath().lastIndexOf("/") + 1);
            System.out.println("========>" + userId);
//            String userId = createUserResponse.getLocation().getPath().split("/")[6];

            // join group and update user status by group enable.
            Group group = this.joinGroup(request.getGroupId(), userId, request.getUsername().toLowerCase());
            if (group.getEnable() != userRepresentation.isEnabled()) {
                userRepresentation.setEnabled(group.getEnable());
                realmResource.users().get(userId).update(userRepresentation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Authz.PrincipalGetRequest principalGetRequest = Authz.PrincipalGetRequest.newBuilder()
                .setId(AUTHZ_USER_PREFIX + request.getUsername().toLowerCase())
                .build();

        Authz.PrincipalGetResponse principalGetResponse = null;
        try {
            principalGetResponse = authzClient.principalGet(principalGetRequest);
        } catch (StatusRuntimeException ignore) {
            ignore.printStackTrace();
        } catch (AuthzException e) {
            e.printStackTrace();
            if (!e.getStatus().getCode().equals(Status.Code.NOT_FOUND)) throw new RuntimeException(e);
        }

        if (principalGetResponse != null) {
            removePrincipal(request.getUsername().toLowerCase());
        }

        createNewPrincipal(request.getUsername().toLowerCase(),
                request.getAuthzGroupIds() == null ? Collections.emptyList() : request.getAuthzGroupIds(),
                request.getRoleIds() == null ? Collections.emptyList() : request.getRoleIds());
    }

    private void removePrincipal(String username) {
        Authz.PrincipalDeleteRequest createRequest = Authz.PrincipalDeleteRequest.newBuilder()
                .setId(AUTHZ_USER_PREFIX + username)
                .build();
        try {
            authzClient.principalDelete(createRequest);
        } catch (AuthzException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserRequest request, String userId) {
        UserResource userResource = realmResource.users().get(userId);
        if (userResource == null) {
            throw new RuntimeException(messageLanguage.getMessage("user.not_found"));
        }
        UserRepresentation userRepresentation = createBaseUserRepresentation(request.getUsername(), request.getName(), request.getEmail(), request.getEnable(), request.getAttributes(), request.getAvatar(), request.getPhone(), request.getApplication(), request.getDataSource());
        userResource.update(userRepresentation);

        Authz.PrincipalGetRequest principalGetRequest = Authz.PrincipalGetRequest.newBuilder()
                .setId(AUTHZ_USER_PREFIX + request.getUsername())
                .build();

        Authz.PrincipalGetResponse principalGetResponse = null;
        try {
            principalGetResponse = authzClient.principalGet(principalGetRequest);
            clearCache();
        } catch (StatusRuntimeException ignore) {
        } catch (AuthzException e) {
            if (!e.getStatus().getCode().equals(Status.Code.NOT_FOUND)) throw new RuntimeException(e);
        }

        if (principalGetResponse != null) {
            updatePrinciple(principalGetResponse.getPrincipal(),
                    request.getRoleIds(),
                    request.getAuthzGroupIds());
        } else {
            createNewPrincipal(request.getUsername(),
                    request.getAuthzGroupIds() == null ? Collections.emptyList() : request.getAuthzGroupIds(),
                    request.getRoleIds() == null ? Collections.emptyList() : request.getRoleIds());
        }

        // update group for user and update user status by group enable.

        GroupMapping groupMapping = groupMappingRepository
                .findByUserIdEquals(userId);
        if (groupMapping != null && !groupMapping.getGroupId().equals(request.getGroupId())) {
            groupMappingRepository.delete(groupMapping);
            Group group = this.joinGroup(request.getGroupId(), userId, request.getUsername().toLowerCase());
            if (group.getEnable() != userRepresentation.isEnabled() && !group.getEnable()) {
                userRepresentation.setEnabled(false);
                userResource.update(userRepresentation);
            }
        } else if (groupMapping == null) {
            Group group = this.joinGroup(request.getGroupId(), userId, request.getUsername().toLowerCase());
            if (group.getEnable() != userRepresentation.isEnabled() && !group.getEnable()) {
                userRepresentation.setEnabled(false);
                userResource.update(userRepresentation);
            }
        }

        if (!userRepresentation.isEnabled()) {
            userResource.logout();
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserResource userResource = realmResource.users().get(userId);
        if (userResource == null) {
            throw new RuntimeException(messageLanguage.getMessage("user.not_found"));
        }
        UserRepresentation userRepresentation = createBaseUserRepresentation(request.getUsername(), request.getName(), request.getEmail(), request.getEnable(), request.getAttributes(), request.getAvatar(), request.getPhone(), request.getApplication(), request.getDataSource());
        userRepresentation.getAttributes().put("application", userResource.toRepresentation().getAttributes().get("application"));
        userRepresentation.getAttributes().put("dataSource", userResource.toRepresentation().getAttributes().get("dataSource"));
        userResource.update(userRepresentation);
    }

    public void updatePrinciple(Authz.Principal principal, List<String> roleIds, List<String> groupIds) {
        Authz.PrincipalUpdateRequest request = Authz.PrincipalUpdateRequest.newBuilder()
                .setId(principal.getId())
                .addAllGroups(groupIds == null ? Collections.emptyList() : groupIds)
                .addAllRoles(roleIds == null ? Collections.emptyList() : roleIds)
                .build();

        try {
            authzClient.principalUpdate(request);
        } catch (AuthzException e) {
            throw new RuntimeException(e);
        }
    }

    private UserRepresentation createBaseUserRepresentation(String username, String name, String email, Boolean enable, Map<String, String> attributes2, String avatar, String phone, List<ApplicationType> applications, List<String> dataSources) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(username);

        if (name != null) {
            userRepresentation.setFirstName(name);
        }

        if (email != null) {
            userRepresentation.setEmail(email);
        }

        userRepresentation.setEnabled(enable);

        Map<String, List<String>> attributes = new HashMap<>();

        if (attributes2 != null) {
            attributes.putAll(attributes2.entrySet()
                    .stream()
                    .map(entry -> Map.entry(entry.getKey(), List.of(entry.getValue())))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        if (avatar != null) {
            attributes.put("avatar", List.of(avatar));
        }

        if (phone != null) {
            attributes.put("phone", List.of(phone));
        }

        if (applications != null) {
            attributes.put("application", applications.stream().map(i -> i.name()).collect(Collectors.toList()));
        }
        if (applications != null) {
            attributes.put("dataSource", dataSources);
        }


        userRepresentation.setAttributes(attributes);
        return userRepresentation;
    }

    @Override
    public void resetUserPassword(String userId, ResetUserRequest request) {
        if(request.getUsername().equalsIgnoreCase("admin")) {
            throw new ValidationAuthorException(messageLanguage.getMessage("user.not_permit"));
        }

        UserResource userResource = realmResource.users().get(userId);
        if (userResource == null) {
            throw new RuntimeException("User doesn't exist");
        }
        UserRepresentation userRepresentation = new UserRepresentation();
        CredentialRepresentation passwordCredential = new CredentialRepresentation();
        passwordCredential.setType(CredentialRepresentation.PASSWORD);
        passwordCredential.setValue(request.getPassword());

        userRepresentation.setCredentials(List.of(passwordCredential));
        userResource.update(userRepresentation);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        UserResource userResource = realmResource.users().get(userId);
        if (userResource == null) {
            throw new RuntimeException("User doesn't exist");
        }
        String username = userResource.toRepresentation().getUsername();
        try (Response deleteUserResponse = realmResource.users().delete(userId)) {
            removePrincipal(username);
        }

        this.leaveGroup(userId);
    }

    @Override
    public List<Map<String, List<ApiMapping.MappingResource>>> getMenuOfCurrentUser() {
        String currentUsername = (String) ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaims().get("preferred_username");
        List<Map<String, List<ApiMapping.MappingResource>>> menu = new ArrayList<>();
        MENU_MAPPING.entrySet()
                .stream().filter(entry -> {
            List<ApiMapping.MappingResource> valuesToCheck = entry.getValue();
            Set<Authz.Check> checkRequests = new HashSet<>();
            valuesToCheck.forEach(mappingResource -> {
                if (mappingResource.getActionMapping().contains(",")) {
                    List<String> actions = Stream.of(mappingResource.getActionMapping().split(","))
                            .collect(Collectors.toList());
                    actions.forEach(item -> {
                        Authz.Check check = Authz.Check.newBuilder()
                                .setPrincipal("authz-user-" + currentUsername)
                                .setResourceKind(mappingResource.getResourceKindMapping())
                                .setResourceValue(mappingResource.getResourceValueMapping())
                                .setAction(item)
                                .build();
                        checkRequests.add(check);
                    });
                } else {
                    Authz.Check check = Authz.Check.newBuilder()
                            .setPrincipal("authz-user-" + currentUsername)
                            .setResourceKind(mappingResource.getResourceKindMapping())
                            .setResourceValue(mappingResource.getResourceValueMapping())
                            .setAction(mappingResource.getActionMapping())
                            .build();
                    checkRequests.add(check);
                }
            });

            Authz.CheckResponse checkResponse = null;
            try {
                checkResponse = authzClient.checkPermision(new ArrayList<>(checkRequests));
            } catch (AuthzException e) {
                e.printStackTrace();
                throw new RuntimeException("Service authz lỗi.");
            }
            Map<String, List<ApiMapping.MappingResource>> mapMenu = new HashMap<>();
            List<ApiMapping.MappingResource> checkAnswers = checkResponse.getChecksList().stream().filter(Authz.CheckAnswer::getIsAllowed).map(item -> {
                ApiMapping.MappingResource resource = new ApiMapping.MappingResource();
                resource.setResourceKindMapping(item.getResourceKind());
                resource.setActionMapping(item.getAction());
                return resource;
            }).collect(Collectors.toList());
            if (!checkAnswers.isEmpty()) {
                mapMenu.put(entry.getKey(), checkAnswers);
                menu.add(mapMenu);
            }
            return true;
        }).collect(Collectors.toList());
        return menu;
    }

    private Group joinGroup(String groupId, String userId, String username) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            throw new ResourceNotFoundException("Group không tồn tại.");
        }

        groupMappingRepository.save(new GroupMapping()
                .setGroupId(groupId)
                .setUserId(userId)
                .setUsername(username));
        return groupOptional.get();
    }

    private void leaveGroup(String userId) {
        GroupMapping groupMapping = groupMappingRepository
                .findByUserIdEquals(userId);
        if (groupMapping != null) {
            groupMappingRepository.delete(groupMapping);
        }
    }

    private void clearCache() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authz.getToken());

        HttpEntity<Object> entity = new HttpEntity<>(headers);
        restTemplate.exchange(authz.getClearCacheUrl(), HttpMethod.GET, entity, Object.class);
    }
}

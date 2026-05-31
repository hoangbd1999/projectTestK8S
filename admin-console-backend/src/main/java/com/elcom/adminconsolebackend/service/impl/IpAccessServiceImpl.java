package com.elcom.adminconsolebackend.service.impl;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.IpAccessDTO;
import com.elcom.adminconsolebackend.dto.IpAccessRequest;
import com.elcom.adminconsolebackend.entity.management.IpAccessEntity;
import com.elcom.adminconsolebackend.exception.ResourceNotFoundException;
import com.elcom.adminconsolebackend.repository.management.IpAccessRepository;
import com.elcom.adminconsolebackend.repository.management.IpAccessRepositoryCustom;
import com.elcom.adminconsolebackend.service.IpAccessService;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpAccessServiceImpl implements IpAccessService {

    @Autowired
    private IpAccessRepository ipAccessRepository;
    @Autowired
    private IpAccessRepositoryCustom ipAccessRepositoryCustom;


    @Override
    public void create(IpAccessRequest ipAccessRequest) {
        IpAccessEntity ipAccessEntity = ipAccessRepository.findByIpAddress(ipAccessRequest.getIpAddress());
        if (ipAccessEntity != null) {
            throw new ResourceNotFoundException("Ip Address already exist");
        }
        IpAccessEntity ipAccessEntitySave = new IpAccessEntity();
        ipAccessEntitySave.setIpAddress(ipAccessRequest.getIpAddress());
        ipAccessEntitySave.setDescription(ipAccessRequest.getDescription());
        ipAccessEntitySave.setAccounts(ipAccessRequest.getAccounts());
        ipAccessEntitySave.setCreatedBy(SecurityUtils.getCurrentUserName());
        ipAccessEntitySave.setStatus(ipAccessRequest.getIsAllowed());
        ipAccessEntitySave.setCreatedAt(LocalDateTime.now().plusHours(7));
        ipAccessRepository.save(ipAccessEntitySave);
    }

    @Override
    public void update(IpAccessRequest ipAccessRequest) {
        Optional<IpAccessEntity> ipAccessEntity = ipAccessRepository.findById(ipAccessRequest.getId());
        if (ipAccessEntity.isEmpty()) {
            throw new ResourceNotFoundException("Resource not found");
        }
        IpAccessEntity ipAccess = ipAccessEntity.get();
        ipAccess.setIpAddress(ipAccessRequest.getIpAddress());
        ipAccess.setDescription(ipAccessRequest.getDescription());
        ipAccess.setAccounts(ipAccessRequest.getAccounts());
        ipAccess.setStatus(ipAccessRequest.getIsAllowed());
        ipAccessRepository.save(ipAccess);
    }

    @Override
    public void delete(List<Long> ids) {
        ipAccessRepository.deleteAllById(ids);
    }

    @Override
    public Page<IpAccessDTO> filter(List<FilterCondition> conditions, Pageable pageable) {
        IpAccessRepositoryCustom.QueryInfo queryInfo = ipAccessRepositoryCustom.calculateQueryInfo(conditions);
        List<IpAccessDTO> ipAccessDTOS = ipAccessRepositoryCustom.filterElementsOnSpecificPage(queryInfo, pageable);
        Long count = ipAccessRepositoryCustom.count(queryInfo);
        return new PageImpl<>(ipAccessDTOS, pageable, count);
    }
}

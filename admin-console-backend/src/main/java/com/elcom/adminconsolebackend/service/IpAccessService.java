package com.elcom.adminconsolebackend.service;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.IpAccessDTO;
import com.elcom.adminconsolebackend.dto.IpAccessRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IpAccessService {

    void create(IpAccessRequest ipAccessRequest);

    void update(IpAccessRequest ipAccessRequest);

    void delete(List<Long> id);

    Page<IpAccessDTO> filter(List<FilterCondition> conditions, Pageable pageable);

}

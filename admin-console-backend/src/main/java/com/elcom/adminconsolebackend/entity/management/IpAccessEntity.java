package com.elcom.adminconsolebackend.entity.management;

import com.elcom.adminconsolebackend.dto.AccountLimitDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "ip_access", schema = "admin_console")
public class IpAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "accounts", columnDefinition = "jsonb")
    private List<AccountLimitDTO> accounts;

    private Integer status;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;

}

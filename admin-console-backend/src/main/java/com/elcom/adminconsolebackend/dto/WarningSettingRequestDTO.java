
package com.elcom.adminconsolebackend.dto;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WarningSettingRequestDTO implements Serializable {

    private String id;

    private String warningChannel;

    @ColumnTransformer(write = "?::jsonb")
    private List<String> warningObjects;

    @ColumnTransformer(write = "?::jsonb")
    private List<String> resourceTypes;

    @ColumnTransformer(write = "?::jsonb")
    private List<String> warningLevels;

    private String emailSend;

    private String passwordEmailSend;

    private int status;

    @CreationTimestamp
    private LocalDateTime modifiedAt;

    private String modifiedBy;

}

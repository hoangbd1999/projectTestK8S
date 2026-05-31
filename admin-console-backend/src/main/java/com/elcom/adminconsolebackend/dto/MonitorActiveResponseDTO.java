
package com.elcom.adminconsolebackend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class MonitorActiveResponseDTO implements Serializable {

    private String serviceActive;

    private String serverActive;

    private String dataSourceActive;

}


package com.elcom.adminconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

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
public class DataCollectionDTO implements Serializable {

    private String uuid;

    private String application;

    private String ip;

    private Boolean status;

    private String cpu;

    private String ram;

    private String disk;

    private String network;

    private String powerUsage;

}

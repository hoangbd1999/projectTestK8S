
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
public class MonitorServerRequestDTO implements Serializable {

    private String id;

    @NotBlank(message = "Name not empty")
    @Length(max = 500, message = "Name exceeds 256 characters")
    private String name;

    @NotBlank(message = "Ip not empty")
    private String ip;

    private String domain;

    private String description;

}

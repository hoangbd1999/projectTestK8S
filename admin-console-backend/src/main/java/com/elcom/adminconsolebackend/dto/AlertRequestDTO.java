
package com.elcom.adminconsolebackend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class AlertRequestDTO implements Serializable {

    private String name;

    private String ip;

}

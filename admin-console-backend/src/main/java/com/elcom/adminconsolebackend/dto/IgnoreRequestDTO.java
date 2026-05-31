
package com.elcom.adminconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class IgnoreRequestDTO implements Serializable {

    private String key;

    private String timeIgnore; // 1 tiếng , 4 tiếng, 8 tiếng, cho đến khi mở lại (until reopens)

}

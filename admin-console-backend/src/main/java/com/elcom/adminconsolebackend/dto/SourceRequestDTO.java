
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
public class SourceRequestDTO implements Serializable {

    private String id;

    private String type;

    private Boolean connectStatus;

    private Boolean activeStatus;

    private String host;

    private Integer port;

    private String dataAlias;

    List<String> childs;

}

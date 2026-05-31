
package com.elcom.adminconsolebackend.dto;

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
public class ResourceDTO implements Serializable {

    private List<String> server;

    private List<String> applicationService;

    private List<String> platformService;




}

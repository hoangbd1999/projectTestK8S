package com.elcom.adminconsolebackend.config.api;

import com.elcom.adminconsolebackend.dto.authorization.ApiMapping;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Component
public class RequestMappingConfig {
    public static Map<String, Map<String, ApiMapping>> API_MAPPING;
    public static Map<String, List<ApiMapping.MappingResource>> MENU_MAPPING;

    private static final String REQUEST_MAPPING_FOLDER = "/request-mapping/**";

    @SneakyThrows
    public RequestMappingConfig(ObjectMapper objectMapper) {
        PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourceResolver.getResources(REQUEST_MAPPING_FOLDER);

        API_MAPPING = Arrays.stream(resources)
                .filter(file -> file.getFilename().endsWith("-mapping.json"))
                .map(mappingFile -> {
                    try {
                        List<ApiMapping> apiMappings = objectMapper.readValue(mappingFile.getInputStream(), new TypeReference<>() {
                        });

                        Map<String, ApiMapping> apiMappingDicionary = apiMappings.stream()
                                .collect(toMap(apiMapping -> apiMapping.getMethod() + apiMapping.getUri(), identity()));

                        return Map.entry(mappingFile.getFilename().replace("-mapping.json", ""), apiMappingDicionary);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        MENU_MAPPING = Arrays.stream(resources)
                .filter(file -> file.getFilename().endsWith("-mapping.json"))
                .flatMap(mappingFile -> {
                    try {
                        List<ApiMapping> apiMappings = objectMapper.readValue(mappingFile.getInputStream(), new TypeReference<>() {
                        });

                        return apiMappings.stream();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(apiMapping -> apiMapping.getMappingResources().stream())
                .distinct()
                .collect(groupingBy(ApiMapping.MappingResource::getResourceKindMapping));
    }
}

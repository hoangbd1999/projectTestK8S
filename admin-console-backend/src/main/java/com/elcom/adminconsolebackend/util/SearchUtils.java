package com.elcom.adminconsolebackend.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class SearchUtils {

    /**
     * IMPORTANT: Escape character must be the first one of the special characters
     */
    public static final String[] SPECIAL_CHARACTERS = {"\\", "%", "_"};

    public static final String ESCAPE_CHARACTER = "\\";

    public static String createSearchPatternFrom(String search) {
        return createSearchPatternFrom(search, false);
    }

    public static String createSearchPatternFrom(String search, boolean nullIfBlank) {
        if (!StringUtils.hasLength(search)) {
            return nullIfBlank ? null : "%%";
        }

        String pattern = search;
        for (String specialCharacter : SPECIAL_CHARACTERS) {
            String replacement = ESCAPE_CHARACTER + specialCharacter;
            pattern = pattern.replace(specialCharacter, replacement);
        }
        return "%" + pattern.trim() + "%";
    }

}

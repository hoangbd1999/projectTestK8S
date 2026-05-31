package com.elcom.adminconsolebackend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.shaded.gson.Gson;

public class JsonUtils {

    public static final Gson GSON = new Gson();

    public static <T> T fromJson(String str, Class<T> type) {
        return GSON.fromJson(str, type);
    }

    public static String toJson(Object obj) {
        try {
            return GSON.toJson(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T cast(Object source, Class<T> destType) {
        try {
            return fromJson(toJson(source), destType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAsText(JsonNode node, String attr) {
        if (node == null) return null;
        JsonNode attrNode = node.get(attr);
        return attrNode == null ? null : attrNode.textValue();
    }
}

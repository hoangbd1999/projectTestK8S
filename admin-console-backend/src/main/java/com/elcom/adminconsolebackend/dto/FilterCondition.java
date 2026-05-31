package com.elcom.adminconsolebackend.dto;

import com.elcom.adminconsolebackend.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FilterCondition {

    private String attrMappingCode;

    private Long attrId;

    private String attrCode;

    private FilterOperator operator;

    private String data;

    public static FilterCondition from(String attrCode, FilterOperator operator, List<Object> value) {
        FilterCondition condition = new FilterCondition();
        condition.setAttrCode(attrCode);
        condition.setOperator(operator);
        String data = "{\"value\": " + JsonUtils.toJson(value) +"}";
        condition.setData(data);
        return condition;
    }

    public static FilterCondition from(long attrId, FilterOperator operator, List<Object> value) {
        FilterCondition condition = new FilterCondition();
        condition.setAttrId(attrId);
        condition.setOperator(operator);
        String data = "{\"value\": " + JsonUtils.toJson(value) +"}";
        condition.setData(data);
        return condition;
    }

}
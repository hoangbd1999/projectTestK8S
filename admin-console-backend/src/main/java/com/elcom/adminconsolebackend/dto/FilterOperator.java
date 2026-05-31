package com.elcom.adminconsolebackend.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum FilterOperator {

    EQUALS,
    NOT_EQUALS,
    EMPTY,
    NOT_EMPTY,
    IN_RANGE,
    OUT_OF_RANGE,
    GREATER_THAN,
    LESS_THAN,

    STRING_CONTAINS,
    STRING_EQUALS,
    STRING_NOT_EQUALS,

    MULTI_STRING_CONTAINS,
    MULTI_STRING_EQUALS,
    MULTI_STRING_NOT_EQUALS,

    FILE_UPLOAD_CONTAINS,
    FILE_UPLOAD_EQUALS,
    FILE_UPLOAD_NOT_EQUALS,

    RANGE_CONTAINS,
    RANGE_IN_RANGE,
    RANGE_OUT_OF_RANGE,
    RANGE_GREATER_THAN,
    RANGE_LESS_THAN,

    MULTI_SELECTION_CONTAINS,
    MULTI_SELECTION_EQUALS,
    MULTI_SELECTION_NOT_EQUALS,
    MULTI_SELECTION_CONTAINS_ANY,
    ARRAY_NOT_CONTAINS;

}

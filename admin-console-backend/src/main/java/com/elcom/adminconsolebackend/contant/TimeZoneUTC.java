package com.elcom.adminconsolebackend.contant;

import java.util.HashMap;
import java.util.Map;

public final class TimeZoneUTC {

    public static final String UTC_MINUS_12 = "UTC-12";
    public static final String UTC_MINUS_11 = "UTC-11";
    public static final String UTC_MINUS_10 = "UTC-10";
    public static final String UTC_MINUS_9_30 = "UTC-09:30";
    public static final String UTC_MINUS_9 = "UTC-09";
    public static final String UTC_MINUS_8 = "UTC-08";
    public static final String UTC_MINUS_7 = "UTC-07";
    public static final String UTC_MINUS_6 = "UTC-06";
    public static final String UTC_MINUS_5 = "UTC-05";
    public static final String UTC_MINUS_4 = "UTC-04";
    public static final String UTC_MINUS_3_30 = "UTC-03:30";
    public static final String UTC_MINUS_3 = "UTC-03";
    public static final String UTC_MINUS_2 = "UTC-02";
    public static final String UTC_MINUS_1 = "UTC-01";
    public static final String UTC_0 = "UTC";
    public static final String UTC_1 = "UTC+01";
    public static final String UTC_2 = "UTC+02";
    public static final String UTC_3 = "UTC+03";
    public static final String UTC_3_30 = "UTC+03:30";
    public static final String UTC_4 = "UTC+04";
    public static final String UTC_4_30 = "UTC+04:30";
    public static final String UTC_5 = "UTC+05";
    public static final String UTC_5_30 = "UTC+05:30";
    public static final String UTC_5_45 = "UTC+05:45";
    public static final String UTC_6 = "UTC+06";
    public static final String UTC_6_30 = "UTC+06:30";
    public static final String UTC_7 = "UTC+07";
    public static final String UTC_8 = "UTC+08";
    public static final String UTC_8_45 = "UTC+08:45";
    public static final String UTC_9 = "UTC+09";
    public static final String UTC_9_30 = "UTC+09:30";
    public static final String UTC_10 = "UTC+10";
    public static final String UTC_10_30 = "UTC+10:30";
    public static final String UTC_11 = "UTC+11";
    public static final String UTC_12 = "UTC+12";
    public static final String UTC_12_45 = "UTC+12:45";
    public static final String UTC_13 = "UTC+13";
    public static final String UTC_14 = "UTC+14";

    public static final Map<Integer, String> mapTimeZoneUTC = new HashMap<>();

    static {
        mapTimeZoneUTC.put(-12, UTC_MINUS_12);
        mapTimeZoneUTC.put(-11, UTC_MINUS_11);
        mapTimeZoneUTC.put(-10, UTC_MINUS_10);
        mapTimeZoneUTC.put(-9, UTC_MINUS_9);
        mapTimeZoneUTC.put(-8, UTC_MINUS_8);
        mapTimeZoneUTC.put(-7, UTC_MINUS_7);
        mapTimeZoneUTC.put(-6, UTC_MINUS_6);
        mapTimeZoneUTC.put(-5, UTC_MINUS_5);
        mapTimeZoneUTC.put(-4, UTC_MINUS_4);
        mapTimeZoneUTC.put(-3, UTC_MINUS_3);
        mapTimeZoneUTC.put(-2, UTC_MINUS_2);
        mapTimeZoneUTC.put(-1, UTC_MINUS_1);
        mapTimeZoneUTC.put(0, UTC_0);
        mapTimeZoneUTC.put(1, UTC_1);
        mapTimeZoneUTC.put(2, UTC_2);
        mapTimeZoneUTC.put(3, UTC_3);
        mapTimeZoneUTC.put(4, UTC_4);
        mapTimeZoneUTC.put(5, UTC_5);
        mapTimeZoneUTC.put(6, UTC_6);
        mapTimeZoneUTC.put(7, UTC_7);
        mapTimeZoneUTC.put(8, UTC_8);
        mapTimeZoneUTC.put(9, UTC_9);
        mapTimeZoneUTC.put(10, UTC_10);
        mapTimeZoneUTC.put(11, UTC_11);
        mapTimeZoneUTC.put(12, UTC_12);
        mapTimeZoneUTC.put(13, UTC_13);
        mapTimeZoneUTC.put(14, UTC_14);
    }

}

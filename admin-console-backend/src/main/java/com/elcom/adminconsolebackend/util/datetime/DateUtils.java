package com.elcom.adminconsolebackend.util.datetime;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@Component
public class DateUtils {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    public static final SimpleDateFormat DATE_FORMAT_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

    private static final DateTimeFormatter NEW_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    private static final DateFormat OLD_DATE_TIME_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
    private static ZoneId DEFAULT_TIME_ZONE;
    private static final long UTC_ADD_7 = 7L * 60L * 60L * 1000L; // VietNam UTC+7

    @Value("${app.timezone}")
    private String systemTimezone;

    public static int GLOBAL_GMT;
    public static Long TIME_SECOND;

    @PostConstruct
    public void init() {
        DEFAULT_TIME_ZONE = ZoneId.of(systemTimezone);
        TimeZone tz = TimeZone.getTimeZone(DEFAULT_TIME_ZONE);
        OLD_DATE_TIME_FORMATTER.setTimeZone(tz);
        DATE_FORMAT_YYYY_MM_DD.setTimeZone(tz);
        NEW_DATE_TIME_FORMATTER.withZone(tz.toZoneId());
        DTF.withZone(tz.toZoneId());
        GLOBAL_GMT = getTimeZoneGMT();
        TIME_SECOND = GLOBAL_GMT * 60L * 60L;
    }

//    static {
//        TimeZone defautlTimeZone = TimeZone.getTimeZone(DEFAULT_TIME_ZONE);
//        OLD_DATE_TIME_FORMATTER.setTimeZone(defautlTimeZone);
//        OLD_DATE_TIME_FORMATTER.setTimeZone(defautlTimeZone);
//    }


    public int getTimeZoneGMT() {

        ZoneId zoneId = ZoneId.of(systemTimezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZoneOffset offset = now.getOffset();
        return offset.getTotalSeconds() / 3600;

    }

    public static String getDateAndTimeZoneByCoordinates(double latitude, double longitude) {
        try {
            TimeZone timeZone = TimeZone.getTimeZone(
                    TimezoneMapper.latLngToTimezoneString(latitude, longitude)
            );
            
            String utc = ((timeZone.getRawOffset() + timeZone.getDSTSavings()) / 1000L) / (60L * 60L) + "";
            if( !utc.contains("-") )
                utc = "+" + utc;
            
            return format(new Date(( System.currentTimeMillis() - UTC_ADD_7 ) + ( timeZone.getRawOffset() + timeZone.getDSTSavings() ))) + " (UTC" + utc + ")";
            
        } catch (Exception e) {
            log.error("ex: ", e);
        }
        return null;
    }

    public static LocalDateTime parse(String str) {
        if (!StringUtils.hasLength(str)) return null;
        return LocalDateTime.parse(str, NEW_DATE_TIME_FORMATTER);
    }

    public static Date parseDate(String str) {
        if (!StringUtils.hasLength(str)) return null;
        try {
            return OLD_DATE_TIME_FORMATTER.parse(str);
        } catch (ParseException e) {
            log.error("Can't parse sequence {} into {}", str, Date.class.getName());
            return null;
        }
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(NEW_DATE_TIME_FORMATTER) : "";
    }

    public static String format(Date dateTime) {
        return dateTime != null ? OLD_DATE_TIME_FORMATTER.format(dateTime) : "";
    }

    public static String format(Date dateTime, String pattern, TimeZone zone) {
        if (dateTime != null) {
            if (pattern == null) {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }

            DateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setTimeZone(zone);
            return formatter.format(dateTime);
        } else {
            return "";
        }
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime != null) {
            return formatter == null ? dateTime.format(NEW_DATE_TIME_FORMATTER) : dateTime.format(formatter);
        } else {
            return "";
        }
    }

    public static String format(LocalDate dateTime, DateTimeFormatter formatter) {
        if (dateTime != null) {
            return formatter == null ? dateTime.format(DateTimeFormatter.ISO_DATE) : dateTime.format(formatter);
        } else {
            return "";
        }
    }

    public static LocalDateTime getDateFromLong(long timestamp) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.of("+07:00"));
        } catch (DateTimeException var3) {
            return null;
        }
    }

    public static LocalDateTime getDateFromLongTime(String timestamp) {
        try {
            long epochSecond = (long) Double.parseDouble(timestamp);
            Instant instant = Instant.ofEpochSecond(epochSecond);
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneId);
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            return dateTime.format(formatter);
            return dateTime;
        } catch (Exception e) {
            return null;
        }
    }
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public static String convertDateStringBetweenTimeZone(String dateStrInput, String sourceTimeZone, String destTimeZone) {
        try {
            // Parse the date-time string to LocalDateTime
            LocalDateTime localDateTime = LocalDateTime.parse(dateStrInput, DTF);

            // Assume the original time is in UTC+07
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(sourceTimeZone));

            // Convert to UTC+00 (GMT)
            ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(destTimeZone));

            // Format the result as a string
            return utcDateTime.format(DTF);

        } catch (Exception e) {
            log.error("ex: ", e);
        }
        return null;
    }

    public static Long getLongFromDateTime(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    public static Long getLongFromDateTimeLocal(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_TIME_ZONE).toEpochSecond();
    }

    public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert == null ? null : dateToConvert.toInstant().atZone(DEFAULT_TIME_ZONE).toLocalDateTime();
    }

    public static LocalDateTime convertToLocalDateTimeViaMilisecond(Date dateToConvert) {
        return dateToConvert == null ? null : Instant.ofEpochMilli(dateToConvert.getTime()).atZone(DEFAULT_TIME_ZONE).toLocalDateTime();
    }

    /*public static void main(String[] args) {
        // 21.88779186564582, 105.10598517830537 VN
        // 20.337557786830963, 101.48695853256464 Laos
        // 29.58714692284418, 29.33222637048704 Egypts
        // 52.52, 13.40 Germany
        // 61.351099308528354, -112.31148880497446 Canada
        // 48.05690627059085, -0.613865300349616 France
        // -7.584242295669291, -53.270055876937604 Brazil
        // 24.351355641559866, -103.72219757771242 Mexico
        
        System.out.println(getDateAndTimeZoneByCoordinates(21.88779186564582, 105.10598517830537));
        System.out.println(getDateAndTimeZoneByCoordinates(20.337557786830963, 101.48695853256464));
        System.out.println(getDateAndTimeZoneByCoordinates(29.58714692284418, 29.33222637048704));
        System.out.println(getDateAndTimeZoneByCoordinates(52.52, 13.40));
        System.out.println(getDateAndTimeZoneByCoordinates(24.351355641559866, -103.72219757771242));
        System.out.println(getDateAndTimeZoneByCoordinates(0.26470960315239084, -0.11000482613992431));
    }*/
    
    /*public static void main(String[] args) {
        
        // 21.88779186564582, 105.10598517830537 VN
        // 20.337557786830963, 101.48695853256464 Laos
        // 29.58714692284418, 29.33222637048704 Egypts
        // 52.52, 13.40 Germany
        // 61.351099308528354, -112.31148880497446 Canada
        // 48.05690627059085, -0.613865300349616 France
        // -7.584242295669291, -53.270055876937604 Brazil
        // 24.351355641559866, -103.72219757771242 Mexico
        // 0.26470960315239084, -0.11000482613992431 UTC+0
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        String tz = TimezoneMapper.latLngToTimezoneString(0.26470960315239084, -0.11000482613992431);
        stopwatch.stop();
        System.out.println("tz -> " + tz);
        
        System.out.println("taked {} " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        
        java.util.TimeZone timeZone = java.util.TimeZone.getTimeZone(tz);
        System.out.println("getDisplayName -> " + timeZone.getDisplayName());
        System.out.println("getRawOffset   -> " + timeZone.getRawOffset());
        System.out.println("useDaylightTime   -> " + timeZone.useDaylightTime());
        System.out.println("getDSTSavings   -> " + timeZone.getDSTSavings());
        System.out.println("toZoneId       -> " + timeZone.toZoneId().toString());
        System.out.println("VN time now: " + new Date());
        
        final long UTC_ADD_7 = 7L * 60L * 60L * 1000L; // VietNam UTC+7
        long currentTime = ( System.currentTimeMillis() - UTC_ADD_7 ) + ( timeZone.getRawOffset() + timeZone.getDSTSavings() );
        System.out.println("local time: " + new Date(currentTime));
        
        String utc = ((timeZone.getRawOffset() + timeZone.getDSTSavings()) / 1000L) / (60L * 60L) + "";
        if( !utc.contains("-") )
            utc = "+" + utc;
        System.out.println("UTC: " + utc );
    }*/
}

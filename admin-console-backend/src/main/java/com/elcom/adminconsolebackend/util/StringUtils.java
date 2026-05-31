package com.elcom.adminconsolebackend.util;

import lombok.experimental.UtilityClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ParsingUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@UtilityClass
public class StringUtils {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static final String EMPTY = "";

    public static String toSnackCase(String camelCase) {
        return ParsingUtils.reconcatenateCamelCase(camelCase, "_");
    }

    public static boolean convertableToInt(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidIpAddress(String ip) {
        if (ip == null)
            return false;
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    public static String printException(Exception ex) {
        return ex.getCause() != null ? ex.getCause().toString() : ex.toString();
    }

    public static Long stringToLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }

    public static Integer stringToInteger(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }

    public static String objectToString(Object input) {
        if (input == null)
            return null;
        try {
            return ((String) input).trim();
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }

    public static UUID objectToUUID(Object input) {
        if (input == null)
            return null;
        try {
            return UUID.fromString(input.toString());
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }

    public static Integer objectToInteger(Object input) {
        try {

            if (input instanceof String)
                return Integer.parseInt((String) input);

            return (Integer) input;

        } catch (Exception ex) {
            LOGGER.warn(StringUtils.printException(ex));
        }
        return null;
    }

    public static Long objectToLong(Object input) {
        try {
            if (input instanceof String)
                return Long.parseLong((String) input);
            return (Long) input;
        } catch (Exception ex) {
            LOGGER.warn(StringUtils.printException(ex));
        }
        return null;
    }

    public static Map<String, String> getUrlParamValues(String url) {
        Map<String, String> paramsMap = new HashMap<>();
        String params[] = url.split("&");
        String[] temp;
        for (String param : params) {
            temp = param.split("=");
            try {
                //paramsMap.put(temp[0], java.net.URLDecoder.decode(temp[1], "UTF-8"));
                paramsMap.put(temp[0], temp.length > 1 ? java.net.URLDecoder.decode(temp[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return paramsMap;
    }

//    public static String generateRandomString(int num) {
//        return RandomStringUtils.randomAlphanumeric(num);
//    }

    public static String putArrayStringIntoParameter(String input) {

        if (isNullOrEmpty(input)) {
            return "";
        }

        String output = input.substring(0, input.length() - 1).replaceAll(",", "','");

        return "('" + output + "')";
    }

    public static String getComputerName() {
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (Throwable ex) {
            try {
                Map<String, String> env = System.getenv();
                if (env.containsKey("COMPUTERNAME")) {
                    return env.get("COMPUTERNAME");
                } else if (env.containsKey("HOSTNAME")) {
                    return env.get("HOSTNAME");
                } else {
                    return "Unknown";
                }
            } catch (Exception e) {
                return "Unknown";
            }
        }
    }

    public static boolean equalsIgnoreCase(String input, String input1) {
        if (input == null && input1 == null) {
            return true;
        }

        if (input == null && input1 != null) {
            return false;
        }

        if (input != null && input1 == null) {
            return false;
        }

        if (input.equalsIgnoreCase(input1)) {
            return true;
        }

        return false;
    }

    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty() || "NULL".equalsIgnoreCase(input.trim());
    }

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }

    public static boolean isNumeric(String s) {
        if (isNullOrEmpty(s))
            return false;

        return s.trim().matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean isNumericAcceptZero(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.trim().matches("-?\\d*\\.?\\d+");
    }

    public static boolean isDigit(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.matches("\\d+");
    }

    public static String consolidate(String s) {
        if (isNullOrEmpty(s)) {
            return EMPTY;
        } else {
            s = s.trim();
            return s;
        }
    }

    public static String toLiteral(String str) {
        if (str == null || str.isEmpty() || str.trim().isEmpty()) {
            return "''";
        } else {
            return "'" + str + "'";
        }
    }

    public static String consolidate(String s, String outValue) {
        if (isNullOrEmpty(s)) {
            return outValue;
        } else {
            s = s.trim();
            return s;
        }
    }

    public static boolean isNotContainSpecialCharator(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.matches("^[a-zA-Z0-9]*$");
    }

    public static Integer toInt(String s) throws Exception {
        if (isNullOrEmpty(s)) {
            throw new Exception("Input is required.");
        }

        try {
            return Integer.valueOf(s.trim());
        } catch (Exception ex) {
            throw new Exception("Input is invalid format.");
        }
    }

    public static Long toLong(String input) {
        if (isNullOrEmpty(input) || !isNumeric(input)) {
            return null;
        }
        return Long.parseLong(input);
    }

    public static int intFromString(String input) {
        if (isNullOrEmpty(input) || !isNumberic(input)) {
            return 0;
        }
        return Integer.parseInt(input);
    }

    public static String currencyFormat(String input) {
        double myNum = Double.parseDouble(input);
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        try {
            return nf.format(myNum).replace("$", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String ConvertFromFloatingPointToInt(String disbursementAmount) {

        BigDecimal bd = new BigDecimal(disbursementAmount);
        bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd.stripTrailingZeros().toPlainString();

    }

    public static boolean validLength(String field, int maxLength) {
        if (!isNullOrEmpty(field) && field.length() > maxLength) {
            return false;
        }
        return true;
    }

    public static String ConvertFromFloatingPoint(String disbursementAmount,
                                                  int scale) {

        BigDecimal bd = new BigDecimal(disbursementAmount);
        bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return bd.stripTrailingZeros().toPlainString();

    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static BigDecimal toBigDecimal(String input) {
        return toBigDecimal(input, 0, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal toBigDecimal(String input, int roundingMode) {
        return toBigDecimal(input, 0, roundingMode);
    }

    public static BigDecimal toBigDecimal(String input, int scale,
                                          int roundingMode) {
        BigDecimal output = new BigDecimal(input);
        output.setScale(scale, roundingMode);
        return output;
    }

    public static String nullToEmpty(Object input) {
        return (input == null ? "" : ("null".equals(input) ? "" : input
                .toString().trim()));
    }

    public static boolean isNumberic(String sNumber) {
        if (sNumber == null || "".equals(sNumber)) {
            return false;
        }
        char ch_max = (char) 0x39;
        char ch_min = (char) 0x30;

        for (int i = 0; i < sNumber.length(); i++) {
            char ch = sNumber.charAt(i);
            if ((ch < ch_min) || (ch > ch_max)) {
                return false;
            }
        }
        return true;
    }


    public static int extractValueAfterZero(String input) {
        int numericValue = Integer.parseInt(input.replaceAll("[^0-9]", ""));
        return numericValue;
    }

    public static Converter<String, String> getNullConverter() {
        return new Converter<>() {
            @Override
            public String convert(MappingContext<String, String> context) {
                String sourceValue = context.getSource();
                return org.springframework.util.StringUtils.hasText(sourceValue) ? sourceValue : null;
            }
        };
    }

    public static String getValueJson(String value) {
        try {
            if(!isNullOrEmpty(value)) {
                JSONObject jsonObject = new JSONObject(value);
                if (jsonObject.has("value")) {
                    Object valueObj = jsonObject.get("value");
                    if (valueObj instanceof Integer) {
                        return String.valueOf(valueObj); // Return Integer as String
                    } else if (valueObj instanceof String) {
                        return (String) valueObj; // Return String
                    } else if (valueObj instanceof JSONArray) {
                        return (String) ((JSONArray) valueObj).get(0);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> convertLongListToStringList(List<Long> longList) {
        return longList.stream()
                .map(String::valueOf) // Convert each Long to String
                .collect(Collectors.toList()); // Collect as List<String>
    }

    public static String splitData(List<String> data) {
        if(!data.isEmpty()) {
            String result = data.stream()
                    .collect(Collectors.joining(","));
            return result;
        }
        return null;
    }

    public static String splitDataWithSpace(List<String> data) {
        if(!data.isEmpty()) {
            String result = data.stream()
                    .collect(Collectors.joining(", "));
            return result;
        }
        return null;
    }

    public static List<String> convertStringToList(String data) {
        if (!org.springframework.util.StringUtils.hasText(data)) {
            return new ArrayList<>();
        }
        return Arrays.stream(data.substring(1, data.length() - 1).split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

}

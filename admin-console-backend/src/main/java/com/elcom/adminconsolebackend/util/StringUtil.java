package com.elcom.adminconsolebackend.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
    
    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static String Empty = "";

    public static boolean isValidIpAddress(String ip) {
        if( ip == null )
            return false;
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }
    
    public static String printException(Exception ex) {
        return ex.getCause()!=null ? ex.getCause().toString() : ex.toString();
    }
    
    public static Long stringToLong(String input) {
        try {
            return Long.parseLong(input);
        }catch(NumberFormatException ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
    
    public static Integer stringToInteger(String input) {
        try {
            return Integer.parseInt(input);
        }catch(NumberFormatException ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
    
    public static String objectToString(Object input) {
        if( input == null )
            return null;
        try {
            return ((String) input).trim();
        }catch(Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }

    public static UUID objectToUUID(Object input){
        if( input == null )
            return null;
        try {
            return UUID.fromString(input.toString());
        }catch(Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
    
    public static Integer objectToInteger(Object input) {
        try {
            
            if( input instanceof String )
                return Integer.parseInt((String) input);
            
            return (Integer) input;
            
        }catch(Exception ex) {
            LOGGER.warn(StringUtil.printException(ex));
        }
        return null;
    }
    
    public static Long objectToLong(Object input) {
        try {
            if( input instanceof String )
                return Long.parseLong((String) input);
            return (Long) input;
        }catch(Exception ex) {
            LOGGER.warn(StringUtil.printException(ex));
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
                Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return paramsMap;
    }

    public static String generateRandomString(int num) {
        return RandomStringUtils.randomAlphanumeric(num);
    }

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

        return input == null || input.trim().isEmpty();
    }

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }

    public static boolean isNumeric(String s) {
        if (isNullOrEmpty(s))
            return false;

        return s.trim().matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean isDigit(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.matches("\\d+");
    }

    public static String consolidate(String s) {
        if (isNullOrEmpty(s)) {
            return Empty;
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

    public static String toCurrency(String amount) {
        return String.format("%,.0f", Double.valueOf(amount));
    }

    public static String nullToEmpty(Object input) {
        return (input == null ? "" : ("null".equals(input) ? "" : input
                .toString()));
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

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    public static boolean validateEmail(String emailStr) {
        if (isNullOrEmpty(emailStr)) {
            return false;
        }
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean checkMobilePhoneNumber(String number) {
        if (number == null) {
            return false;
        }

        boolean result = false;

        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(number);

        if (matcher.matches() && (number.length() == 10 || number.length() == 11)) {
            number = number.substring(0, 2);
            if (number.equals("01") || number.equals("02") || number.equals("08") || number.equals("09")) {
                result = true;
            }
        }

        return result;
    }

    public static boolean checkMobilePhoneNumberNew(String number) {
        if (isNullOrEmpty(number)) {
            return false;
        }
        if(number.startsWith("+84")) number = number.replace("+84", "0");
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(number);

        if (matcher.matches() && (number.length() == 10)) {
            if ("09".equals(number.substring(0, 2)) || Arrays.asList(new String[]{"032", "033", "034", "035", "036", "037", "038", "039", "052", "056", "058", "059", "070", "076", "077", "078", "079", "081", "082", "083", "084", "085", "086", "088", "089"}).contains(number.substring(0, 3))) {
                return true;
            }
        }

        return false;
    }

    public static boolean validIpV4(String ip) {
        return ip.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }
    
    public static boolean isUUID(String string) {
        if ( isNullOrEmpty(string) )
            return false;
        
        try {
            UUID.fromString(string.trim());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean validateBirthDay(String birthDay, String dateFormat) {
        if (isNullOrEmpty(birthDay)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(birthDay);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

//    public static void main(String[] args) {
//        //Check number
//        String input1 = "123";
//        LOGGER.info(isDigit(input1) + "|" + isNumberic(input1) + "|" + isNumeric(input1));
//        
//        String input2 = "123.1";
//        LOGGER.info(isDigit(input2) + "|" + isNumberic(input2) + "|" + isNumeric(input2));
//        
//        String input3 = "-123.1";
//        LOGGER.info(isDigit(input3) + "|" + isNumberic(input3) + "|" + isNumeric(input3));
//    }
}

package org.xmuyoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class Utils {

    public static final ZoneOffset ZONE_OFFSET_8_HOURS = ZoneOffset.ofHours(8);
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final ZoneOffset ZONE_OFFSET_0_HOURS = ZoneOffset.ofHours(0);
    public static final HashFunction MURMUR3 = Hashing.murmur3_128();

    private static final Joiner COMMA_JOINER = Joiner.on(",");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter HOUR_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("H:m");
    private static final long ONE_DAY_MILLIS = 86400 * 1000;

    public static String commaJoin(List<String> items) {
        if (null == items)
            return null;

        return COMMA_JOINER.join(items);
    }

    public static String randomId() {
        return MURMUR3.hashBytes(UUID.randomUUID().toString().getBytes()).toString();
    }

    public static long toDayInMillis(long milliseconds) {
        return milliseconds / ONE_DAY_MILLIS * ONE_DAY_MILLIS;
    }

    public static LocalDateTime toLocalDateTime(String datetimeStr) {
        return LocalDateTime.parse(datetimeStr, DATETIME_FORMATTER);
    }

    public static LocalTime toLocalTime(String timeStr) {
        return LocalTime.parse(timeStr, HOUR_TIME_FORMATTER);
    }
}

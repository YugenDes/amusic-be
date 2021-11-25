package it.polimi.amusic.utils;

import com.google.cloud.Timestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class TimestampUtils {

    public static com.google.cloud.Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTimeToConvert) {
        if (Objects.nonNull(localDateTimeToConvert)) {
            return Timestamp.of(java.sql.Timestamp.valueOf(localDateTimeToConvert));
        } else {
            return null;
        }
    }

    public static com.google.cloud.Timestamp convertLocalDateToTimestamp(LocalDate localDateToConvert) {
        if (Objects.nonNull(localDateToConvert)) {
            return Timestamp.of(java.sql.Timestamp.valueOf(localDateToConvert.atTime(LocalTime.MIDNIGHT)));
        } else {
            return null;
        }
    }

    public static LocalDate convertTimestampToLocalDate(Timestamp timestamp) {
        if (Objects.nonNull(timestamp)) {
            return timestamp.toSqlTimestamp().toLocalDateTime().toLocalDate();
        } else {
            return null;
        }
    }

    public static LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
        if (Objects.nonNull(timestamp)) {
            return timestamp.toSqlTimestamp().toLocalDateTime();
        } else {
            return null;
        }
    }


}

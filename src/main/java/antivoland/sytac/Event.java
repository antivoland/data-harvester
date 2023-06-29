package antivoland.sytac;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Builder
@Jacksonized
@EqualsAndHashCode
class Event {
    final static String STREAM_STARTED = "stream-started";
    final static String STREAM_FINISHED = "stream-finished";
    final static String SHOW_LIKED = "show-liked";
    final static String STREAM_INTERRUPTED = "stream-interrupted";

    private static final ZoneId CET_TIMEZONE = ZoneId.of("UTC+1");

    static boolean isSuccessfulStreamingEvent(Event a, Event b) {
        if (a == null || b == null) return false;
        if (a.payload == null || b.payload == null) return false;
        return Objects.equals(a.payload.user.id, b.payload.user.id)
                && Objects.equals(a.platform, b.platform)
                && Event.STREAM_STARTED.equals(a.name)
                && Event.STREAM_FINISHED.equals(b.name)
                && Objects.equals(a.payload.show.show_id, b.payload.show.show_id);
    }

    String id;
    String name;
    String platform;
    Payload payload;

    boolean isSytacUser() {
        return payload != null && payload.isSytacUser();
    }

    @Builder
    @Jacksonized
    @EqualsAndHashCode
    static class Payload {
        Show show;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss.SSS")
        LocalDateTime event_date;
        User user;

        boolean isSytacUser() {
            return user != null && user.isSytac();
        }

        LocalDateTime cetDatetime() {
            return timestamp().atZone(CET_TIMEZONE).toLocalDateTime();
        }

        Instant timestamp() {
            return event_date.atZone(timezone()).toInstant();
        }

        ZoneId timezone() {
            return switch (user.country) {
                case "PT" -> ZoneId.of("UTC");
                case "CA" -> ZoneId.of("UTC-5");
                case "US" -> ZoneId.of("UTC-8");
                case "RU" -> ZoneId.of("UTC+3");
                case "ID" -> ZoneId.of("UTC+7");
                case "CN" -> ZoneId.of("UTC+8");
                default -> CET_TIMEZONE;
            };
        }
    }

    @Builder
    @Jacksonized
    @EqualsAndHashCode
    static class Show {
        String show_id;
        String cast;
        String country;
        String date_added;
        String description;
        String director;
        String duration;
        String listed_in;
        String rating;
        Integer release_year;
        String title;
        String type;
        String platform;

        String cast1stName() {
            if (cast == null) return null;
            var idx = cast.indexOf(",");
            return idx > 0 ? cast.substring(0, idx) : cast;
        }
    }

    @Builder
    @Jacksonized
    @EqualsAndHashCode
    static class User {
        String id;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate date_of_birth;
        String email;
        String first_name;
        String last_name;
        String gender;
        String ip_address;
        String country;

        boolean isSytac() {
            return "Sytac".equals(first_name);
        }
    }
}
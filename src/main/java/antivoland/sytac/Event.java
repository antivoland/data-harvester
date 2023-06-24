package antivoland.sytac;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Data
@Builder
@Jacksonized
class Event {
    final static String STREAM_STARTED = "stream-started";
    final static String STREAM_FINISHED = "stream-finished";
    final static String SHOW_LIKED = "show-liked";
    final static String STREAM_INTERRUPTED = "stream-interrupted";

    private static final ZoneId AMSTERDAM_TIMEZONE = ZoneId.of("Europe/Amsterdam");

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

    @Data
    @Builder
    @Jacksonized
    static class Payload {
        Show show;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss.SSS")
        LocalDateTime event_date;
        User user;

        boolean isSytacUser() {
            return user != null && user.isSytac();
        }

        LocalDateTime amsterdamDatetime() {
            return timestamp().atZone(AMSTERDAM_TIMEZONE).toLocalDateTime();
        }

        Instant timestamp() {
            return event_date.atZone(timezone()).toInstant();
        }

        ZoneId timezone() {
            return switch (user.country) {
                case "PT" -> ZoneId.of("UTC");
                case "CA" -> ZoneId.of("America/Toronto");
                case "US" -> ZoneId.of("America/Los_Angeles");
                case "RU" -> ZoneId.of("Europe/Moscow");
                case "ID" -> ZoneId.of("Asia/Jakarta");
                case "CN" -> ZoneId.of("Asia/Shanghai");
                default -> AMSTERDAM_TIMEZONE;
            };
        }
    }

    @Data
    @Builder
    @Jacksonized
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

    @Data
    @Builder
    @Jacksonized
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
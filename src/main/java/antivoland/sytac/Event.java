package antivoland.sytac;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
class Event {
    final static String STREAM_STARTED = "stream-started";
    final static String STREAM_ENDED = "stream-ended";
    final static String SHOW_LIKED = "show-liked";
    final static String STREAM_INTERRUPTED = "stream-interrupted";

    String id;
    String name;
    String platform;
    Payload payload;

    @Data
    @Builder
    @Jacksonized
    static class Payload {
        Show show;
        String event_date;
        User user;

        boolean isSytacUser() {
            return user != null && user.isSytac();
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
        String release_year;
        String title;
        String type;
        String platform;
    }

    @Data
    @Builder
    @Jacksonized
    static class User {
        String id;
        String date_of_birth;
        String email;
        String first_name;
        String gender;
        String ip_address;
        String country;
        String last_name;

        boolean isSytac() {
            return "Sytac".equals(first_name);
        }
    }
}
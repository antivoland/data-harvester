package antivoland.sytac;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class Summary {
    User user;
    List<Event> events;

    @Data
    @Builder
    static class User {
        String id;
        String first_name;
        String last_name;
        String age;
    }

    @Data
    @Builder
    static class Event {
        String id;
        String name;
        String date;
        String show_id;
        String show_title;
        String show_cast_1st;
        String platform;
    }
}
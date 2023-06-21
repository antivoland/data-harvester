package antivoland.sytac;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class AggregatedView {
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    final Map<String, User> users = new HashMap<>();
    long showsReleasedIn2020OrLater;
    long runtimeDurationMillis;
    long successfulStreamingEvents;
    double percentageOfStartedStreamEvents;

    @SneakyThrows
    synchronized void print() {
        System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    }

    synchronized void register(antivoland.sytac.Event event) {
        if (event.payload == null) return;
        var user = registerUser(event.payload.user);
        user.registerEvent(event);
        if (event.payload.show.release_year >= 2020) {
            ++showsReleasedIn2020OrLater; // TODO: the condition in unclear
        }
        // TODO: successfulStreamingEvents, percentageOfStartedStreamEvents
    }

    private User registerUser(antivoland.sytac.Event.User user) {
        var view = users.get(user.id);
        if (view != null) return view;
        view = new User()
                .setId(user.id)
                .setName(user.first_name + " " + user.last_name)
                .setAge(Period.between(user.date_of_birth, LocalDate.now()).getYears());
        users.put(view.id, view);
        return view;
    }

    @Data
    @NoArgsConstructor
    static class User {
        String id;
        String name;
        int age;
        final Map<String, Event> events = new HashMap<>();

        private void registerEvent(antivoland.sytac.Event event) {
            var view = events.get(event.id);
            if (view != null) return;
            view = new Event()
                    .setId(event.id)
                    .setName(event.name)
                    .setPlatform(event.platform)
                    .setShowTitle(event.payload.show.title)
                    .setShowCast1stName(event.payload.show.cast1stName())
                    .setShowId(event.payload.show.show_id)
                    .setAmsterdamDatetime(event.payload.amsterdamDatetime());
            events.put(view.id, view);
        }
    }

    @Data
    @NoArgsConstructor
    static class Event {
        String id;
        String name;
        String platform;
        String showTitle;
        String showCast1stName;
        String showId;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss.SSS")
        LocalDateTime amsterdamDatetime;
    }
}
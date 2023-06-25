package antivoland.sytac;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Data
@NoArgsConstructor
public class AggregatedView {
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    final Map<String, User> users = new HashMap<>();
    @JsonIgnore
    private final Set<String> showsReleasedIn2020OrLater = new HashSet<>();
    long runtimeDurationMillis;
    @JsonIgnore
    private long sytflixStartedStreamEvents;
    @JsonIgnore
    private long sytflixTotalEvents;

    @JsonProperty
    double getSytflixPercentageOfStartedStreamEvents() {
        return (double) sytflixStartedStreamEvents / sytflixTotalEvents;
    }

    @JsonProperty
    public long getShowsReleasedIn2020OrLater() {
        return showsReleasedIn2020OrLater.size();
    }

    @SneakyThrows
    synchronized void print() {
        System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    }

    synchronized void registerEvent(antivoland.sytac.Event event) {
        if (event.payload == null) return;
        var user = registerUser(event.payload.user);
        user.registerEvent(event);
        if (event.payload.show.release_year >= 2020) {
            showsReleasedIn2020OrLater.add(event.payload.show.show_id);
        }
        if ("sytflix".equals(event.platform)) {
            if (antivoland.sytac.Event.STREAM_STARTED.equals(event.name)) ++sytflixStartedStreamEvents;
            ++sytflixTotalEvents;
        }
    }

    synchronized void incrementSuccessfulStreamingEvents(antivoland.sytac.Event.User user) {
        var view = registerUser(user);
        ++view.successfulStreamingEvents;
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
        final List<Event> events = new ArrayList<>();
        long successfulStreamingEvents;

        private void registerEvent(antivoland.sytac.Event event) {
            events.add(new Event()
                    .setId(event.id)
                    .setName(event.name)
                    .setPlatform(event.platform)
                    .setShowTitle(event.payload.show.title)
                    .setShowCast1stName(event.payload.show.cast1stName())
                    .setShowId(event.payload.show.show_id)
                    .setCetDatetime(event.payload.cetDatetime()));
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
        LocalDateTime cetDatetime;
    }
}
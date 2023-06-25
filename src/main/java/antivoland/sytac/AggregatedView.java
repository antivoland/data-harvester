package antivoland.sytac;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

public class AggregatedView {
    private static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @JsonProperty
    final Map<String, User> users = new HashMap<>();
    @JsonProperty
    long runtimeDurationMillis;
    private long sytflixStartedStreamEvents;
    private long sytflixTotalEvents;
    private final Set<String> showsReleasedIn2020OrLater = new HashSet<>();

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
        view = User.builder()
                .id(user.id)
                .name(user.first_name + " " + user.last_name)
                .age(Period.between(user.date_of_birth, LocalDate.now()).getYears())
                .build();
        users.put(view.id, view);
        return view;
    }

    @Builder
    @EqualsAndHashCode
    static class User {
        @JsonProperty
        String id;
        @JsonProperty
        String name;
        @JsonProperty
        int age;
        @JsonProperty
        final List<Event> events = new ArrayList<>();
        @JsonProperty
        long successfulStreamingEvents;

        private void registerEvent(antivoland.sytac.Event event) {
            events.add(Event.builder()
                    .id(event.id)
                    .name(event.name)
                    .platform(event.platform)
                    .showTitle(event.payload.show.title)
                    .showCast1stName(event.payload.show.cast1stName())
                    .showId(event.payload.show.show_id)
                    .cetDatetime(event.payload.cetDatetime())
                    .build());
        }
    }

    @Builder
    @EqualsAndHashCode
    static class Event {
        @JsonProperty
        String id;
        @JsonProperty
        String name;
        @JsonProperty
        String platform;
        @JsonProperty
        String showTitle;
        @JsonProperty
        String showCast1stName;
        @JsonProperty
        String showId;
        @JsonProperty
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss.SSS")
        LocalDateTime cetDatetime;
    }
}
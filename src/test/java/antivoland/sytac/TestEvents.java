package antivoland.sytac;

import lombok.Builder;
import org.springframework.http.codec.ServerSentEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

class TestEvents {
    private static final String SSE_EVENT_DATETIME = "23-06-2023 12:34:56.789";
    private static final LocalDateTime EVENT_DATETIME = LocalDateTime.of(2023, 6, 23, 12, 34, 56, 789000000);

    @Builder(toBuilder = true)
    static class SseEventShow {
        String id;
        int release_year;
        String platform;
    }

    @Builder(toBuilder = true)
    static class SseEventUser {
        String id;
        String first_name;
        String country;
    }

    static ServerSentEvent<String> sseEvent(String id, String name, SseEventShow show, SseEventUser user) {
        return ServerSentEvent.
                <String>builder()
                .id(id)
                .event(name)
                .data(sseEventData(show, user))
                .build();

    }

    private static String sseEventData(SseEventShow show, SseEventUser user) {
        return String.format("""
                        {
                          "show": {
                            "show_id": "%s",
                            "cast": "👨, 👩",
                            "country": "HM",
                            "date_added": "June 24, 2023",
                            "description": "👨👩💑💍💏🚗💥👻👻",
                            "director": "👴",
                            "duration": "180 min",
                            "listed_in": "Dramedies",
                            "rating": "G",
                            "release_year": %s,
                            "title": "💥️",
                            "type": "Movie",
                            "platform": "%s"
                          },
                          "event_date": "%s",
                          "user": {
                            "id": "%s",
                            "date_of_birth": "01/01/1970",
                            "email": "%s",
                            "first_name": "%s",
                            "last_name": "%s",
                            "gender": "🤖",
                            "ip_address": "127.0.0.1",
                            "country": "%s"
                          }
                        }
                        """,
                show.id,
                show.release_year,
                show.platform,
                SSE_EVENT_DATETIME,
                user.id,
                user.id + "@test",
                user.first_name,
                user.id,
                user.country);
    }

    static Event event(String id, String name, String platform, Event.Show show, Event.User user) {
        return Event
                .builder()
                .id(id)
                .name(name)
                .platform(platform)
                .payload(eventPayload(show, user))
                .build();
    }

    private static Event.Payload eventPayload(Event.Show show, Event.User user) {
        return Event.Payload
                .builder()
                .show(show)
                .event_date(EVENT_DATETIME)
                .user(user)
                .build();
    }

    static Event.Show eventShow(String id, int releaseYear, String platform) {
        return Event.Show
                .builder()
                .show_id(id)
                .cast("👨, 👩")
                .country("HM")
                .date_added("June 24, 2023")
                .description("👨👩💑💍💏🚗💥👻👻")
                .director("👴")
                .duration("180 min")
                .listed_in("Dramedies")
                .rating("G")
                .release_year(releaseYear)
                .title("💥️")
                .type("Movie")
                .platform(platform)
                .build();
    }

    static Event.User eventUser(String id, String first_name, String country) {
        return Event.User
                .builder()
                .id(id)
                .date_of_birth(LocalDate.of(1970, 1, 1))
                .email(id + "@test")
                .first_name(first_name)
                .last_name(id)
                .gender("🤖")
                .ip_address("127.0.0.1")
                .country(country)
                .build();
    }
}
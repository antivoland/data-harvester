package antivoland.sytac;

import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {
    private static final String SSE_EVENT_DATETIME = "23-06-2023 12:34:56.789";
    private static final LocalDateTime EVENT_DATETIME = LocalDateTime.of(2023, 6, 23, 12, 34, 56, 789000000);

    @Test
    void test() {
        var mapper = new EventMapper("test");
        var event = mapper.mapEvent(sseEvent("HM"));
        assertThat(event).isEqualTo(Event
                .builder()
                .id("the-event")
                .name("the-name")
                .platform("test")
                .payload(Event.Payload
                        .builder()
                        .show(Event.Show
                                .builder()
                                .show_id("the-crash")
                                .cast("👨, 👩")
                                .country("HM")
                                .date_added("June 25, 2023")
                                .description("👨👩💑💍💏🚗💥👻👻")
                                .director("👴")
                                .duration("180 min")
                                .listed_in("Dramedies")
                                .rating("G")
                                .release_year(2023)
                                .title("💥️")
                                .type("Movie")
                                .platform("Whatever")
                                .build())
                        .event_date(LocalDateTime.of(2023, 6, 23, 12, 34, 56, 789000000))
                        .user(Event.User
                                .builder()
                                .id("bender")
                                .date_of_birth(LocalDate.of(2996, 9, 4))
                                .email("bender.rodriguez@momcorp.com")
                                .first_name("Bender")
                                .last_name("Rodriguez")
                                .gender("🤖")
                                .ip_address("127.0.0.1")
                                .country("HM")
                                .build())
                        .build())
                .build());
    }

    @Test
    void testTimezones() {
        var mapper = new EventMapper("test");
        assertThat(mapper.mapEvent(sseEvent("PT")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME.plusHours(0).plusHours(1)); // Europe/Lisbon (UTC)
        assertThat(mapper.mapEvent(sseEvent("CA")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME.plusHours(5).plusHours(1)); // America/Toronto (UTC-5)
        assertThat(mapper.mapEvent(sseEvent("US")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME.plusHours(8).plusHours(1)); // America/Los_Angeles (UTC-8)
        assertThat(mapper.mapEvent(sseEvent("RU")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME.minusHours(3).plusHours(1)); // Europe/Moscow (UTC+3)
        assertThat(mapper.mapEvent(sseEvent("ID")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME.minusHours(7).plusHours(1)); // Asia/Jakarta (UTC+7)
        assertThat(mapper.mapEvent(sseEvent("CN")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME.minusHours(8).plusHours(1)); // Asia/Shanghai (UTC+8)
        assertThat(mapper.mapEvent(sseEvent("HM")).payload.cetDatetime())
                .isEqualTo(EVENT_DATETIME);
    }

    private static ServerSentEvent<String> sseEvent(String userCountry) {
        return ServerSentEvent.
                <String>builder()
                .id("the-event")
                .event("the-name")
                .data(sseEventData(userCountry))
                .build();

    }

    private static String sseEventData(String userCountry) {
        return String.format("""
                {
                  "show": {
                    "show_id": "the-crash",
                    "cast": "👨, 👩",
                    "country": "HM",
                    "date_added": "June 25, 2023",
                    "description": "👨👩💑💍💏🚗💥👻👻",
                    "director": "👴",
                    "duration": "180 min",
                    "listed_in": "Dramedies",
                    "rating": "G",
                    "release_year": 2023,
                    "title": "💥️",
                    "type": "Movie",
                    "platform": "Whatever"
                  },
                  "event_date": "%s",
                  "user": {
                    "id": "bender",
                    "date_of_birth": "04/09/2996",
                    "email": "bender.rodriguez@momcorp.com",
                    "first_name": "Bender",
                    "last_name": "Rodriguez",
                    "gender": "🤖",
                    "ip_address": "127.0.0.1",
                    "country": "%s"
                  }
                }
                """, SSE_EVENT_DATETIME, userCountry);
    }
}
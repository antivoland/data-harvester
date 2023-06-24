package antivoland.sytac;

import org.springframework.http.codec.ServerSentEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static antivoland.sytac.TestEvents.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.StringUtils.capitalize;

public class TestEventSets {
    static final int SIZE = 10;

    static List<ServerSentEvent<String>> sseEvents(String platform) {
        final var no = new AtomicInteger();
        var show2018 = SseEventShow.builder().id("show2018").release_year(2018).platform(capitalize(platform)).build();
        var show2019 = SseEventShow.builder().id("show2019").release_year(2019).platform(capitalize(platform)).build();
        var show2020 = SseEventShow.builder().id("show2020").release_year(2020).platform(capitalize(platform)).build();
        var show2021 = SseEventShow.builder().id("show2021").release_year(2021).platform(capitalize(platform)).build();
        var john = SseEventUser.builder().id("john").first_name("John").country("HM").build();
        var william = SseEventUser.builder().id("william").first_name("William").country("HM").build();
        var sytac = SseEventUser.builder().id("sytac").first_name("Sytac").country("NL").build();
        var events = List.of(
                // Not a successful streaming event (show_liked in between)
                sseEvent(no.incrementAndGet(), Event.STREAM_STARTED, show2018, john),
                sseEvent(no.incrementAndGet(), Event.SHOW_LIKED, show2018, john),
                sseEvent(no.incrementAndGet(), Event.STREAM_FINISHED, show2018, john),
                // Not a successful streaming event (different shows)
                sseEvent(no.incrementAndGet(), Event.STREAM_STARTED, show2018, john),
                sseEvent(no.incrementAndGet(), Event.STREAM_FINISHED, show2019, john),
                // Not a successful streaming event (different users)
                sseEvent(no.incrementAndGet(), Event.STREAM_STARTED, show2020, john),
                sseEvent(no.incrementAndGet(), Event.STREAM_FINISHED, show2020, william),
                // Successful streaming event
                sseEvent(no.incrementAndGet(), Event.STREAM_STARTED, show2021, john),
                sseEvent(no.incrementAndGet(), Event.STREAM_FINISHED, show2021, john),
                // Sytac user
                sseEvent(no.incrementAndGet(), Event.STREAM_INTERRUPTED, show2021, sytac));
        assertThat(events).hasSize(SIZE);
        return events;
    }

    static List<Event> events(String platform) {
        final var no = new AtomicInteger();
        var show2018 = eventShow("show2018", 2018, capitalize(platform));
        var show2019 = eventShow("show2019", 2019, capitalize(platform));
        var show2020 = eventShow("show2020", 2020, capitalize(platform));
        var show2021 = eventShow("show2021", 2021, capitalize(platform));
        var john = eventUser("john", "John", "HM");
        var william = eventUser("william", "William", "HM");
        var sytac = eventUser("sytac", "Sytac", "NL");
        var events = List.of(
                // Not a successful streaming event (show_liked in between)
                event(no.incrementAndGet(), Event.STREAM_STARTED, platform, show2018, john),
                event(no.incrementAndGet(), Event.SHOW_LIKED, platform, show2018, john),
                event(no.incrementAndGet(), Event.STREAM_FINISHED, platform, show2018, john),
                // Not a successful streaming event (different shows)
                event(no.incrementAndGet(), Event.STREAM_STARTED, platform, show2018, john),
                event(no.incrementAndGet(), Event.STREAM_FINISHED, platform, show2019, john),
                // Not a successful streaming event (different users)
                event(no.incrementAndGet(), Event.STREAM_STARTED, platform, show2020, john),
                event(no.incrementAndGet(), Event.STREAM_FINISHED, platform, show2020, william),
                // Successful streaming event
                event(no.incrementAndGet(), Event.STREAM_STARTED, platform, show2021, john),
                event(no.incrementAndGet(), Event.STREAM_FINISHED, platform, show2021, john),
                // Sytac user
                event(no.incrementAndGet(), Event.STREAM_INTERRUPTED, platform, show2021, sytac));
        assertThat(events).hasSize(SIZE);
        return events;
    }
}
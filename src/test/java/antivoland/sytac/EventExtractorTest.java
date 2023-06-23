package antivoland.sytac;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@EnableAutoConfiguration
@SpringBootTest(classes = {EventExtractorTest.SseController.class}, webEnvironment = RANDOM_PORT)
class EventExtractorTest {
    @LocalServerPort
    int port;

    @Test
    void test() {
        final AtomicInteger cnt = new AtomicInteger();
        var clientFactory = new ClientFactory("http://localhost:" + port, null, null);
        var workerFactory = new PlatformWorkerFactory(clientFactory);
        var extractor = new Aggregator(workerFactory);
        extractor.aggregate(List.of("persons", "persons"), event -> {
            System.out.println(cnt.incrementAndGet() + ": " + event);
        }, 10000);
    }

    @RestController
    static class SseController {
        final AtomicInteger no = new AtomicInteger();

        @GetMapping(path = "/persons", produces = "text/event-stream")
        public Flux<Object> getPersonStream() {
            return Flux.interval(ofMillis(100)).map(index -> "{\"show\": {\"show_id\": \"" + no.incrementAndGet() + "\"}}");
        }
    }

    private static Event event() {
        return Event
                .builder()
                .id(UUID.randomUUID().toString())
                .build();
    }

    private static Event.Payload payload() {
        return Event.Payload
                .builder()
                .build();
    }

    private static Event.Show show() {
        return Event.Show
                .builder()
                .build();
    }

    private static Event.User user() {
        return Event.User
                .builder()
                .id(UUID.randomUUID().toString())
                .build();
    }
}
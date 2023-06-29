package antivoland.sytac;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@EnableAutoConfiguration
@SpringBootTest(classes = {TestController.class}, webEnvironment = RANDOM_PORT)
class AggregatorTest {
    @LocalServerPort
    int port;

    @Test
    void test() {
        var clientFactory = new ClientFactory("http://localhost:" + port, "🤖", "🔑");
        var workerFactory = new PlatformWorkerFactory(clientFactory);
        var aggregator = new Aggregator(workerFactory);
        var view = aggregator.aggregate(List.of("sytflix", "sytazon", "sysney"), 1000);

        assertThat(view.users).containsOnlyKeys("john", "william", "sytac");

        var john = view.users.get("john");
        assertThat(john.id).isEqualTo("john");
        assertThat(john.name).isEqualTo("John john");
        assertThat(john.age).isGreaterThan(0);
        assertThat(john.events).hasSize(3 * 8);
        assertThat(john.successfulStreamingEvents).isEqualTo(3);

        var william = view.users.get("william");
        assertThat(william.id).isEqualTo("william");
        assertThat(william.name).isEqualTo("William william");
        assertThat(william.age).isGreaterThan(0);
        assertThat(william.events).hasSize(3);
        assertThat(william.successfulStreamingEvents).isEqualTo(0);

        var sytac = view.users.get("sytac");
        assertThat(sytac.id).isEqualTo("sytac");
        assertThat(sytac.name).isEqualTo("Sytac sytac");
        assertThat(sytac.age).isGreaterThan(0);
        assertThat(sytac.events).hasSize(3);
        assertThat(sytac.successfulStreamingEvents).isEqualTo(0);

        assertThat(view.runtimeDurationMillis).isGreaterThan(0);
        assertThat(view.getSytflixPercentageOfStartedStreamEvents()).isCloseTo(0.4, offset(0.01));
        assertThat(view.getShowsReleasedIn2020OrLater()).isEqualTo(2);
    }
}
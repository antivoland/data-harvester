package antivoland.sytac;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@EnableAutoConfiguration
@SpringBootTest(classes = {TestController.class}, webEnvironment = RANDOM_PORT)
class PlatformWorkerTest {
    @LocalServerPort
    int port;

    @Test
    void test() throws InterruptedException {
        var clientFactory = new ClientFactory("http://localhost:" + port, "🤖", "🔑");
        var workerFactory = new PlatformWorkerFactory(clientFactory);
        var listener = new TestListener();
        var workers = Stream
                .of("sytflix", "sytazon", "sysney")
                .map(platform -> workerFactory.newWorker(platform, listener))
                .toList();
        workers.forEach(PlatformWorker::start);
        Thread.sleep(1000);
        workers.forEach(PlatformWorker::stop);
        assertThat(listener.events).hasSize(3 * TestEventSets.SIZE);
        assertThat(listener.events).containsAll(TestEventSets.events("sytflix"));
        assertThat(listener.events).containsAll(TestEventSets.events("sytazon"));
        assertThat(listener.events).containsAll(TestEventSets.events("sysney"));
        assertThat(listener.sytacUserEvents).isEqualTo(3);
        assertThat(listener.successfulStreamingEvents).isEqualTo(Map.of("john", 3));
        assertThat(listener.errors).isEqualTo(0);
    }
}